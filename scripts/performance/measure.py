"""Repeatable performance runs of an installed Sello build on one emulator or device.

Each run drives the app through `adb`: launches with `am start -W`, taps through
`uiautomator`, and reads frame timings from `dumpsys gfxinfo`. It never installs,
clears or resets anything, so the device and the build under test are chosen, and
the data restored, beforehand. Method and targets: docs/testing/performance-profile.md.
"""
import argparse
import json
import os
import re
import statistics
import subprocess
import sys
import time
import xml.etree.ElementTree as ElementTree
from dataclasses import dataclass

PACKAGE = "com.software.sello"
ACTIVITY = "com.software.sello.MainActivity"
# Shown once Recibo has read the month and its categories.
READY = "Tus categorías"
# Resumen's figures and its month-by-month chart, the first screenful.
RESUMEN_SHOWN = ("Ingresos menos gastos", "Mes a mes")
# A category's detail with its comparison to last month and its numbers, read from history.
CATEGORY_SHOWN = ("Frente a", "En números")
ANIMATION_SETTINGS = ("window_animation_scale", "transition_animation_scale", "animator_duration_scale")


class Adb:
    def __init__(self, serial):
        self.serial = serial

    def run(self, *args, check=True):
        result = subprocess.run(
            ["adb", "-s", self.serial, *args], capture_output=True, text=True, check=False,
        )
        if check and result.returncode != 0:
            raise RuntimeError(f"adb {' '.join(args)}: {result.stderr.strip() or result.stdout.strip()}")
        return result.stdout

    def shell(self, *args, check=True):
        return self.run("shell", *args, check=check)


# --- Reading what adb prints --------------------------------------------------------

def launch_times(output):
    """`am start -W`: the launch state and the time to the first frame, in ms."""
    state = re.search(r"^LaunchState: (\w+)", output, re.MULTILINE)
    total = re.search(r"^TotalTime: (\d+)", output, re.MULTILINE)
    if total is None:
        raise ValueError(f"no TotalTime in:\n{output}")
    return (state.group(1) if state else None), int(total.group(1))


def fully_drawn(log, package=PACKAGE):
    """The last "Fully drawn" time logged for [package], in ms: `+1s234ms` is 1234."""
    found = None
    for match in re.finditer(rf"Fully drawn {re.escape(package)}/\S+: \+(?:(\d+)s)?(\d+)ms", log):
        found = int(match.group(1) or 0) * 1000 + int(match.group(2))
    return found


@dataclass(frozen=True)
class FrameSummary:
    total: int
    janky: int
    p50: int
    p90: int
    p95: int
    p99: int

    @property
    def janky_percent(self):
        return 100.0 * self.janky / self.total if self.total else 0.0


def frame_summary(gfxinfo):
    """The totals `dumpsys gfxinfo <package>` prints for the app since its last reset."""
    def number(label):
        match = re.search(rf"^{label}: (\d+)", gfxinfo, re.MULTILINE)
        if match is None:
            raise ValueError(f"no {label!r} in gfxinfo")
        return int(match.group(1))

    return FrameSummary(
        total=number("Total frames rendered"), janky=number("Janky frames"),
        p50=number("50th percentile"), p90=number("90th percentile"),
        p95=number("95th percentile"), p99=number("99th percentile"),
    )


def frame_rows(framestats):
    """The valid frames of `dumpsys gfxinfo <package> framestats`, as dicts of ns timestamps."""
    rows, inside, header = [], False, None
    for line in framestats.splitlines():
        line = line.strip().rstrip(",")
        if line.startswith("---PROFILEDATA---"):
            inside, header = not inside, None
        elif inside and header is None:
            header = line.split(",")
        elif inside and line:
            values = dict(zip(header, (int(value) for value in line.split(","))))
            if values.get("Flags") == 0:
                rows.append(values)
    return rows


def settle_ms(rows):
    """From the first frame's intended start to the last frame's completion, in ms."""
    if not rows:
        return None
    return (max(row["FrameCompleted"] for row in rows) - min(row["IntendedVsync"] for row in rows)) / 1e6


