#!/usr/bin/env python3
"""
Architecture, DI, and Code Health Gatekeeper for GeoQibla.
Enforces modular structure, clean architecture boundaries, DI principles,
and complexity limits on ViewModels and Composables.
"""

import os
import re
import sys
from pathlib import Path

# Resolve repository root
ROOT = Path(__file__).resolve().parents[2]
SHARED_COMMON = ROOT / "shared" / "src" / "commonMain"
SHARED_ANDROID = ROOT / "shared" / "src" / "androidMain"
SHARED_IOS = ROOT / "shared" / "src" / "iosMain"
ANDROID_APP = ROOT / "androidApp" / "src"

# Thresholds
MAX_VIEWMODEL_LINES = 400
WARN_VIEWMODEL_LINES = 300
MAX_VIEWMODEL_PROPS = 20
MAX_VIEWMODEL_FUNCS = 20
MAX_COMPOSABLE_FILE_LINES = 800
WARN_COMPOSABLE_FILE_LINES = 450
MAX_LINE_LENGTH = 140
MAX_NESTING_INDENT = 28  # 7 levels of 4-space indent

errors = []
warnings = []

def log_error(file_path: Path, line_no: int, message: str):
    rel = file_path.relative_to(ROOT)
    errors.append(f"{rel}:{line_no}: ERROR: {message}")
    print(f"::error file={rel},line={line_no}::{message}")

def log_warning(file_path: Path, line_no: int, message: str):
    rel = file_path.relative_to(ROOT)
    warnings.append(f"{rel}:{line_no}: WARNING: {message}")
    print(f"::warning file={rel},line={line_no}::{message}")

def check_architecture_boundaries():
    """Ensure commonMain stays pure Kotlin and platform-agnostic."""
    if not SHARED_COMMON.exists():
        return

    forbidden_common_imports = [
        re.compile(r"^\s*import\s+android\."),
        re.compile(r"^\s*import\s+androidx\.appcompat\."),
        re.compile(r"^\s*import\s+com\.google\.android\."),
        re.compile(r"^\s*import\s+platform\."),  # iOS native in commonMain
    ]

    for kt_file in SHARED_COMMON.rglob("*.kt"):
        lines = kt_file.read_text(encoding="utf-8", errors="ignore").splitlines()
        for idx, line in enumerate(lines, 1):
            for pat in forbidden_common_imports:
                if pat.search(line):
                    log_error(
                        kt_file,
                        idx,
                        f"Architecture violation: illegal platform import in commonMain: '{line.strip()}'. "
                        "Keep commonMain pure multiplatform."
                    )

def check_god_viewmodels_and_controllers():
    """Detect god classes in ViewModels, StateMachines, and Controllers."""
    candidates = (
        list(ROOT.rglob("*ViewModel*.kt")) +
        list(ROOT.rglob("*Controller*.kt")) +
        list(ROOT.rglob("*StateMachine*.kt"))
    )

    for kt_file in candidates:
        if "build" in kt_file.parts or "test" in str(kt_file).lower():
            continue

        lines = kt_file.read_text(encoding="utf-8", errors="ignore").splitlines()
        total_lines = len(lines)

        if total_lines > MAX_VIEWMODEL_LINES:
            log_error(
                kt_file,
                1,
                f"God ViewModel/Controller detected! {total_lines} lines exceeds maximum allowed "
                f"({MAX_VIEWMODEL_LINES} lines). Decompose into domain use-cases or smaller controllers."
            )
        elif total_lines > WARN_VIEWMODEL_LINES:
            log_warning(
                kt_file,
                1,
                f"ViewModel/Controller approaching limit: {total_lines} lines (warning threshold: {WARN_VIEWMODEL_LINES})."
            )

        prop_count = sum(1 for l in lines if re.match(r"^\s*(val|var)\s+\w+", l))
        if prop_count > MAX_VIEWMODEL_PROPS:
            log_warning(
                kt_file,
                1,
                f"ViewModel/Controller declares {prop_count} properties (threshold: {MAX_VIEWMODEL_PROPS}). "
                "Consider consolidating state into a single UiState data class."
            )

        func_count = sum(1 for l in lines if re.match(r"^\s*fun\s+\w+", l))
        if func_count > MAX_VIEWMODEL_FUNCS:
            log_warning(
                kt_file,
                1,
                f"ViewModel/Controller declares {func_count} functions (threshold: {MAX_VIEWMODEL_FUNCS}). "
                "Split responsibilities into domain delegates."
            )

