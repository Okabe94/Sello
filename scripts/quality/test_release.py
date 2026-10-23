"""Release facts must come from what the tools print, and the pinned certificate must be exact."""
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import release

APKSIGNER = """Verifies
Verified using v1 scheme (JAR signing): false
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Verified using v3.1 scheme (APK Signature Scheme v3.1): false
Verified using v4 scheme (APK Signature Scheme v4): false
Verified for SourceStamp: false
Number of signers: 1
Signer #1 certificate DN: CN=Unknown
Signer #1 certificate SHA-256 digest: abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952
Signer #1 certificate SHA-1 digest: 0123456789abcdef0123456789abcdef01234567
"""

BADGING = """package: name='com.software.sello' versionCode='1' versionName='1.0' platformBuildVersionName='17' compileSdkVersion='37'
minSdkVersion:'30'
targetSdkVersion:'37'
application-label:'Sello'
launchable-activity: name='com.software.sello.MainActivity'  label='' icon=''
"""


class SignersTest(unittest.TestCase):
    def test_reads_verification_schemes_and_certificate(self):
        self.assertEqual(
            {"verifies": True, "schemes": ["v2", "v3"],
             "certificates": ["abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952"]},
            release.signers(APKSIGNER))

    def test_a_signer_named_by_its_sdk_range_counts_once(self):
        output = APKSIGNER.replace("Signer #1 certificate SHA-256", "Signer (minSdkVersion=30, maxSdkVersion=2147483647) certificate SHA-256")
        output += "Signer (minSdkVersion=33, maxSdkVersion=2147483647) certificate SHA-256 digest: " + "abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952\n"
        self.assertEqual(["abf6ccf5b58de43626daea387d5c18c67981e42c302f8a1c7e16ad5cf0b4d952"],
                         release.signers(output)["certificates"])

    def test_apksigner_37_names_the_signer_by_scheme(self):
        output = (
            "Verifies\nVerified using v3 scheme (APK Signature Scheme v3): true\n"
            "Verified using v3.2 scheme (APK Signature Scheme v3.2): false\nNumber of signers: 1\n"
            "V3.0 Signer: certificate DN: CN=Sello CI ephemeral\n"
            "V3.0 Signer: certificate SHA-256 digest: " + "6f" * 32 + "\n"
            "V3.0 Signer: certificate SHA-1 digest: c9a9239e98891c5dfb789df384935b35c45814c2\n"
            "V3.0 Signer: public key SHA-256 digest: " + "39" * 32 + "\n"
            "Source Stamp Signer: certificate SHA-256 digest: " + "aa" * 32 + "\n"
        )
        self.assertEqual({"verifies": True, "schemes": ["v3"], "certificates": ["6f" * 32]}, release.signers(output))

    def test_an_unsigned_or_failing_apk_does_not_verify(self):
        self.assertEqual({"verifies": False, "schemes": [], "certificates": []},
                         release.signers("DOES NOT VERIFY\nERROR: Missing META-INF/MANIFEST.MF\n"))


class BadgingTest(unittest.TestCase):
    def test_reads_identity_versions_and_a_release_build(self):
        facts = release.badging(BADGING)
        self.assertEqual(("com.software.sello", "1", "1.0", 30, 37, False),
                         (facts["package"], facts["version_code"], facts["version_name"],
                          facts["min_sdk"], facts["target_sdk"], facts["debuggable"]))

    def test_a_debuggable_build_is_reported(self):
        self.assertTrue(release.badging(BADGING + "application-debuggable\n")["debuggable"])

    def test_no_package_line_is_an_error(self):
        with self.assertRaises(release.ReleaseError):
            release.badging("minSdkVersion:'30'\n")


class PinnedCertificateTest(unittest.TestCase):
    def pinned(self, text):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "pin.txt"
            path.write_text(text, encoding="utf-8")
            return release.pinned_certificate(path)

    def test_the_repository_pins_one_digest(self):
        self.assertRegex(release.pinned_certificate(), r"^[0-9a-f]{64}$")

    def test_comments_are_ignored_and_case_is_folded(self):
        self.assertEqual("ab" * 32, self.pinned("# owner's key\n" + "AB" * 32 + "\n"))

    def test_anything_but_exactly_one_digest_is_refused(self):
        for text in ("", "ab" * 31 + "\n", "ab" * 32 + "\n" + "cd" * 32 + "\n"):
            with self.assertRaises(release.ReleaseError):
                self.pinned(text)


class ProductionTest(unittest.TestCase):
    def test_without_a_signed_apk_g3_is_blocked_not_downgraded(self):
        with tempfile.TemporaryDirectory() as root:
            with self.assertRaisesRegex(release.ReleaseError, "blocked"):
                release.production(root)

    def test_a_certificate_other_than_the_pinned_one_fails(self):
        with tempfile.TemporaryDirectory() as root:
            (Path(root) / release.SIGNED).parent.mkdir(parents=True)
            (Path(root) / release.SIGNED).write_bytes(b"apk")
            (Path(root) / release.MAPPING).parent.mkdir(parents=True)
            (Path(root) / release.MAPPING).write_text("map", encoding="utf-8")
            other = APKSIGNER.replace("abf6ccf5", "00000000")
            outputs = {"aapt2": BADGING, "apksigner": other}
            with patch.object(release, "build_tools", return_value=Path("/tools")), \
                    patch.object(release, "run", side_effect=lambda argv, **_: outputs[Path(str(argv[0])).name]):
                with self.assertRaisesRegex(release.ReleaseError, "not the pinned certificate"):
                    release.production(root)


class LaunchTest(unittest.TestCase):
    def test_a_physical_device_is_refused_before_anything_is_installed(self):
        calls = []

        def fake(argv, **_):
            calls.append(argv)
            class Result:
                returncode = 0
                stdout = "0\n"
                stderr = ""
            return Result()

        with tempfile.TemporaryDirectory() as root, patch.object(release.subprocess, "run", side_effect=fake):
            with self.assertRaisesRegex(release.ReleaseError, "not an emulator"):
                release.launch(root, serial="R5CY149QBNJ")
        self.assertFalse(any("install" in argv for argv in calls))


if __name__ == "__main__":
    unittest.main()
