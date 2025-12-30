# -*- coding: utf-8 -*-
"""
common.py
---------
Shared helpers/constants (minimal).
"""

from __future__ import annotations
import time


def banner(title: str) -> None:
    print("\n" + "=" * 70)
    print(title)
    print("=" * 70 + "\n")


def sleep_seconds(sec: int) -> None:
    # Small wrapper for clarity
    time.sleep(sec)
