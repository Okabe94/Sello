"""What adb prints must be read as the numbers it states, never guessed or rounded."""
import unittest

import measure

AM_START = """Starting: Intent { cmp=com.software.sello/.MainActivity }
Status: ok
LaunchState: COLD
Activity: com.software.sello/.MainActivity
TotalTime: 836
WaitTime: 839
Complete
"""

LOGCAT = """10-23 09:40:01.100   512   540 I ActivityTaskManager: Displayed com.software.sello/.MainActivity: +836ms
10-23 09:40:01.400   512   540 I ActivityTaskManager: Fully drawn com.software.sello/.MainActivity: +1s105ms
10-23 09:41:02.400   512   540 I ActivityTaskManager: Fully drawn com.software.sello.debug/com.software.sello.MainActivity: +99ms
10-23 09:42:03.400   512   540 I ActivityTaskManager: Fully drawn com.software.sello/.MainActivity: +912ms
"""

GFXINFO = """Applications Graphics Acceleration Info:
Uptime: 293127 Realtime: 293127

** Graphics info for pid 5384 [com.software.sello] **

Stats since: 290126359429ns
Total frames rendered: 361
Janky frames: 15 (4.16%)
50th percentile: 5ms
90th percentile: 6ms
95th percentile: 8ms
99th percentile: 18ms
Number Missed Vsync: 2
"""

FRAMESTATS = """Window: com.software.sello/com.software.sello.MainActivity
Stats since: 293126359429ns
Total frames rendered: 3

---PROFILEDATA---
Flags,IntendedVsync,Vsync,OldestInputEvent,NewestInputEvent,HandleInputStart,AnimationStart,PerformTraversalsStart,DrawStart,SyncQueued,SyncStart,IssueDrawCommandsStart,SwapBuffers,FrameCompleted,DequeueBufferDuration,QueueBufferDuration,GpuCompleted,
0,1000000000,1000000000,0,0,0,0,0,0,0,0,0,0,1214000000,0,0,0,
1,1100000000,1100000000,0,0,0,0,0,0,0,0,0,0,9000000000,0,0,0,
0,1300000000,1300000000,0,0,0,0,0,0,0,0,0,0,1568000000,0,0,0,
---PROFILEDATA---

View hierarchy:

  com.software.sello/com.software.sello.MainActivity/android.view.ViewRootImpl@a1b2c3
  8 views, 12.50 kB of display lists
"""

DUMP = """<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0">
<node index="0" text="" resource-id="" class="android.widget.FrameLayout" package="com.software.sello" content-desc="" clickable="false" bounds="[0,0][1080,2072]">
<node index="0" text="Tus categorías" resource-id="" class="android.widget.TextView" package="com.software.sello" content-desc="" clickable="false" bounds="[77,1061][871,1115]" />
<node index="1" text="" resource-id="" class="android.view.View" package="com.software.sello" content-desc="Abrir configuración" clickable="true" bounds="[964,189][1030,255]" />
</node></hierarchy>"""


class LaunchTest(unittest.TestCase):
    def test_reads_the_launch_state_and_first_frame_time(self):
        self.assertEqual(("COLD", 836), measure.launch_times(AM_START))

    def test_a_launch_without_a_time_is_an_error_not_zero(self):
        with self.assertRaises(ValueError):
            measure.launch_times("Status: timeout\n")

    def test_fully_drawn_takes_the_last_report_of_this_package_in_milliseconds(self):
        self.assertEqual(912, measure.fully_drawn(LOGCAT))
        self.assertEqual(1105, measure.fully_drawn(LOGCAT.splitlines()[1]))

    def test_no_fully_drawn_report_is_none(self):
        self.assertIsNone(measure.fully_drawn(LOGCAT.splitlines()[0]))


class FramesTest(unittest.TestCase):
    def test_reads_the_frame_totals_and_percentiles(self):
        summary = measure.frame_summary(GFXINFO)
        self.assertEqual((361, 15, 5, 6, 8, 18),
                         (summary.total, summary.janky, summary.p50, summary.p90, summary.p95, summary.p99))
        self.assertAlmostEqual(4.155, summary.janky_percent, places=3)

    def test_missing_totals_are_an_error(self):
        with self.assertRaises(ValueError):
            measure.frame_summary("Total frames rendered: 3\n")

    def test_only_valid_frames_inside_the_profile_block_are_rows(self):
        rows = measure.frame_rows(FRAMESTATS)
        self.assertEqual([1000000000, 1300000000], [row["IntendedVsync"] for row in rows])

    def test_settling_runs_from_the_first_intended_start_to_the_last_completion(self):
        self.assertEqual(568.0, measure.settle_ms(measure.frame_rows(FRAMESTATS)))
        self.assertIsNone(measure.settle_ms([]))


class ScreenTest(unittest.TestCase):
    def test_reads_each_nodes_text_description_and_centre(self):
        nodes = measure.nodes(DUMP)
        self.assertEqual(3, len(nodes))
        self.assertEqual(("Tus categorías", (474, 1088)), (nodes[1]["text"], nodes[1]["centre"]))
        self.assertEqual(("Abrir configuración", True), (nodes[2]["desc"], nodes[2]["clickable"]))


if __name__ == "__main__":
    unittest.main()
