"""Run all lab scripts and print logs (partial)

This runner imports each lab as a module and runs its `__main__` demo.
"""
import subprocess
import sys
import os

scripts = [
    'lab1_contiguous.py',
    'lab2_linked.py',
    'lab3_fat.py',
    'lab4_indexed.py',
    'lab5_inode.py'
]

here = os.path.dirname(__file__)

for s in scripts:
    path = os.path.join(here, s)
    print('\n=== Running', s, '===')
    try:
        out = subprocess.check_output([sys.executable, path], stderr=subprocess.STDOUT, text=True)
        # print only first 20 lines to keep log snippet small
        lines = out.splitlines()
        print('\n'.join(lines[:20]))
    except subprocess.CalledProcessError as e:
        print('ERROR running', s)
        print(e.output)