def check_di_principles():
    """Check for violation of Dependency Injection principles (e.g. hardcoded service instantiation)."""
    forbidden_instantations = [
        re.compile(r"val\s+\w+\s*=\s*(DefaultQiblaController|SensorManagerImpl|LocationServiceImpl)\("),
    ]

    for kt_file in ROOT.rglob("*.kt"):
        if "build" in kt_file.parts or "test" in str(kt_file).lower():
            continue

        lines = kt_file.read_text(encoding="utf-8", errors="ignore").splitlines()
        for idx, line in enumerate(lines, 1):
            for pat in forbidden_instantations:
                if pat.search(line):
                    log_error(
                        kt_file,
                        idx,
                        f"DI violation: Direct instantiation of concrete dependency in '{line.strip()}'. "
                        "Inject interfaces via constructor."
                    )

def check_composable_decomposition():
    """Ensure Composable UI files do not become monolithic god-files."""
    for kt_file in ROOT.rglob("*.kt"):
        if "build" in kt_file.parts or "test" in str(kt_file).lower():
            continue

        content = kt_file.read_text(encoding="utf-8", errors="ignore")
        if "@Composable" not in content:
            continue

        lines = content.splitlines()
        total_lines = len(lines)

        if total_lines > MAX_COMPOSABLE_FILE_LINES:
            log_error(
                kt_file,
                1,
                f"Monolithic Composable file: {total_lines} lines exceeds strict threshold of "
                f"{MAX_COMPOSABLE_FILE_LINES} lines. Decompose UI into separate component files."
            )
        elif total_lines > WARN_COMPOSABLE_FILE_LINES:
            log_warning(
                kt_file,
                1,
                f"Large Composable file: {total_lines} lines (warning threshold: {WARN_COMPOSABLE_FILE_LINES}). "
                "Consider extracting sub-components into standalone files."
            )

def check_spaghetti_metrics():
    """Flag lines that are excessively long or start deeply nested blocks."""
    for kt_file in ROOT.rglob("*.kt"):
        if "build" in kt_file.parts:
            continue

        lines = kt_file.read_text(encoding="utf-8", errors="ignore").splitlines()
        in_deep_block = False

        for idx, line in enumerate(lines, 1):
            stripped = line.strip()
            if not stripped:
                in_deep_block = False
                continue

            # Skip comments, imports, markdown/strings for line length
            if not stripped.startswith("//") and not stripped.startswith("import ") and not stripped.startswith("*"):
                if len(line) > MAX_LINE_LENGTH:
                    log_warning(kt_file, idx, f"Line exceeds {MAX_LINE_LENGTH} characters ({len(line)} chars).")

            # Check excessive indentation at block start
            indent = len(line) - len(line.lstrip(" "))
            if indent >= MAX_NESTING_INDENT and not stripped.startswith("//"):
                if not in_deep_block:
                    log_warning(
                        kt_file,
                        idx,
                        f"Excessive nesting depth ({indent // 4} levels). Consider refactoring block into a helper function."
                    )
                    in_deep_block = True
            else:
                in_deep_block = False

def main():
    print("=======================================================")
    print("   GeoQibla Architecture, DI & Code Health Gatekeeper   ")
    print("=======================================================")

    check_architecture_boundaries()
    check_god_viewmodels_and_controllers()
    check_di_principles()
    check_composable_decomposition()
    check_spaghetti_metrics()

    print("-------------------------------------------------------")
    print(f"Summary: {len(errors)} error(s), {len(warnings)} warning(s).")
    print("=======================================================")

    if errors:
        print("\nPipeline check FAILED: Architectural rules violated.")
        sys.exit(1)
    else:
        print("\nPipeline check PASSED: Architecture and code health validated.")
        sys.exit(0)

if __name__ == "__main__":
    main()