def nodes(dump):
    """Every node of a `uiautomator dump` with its text, description and centre."""
    found = []
    for node in ElementTree.fromstring(dump).iter("node"):
        bounds = re.findall(r"\d+", node.get("bounds", ""))
        if len(bounds) != 4:
            continue
        left, top, right, bottom = map(int, bounds)
        found.append({
            "text": node.get("text", ""), "desc": node.get("content-desc", ""),
            "id": node.get("resource-id", ""), "package": node.get("package", ""),
            "clickable": node.get("clickable") == "true",
            "centre": ((left + right) // 2, (top + bottom) // 2),
            "bounds": (left, top, right, bottom),
        })
    return found


def median(values):
    return statistics.median(values) if values else None


# --- Driving the app -----------------------------------------------------------------

class Device:
    def __init__(self, adb):
        self.adb = adb

    def screen(self):
        self.adb.shell("uiautomator", "dump", "/sdcard/sello-ui.xml")
        return nodes(self.adb.shell("cat", "/sdcard/sello-ui.xml"))

    def find(self, label, timeout=15.0, exact=True):
        deadline = time.monotonic() + timeout
        while True:
            for node in self.screen():
                left, top, right, bottom = node["bounds"]
                if right <= left or bottom <= top:
                    continue
                for value in (node["text"], node["desc"]):
                    if value == label or (not exact and label in value):
                        return node
            if time.monotonic() > deadline:
                raise TimeoutError(f"{label!r} not on screen")
            time.sleep(0.3)

    def tap(self, label, **kwargs):
        x, y = self.find(label, **kwargs)["centre"]
        self.adb.shell("input", "tap", str(x), str(y))

    def density(self):
        return int(re.findall(r"\d+", self.adb.shell("wm", "density").splitlines()[-1])[-1]) / 160

    def tab(self, index, count=2):
        """
        The centre of a bottom tab on a compact phone. uiautomator cuts the window off
        above the tab bar, so the bar is found below Recibo's Anotar dock, which sits
        directly on it.
        """
        dock = self.find("Anotar un gasto")["bounds"]
        width, _ = self.size()
        return width * (2 * index + 1) // (2 * count), dock[3] + int(44 * self.density())

    def scroll_to(self, label, swipes=6, exact=True):
        width, height = self.size()
        for _ in range(swipes):
            try:
                return self.find(label, timeout=1.0, exact=exact)
            except TimeoutError:
                self.adb.shell("input", "swipe", str(width // 2), str(height * 3 // 4),
                               str(width // 2), str(height * 2 // 5), "300")
                time.sleep(0.8)
        return self.find(label, timeout=1.0, exact=exact)

    def size(self):
        match = re.search(r"(\d+)x(\d+)", self.adb.shell("wm", "size").splitlines()[-1])
        return int(match.group(1)), int(match.group(2))

    def animations(self, scale):
        for name in ANIMATION_SETTINGS:
            self.adb.shell("settings", "put", "global", name, str(scale))

    def frames(self):
        return self.adb.shell("dumpsys", "gfxinfo", PACKAGE)

    def reset_frames(self):
        self.adb.shell("dumpsys", "gfxinfo", PACKAGE, "reset")

    def wait_until_still(self, quiet=1.5, timeout=60.0):
        """Waits until the app draws no frame for [quiet] seconds."""
        deadline = time.monotonic() + timeout
        last, since = None, time.monotonic()
        while time.monotonic() < deadline:
            count = frame_summary(self.frames()).total
            if count != last:
                last, since = count, time.monotonic()
            elif time.monotonic() - since >= quiet:
                return
            time.sleep(0.2)
        raise TimeoutError("the app kept drawing")

    def launch(self, cold):
        args = ["am", "start-activity", "-W"] + (["-S"] if cold else []) + ["-n", f"{PACKAGE}/{ACTIVITY}"]
        return launch_times(self.adb.shell(*args))

    def home(self):
        self.adb.shell("input", "keyevent", "HOME")


# --- Measurements --------------------------------------------------------------------

def measure_startup(device, runs):
    cold, cold_drawn, warm = [], [], []
    for _ in range(runs):
        device.adb.shell("logcat", "-c")
        state, total = device.launch(cold=True)
        if state != "COLD":
            raise RuntimeError(f"expected a cold start, got {state}")
        device.find(READY)
        time.sleep(1.0)
        cold.append(total)
        cold_drawn.append(fully_drawn(device.adb.shell("logcat", "-d", "-s", "ActivityTaskManager")))
        # Back on the first screen ends the activity and keeps the process on API 30.
        device.adb.shell("input", "keyevent", "BACK")
        time.sleep(1.5)
        state, total = device.launch(cold=False)
        if state != "WARM":
            raise RuntimeError(f"expected a warm start, got {state}")
        warm.append(total)
        device.find(READY)
        time.sleep(1.0)
    return {
        "cold_first_frame_ms": cold, "cold_fully_drawn_ms": cold_drawn, "warm_first_frame_ms": warm,
        "median": {
            "cold_first_frame_ms": median(cold),
            "cold_fully_drawn_ms": median([value for value in cold_drawn if value is not None]),
            "warm_first_frame_ms": median(warm),
        },
    }


def measure_scroll(device, runs, swipes):
    width, height = device.size()
    x = str(width // 2)
    results = []
    for _ in range(runs):
        device.launch(cold=True)
        device.find(READY)
        time.sleep(2.0)
        device.reset_frames()
        for index in range(swipes):
            start, end = (0.8, 0.3) if index % 2 == 0 else (0.3, 0.8)
            device.adb.shell("input", "swipe", x, str(int(height * start)), x, str(int(height * end)), "150")
            time.sleep(0.6)
        device.wait_until_still()
        summary = frame_summary(device.frames())
        results.append({
            "frames": summary.total, "janky": summary.janky,
            "janky_percent": round(summary.janky_percent, 2), "p90_ms": summary.p90, "p99_ms": summary.p99,
        })
    return {
        "runs": results,
        "median": {
            "janky_percent": median([run["janky_percent"] for run in results]),
            "p90_ms": median([run["p90_ms"] for run in results]),
        },
    }


def shown(device, action, visible, pause=0.5, timeout=60.0):
    """
    Runs [action] and times the frames until the screen shows what it was opened for:
    from the first frame's intended start to the completion of the last frame drawn
    before a [pause] in which every label in [visible] began some text on screen. Frames
    a screen draws later, below the fold or unchanged, are not counted. Frame data is
    reset first: framestats keeps the app's recent frames, including older ones.
    """
    device.reset_frames()
    action()
    rows, since, checked = {}, time.monotonic(), 0
    deadline = since + timeout
    while time.monotonic() < deadline:
        fresh = frame_rows(device.adb.shell("dumpsys", "gfxinfo", PACKAGE, "framestats"))
        new = [row for row in fresh if row["IntendedVsync"] not in rows]
        if new:
            rows.update((row["IntendedVsync"], row) for row in new)
            since = time.monotonic()
        elif rows and len(rows) != checked and time.monotonic() - since >= pause:
            checked = len(rows)
            labels = [value for node in device.screen() for value in (node["text"], node["desc"])]
            if all(any(value.startswith(label) for value in labels) for label in visible):
                return settle_ms(list(rows.values()))
        time.sleep(0.1)
    raise TimeoutError(f"{visible} not shown")


def measure_resumen(device, runs):
    """The first opening in a new process, when nothing of Resumen is read or kept yet."""
    times = []
    for _ in range(runs):
        device.launch(cold=True)
        device.find(READY)
        time.sleep(2.0)
        x, y = device.tab(1)
        opened = shown(
            device, lambda: device.adb.shell("input", "tap", str(x), str(y)),
            RESUMEN_SHOWN,
        )
        times.append(round(opened))
    return {"open_ms": times, "median": {"open_ms": median(times)}}


def measure_category(device, runs, name):
    """The first opening of a category's detail in a new process, from Recibo."""
    times = []
    for _ in range(runs):
        device.launch(cold=True)
        device.find(READY)
        x, y = device.find(name, exact=False)["centre"]
        time.sleep(2.0)
        opened = shown(device, lambda: device.adb.shell("input", "tap", str(x), str(y)), CATEGORY_SHOWN)
        times.append(round(opened))
    return {"open_ms": times, "median": {"open_ms": median(times)}}


def open_backup_screen(device):
    device.launch(cold=True)
    device.find(READY)
    device.tap("Abrir configuración")
    device.find("Apariencia")
    device.scroll_to("Respaldo")
    device.tap("Respaldo")
    device.find("Último respaldo")


def system_dialog_save(device):
    """
    The system's "Save" button. uiautomator reports its bounds as empty on the
    reference phone, so it is tapped where it is drawn: calibrated for 1080 × 2340.
    """
    width, height = device.size()
    device.adb.shell("input", "tap", str(width * 852 // 1000), str(height * 915 // 1000))


def measure_backup(device, runs):
    """From Sello's first frame after "Save" until it says "Guardado"."""
    times = []
    for _ in range(runs):
        open_backup_screen(device)
        # "Crear respaldo", or "Crear otro respaldo" once there is one.
        x, y = device.scroll_to("Crear", exact=False)["centre"]
        device.adb.shell("input", "tap", str(x), str(y))
        time.sleep(3.0)
        device.animations(0)
        times.append(round(shown(device, lambda: system_dialog_save(device), ("Guardado",))))
        device.animations(1)
    return {"save_ms": times, "median": {"save_ms": median(times)}}


def measure_restore(device, runs):
    """
    Checking the file (from Sello's first frame after picking it until the preview)
    and replacing (from "Reemplazar con el respaldo" until it says so). The file must
    be the first in the picker's Downloads, where it is opened.
    """
    checks, replacements = [], []
    for _ in range(runs):
        open_backup_screen(device)
        device.scroll_to("Elegir archivo")
        device.tap("Elegir archivo")
        time.sleep(3.0)
        device.animations(0)
        try:
            # The picker answers keys where taps on its file grid go unnoticed.
            device.adb.shell("input", "keyevent", "DPAD_DOWN")
            device.adb.shell("input", "keyevent", "DPAD_DOWN")
            checks.append(round(shown(
                device, lambda: device.adb.shell("input", "keyevent", "ENTER"),
                ("Reemplazar con el respaldo",),
            )))
            confirm = device.find("Reemplazar con el respaldo")["centre"]
            replacements.append(round(shown(
                device, lambda: device.adb.shell("input", "tap", *map(str, confirm)),
                ("Se restauró el respaldo",),
            )))
        finally:
            device.animations(1)
    return {
        "check_ms": checks, "replace_ms": replacements,
        "median": {"check_ms": median(checks), "replace_ms": median(replacements)},
    }


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("journey", choices=("startup", "scroll", "resumen", "category", "backup", "restore", "screen"))
    parser.add_argument("--serial", default=os.environ.get("ANDROID_SERIAL"), required=False)
    parser.add_argument("--runs", type=int, default=10)
    parser.add_argument("--swipes", type=int, default=10)
    parser.add_argument("--category", default="Mercado", help="the category the category journey opens")
    args = parser.parse_args(argv)
    if not args.serial:
        sys.exit("name the target with --serial or ANDROID_SERIAL")
    device = Device(Adb(args.serial))
    if args.journey == "screen":
        for node in device.screen():
            if node["text"] or node["desc"]:
                print(node["package"], repr(node["text"]), repr(node["desc"]), node["id"], node["bounds"])
        return
    if args.journey == "startup":
        device.animations(1)
        result = measure_startup(device, args.runs)
    elif args.journey == "scroll":
        device.animations(1)
        result = measure_scroll(device, args.runs, args.swipes)
    elif args.journey == "category":
        device.animations(0)
        try:
            result = measure_category(device, args.runs, args.category)
        finally:
            device.animations(1)
    elif args.journey == "backup":
        result = measure_backup(device, args.runs)
    elif args.journey == "restore":
        result = measure_restore(device, args.runs)
    else:
        device.animations(0)
        try:
            result = measure_resumen(device, args.runs)
        finally:
            device.animations(1)
    print(json.dumps({"serial": args.serial, "journey": args.journey, **result}, indent=2))


if __name__ == "__main__":
    main()
