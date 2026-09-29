#!/usr/bin/env python3
"""Генератор иконки wrecking_ball.png (16x16) по референсам (docs/art/wrecking_ball.md):
медно-коричневая сфера с оранжевыми изогнутыми бороздами и 14 золотыми сателлитами.
Запуск: python3 gen_wrecking_ball_icon.py <выходной.png>
Это детерминированный расчёт (сфера + числа Фибоначчи), а не рисунок «на глаз»: правки палитры
и числа сателлитов вносим здесь и перегенерируем."""
import math, sys
from PIL import Image

def C(h, a=255):
    h = h.lstrip('#'); return (int(h[0:2],16), int(h[2:4],16), int(h[4:6],16), a)

OUT = C('280e00')
COP, COP_D, COP_DD = C('893f10'), C('5a230a'), C('3a1406')
GRV, GRV_L = C('e8601c'), C('f29f6c')
GOLD, GOLD_D, GOLD_L = C('ebc731'), C('b7840f'), C('fbe98a')
SATELLITES = 14
SIZE, R = 16, 7.3
CX = CY = 7.5
LIGHT = (-0.5, -0.6, 0.62)  # сверху-слева, к зрителю
n = math.sqrt(sum(c*c for c in LIGHT)); LIGHT = tuple(c/n for c in LIGHT)

def fib(count):
    pts = []
    ga = math.pi * (3 - math.sqrt(5))
    for i in range(count):
        y = 1 - 2 * (i + 0.5) / count
        r = math.sqrt(1 - y*y)
        pts.append((r*math.cos(ga*i), y, r*math.sin(ga*i)))
    return pts

def rot(p, ax, ay):  # поворот: вокруг X, затем вокруг Y
    x, y, z = p
    y, z = y*math.cos(ax) - z*math.sin(ax), y*math.sin(ax) + z*math.cos(ax)
    x, z = x*math.cos(ay) + z*math.sin(ay), -x*math.sin(ay) + z*math.cos(ay)
    return x, y, z

AX, AY = math.radians(-20), math.radians(25)
img = Image.new('RGBA', (SIZE, SIZE), (0,0,0,0)); px = img.load()

# --- корпус ---
for j in range(SIZE):
    for i in range(SIZE):
        dx, dy = (i - CX)/R, (j - CY)/R
        d2 = dx*dx + dy*dy
        if d2 > 1: continue
        z = math.sqrt(1 - d2)
        lit = dx*LIGHT[0] + dy*LIGHT[1] + z*LIGHT[2]
        col = COP if lit > 0.35 else COP_D if lit > -0.1 else COP_DD
        # борозды: меридианы, наклонённые как на референсе
        px_, py_, pz_ = rot((dx, dy, z), math.radians(20), math.radians(-30))
        lon = (math.atan2(pz_, px_) / (2*math.pi)) % 1.0
        f = (lon * 6) % 1.0
        if f < 0.2 and abs(py_) < 0.97:
            col = GRV_L if (f < 0.09 and lit > 0.35) else GRV if lit > -0.1 else COP_D
        px[i, j] = col

# --- контур корпуса ---
for j in range(SIZE):
    for i in range(SIZE):
        if px[i, j][3]: continue
        for di, dj in ((1,0),(-1,0),(0,1),(0,-1)):
            a, b = i+di, j+dj
            if 0 <= a < SIZE and 0 <= b < SIZE and px[a, b][3] and px[a, b] != OUT and ((i-CX)**2+(j-CY)**2) < (R+1.3)**2:
                px[i, j] = OUT; break

# --- сателлиты 2x2 (сзади вперёд), вокруг каждого тёмное гнездо ---
sats = sorted((rot(p, AX, AY) for p in fib(SATELLITES)), key=lambda p: p[2])
for x, y, z in sats:
    if z < 0.1: continue
    ix, iy = int(round(CX + x*R*0.9 - 0.5)), int(round(CY + y*R*0.9 - 0.5))
    for a in range(-1, 3):
        for b in range(-1, 3):
            X, Y = ix+a, iy+b
            if 0 <= X < SIZE and 0 <= Y < SIZE and px[X, Y][3] and not (0 <= a <= 1 and 0 <= b <= 1):
                px[X, Y] = COP_DD
    for a in (0, 1):
        for b in (0, 1):
            X, Y = ix+a, iy+b
            if 0 <= X < SIZE and 0 <= Y < SIZE and px[X, Y][3]:
                px[X, Y] = GOLD_L if (a, b) == (0, 0) else GOLD_D if (a, b) == (1, 1) else GOLD

img.save(sys.argv[1] if len(sys.argv) > 1 else 'wrecking_ball.png')
