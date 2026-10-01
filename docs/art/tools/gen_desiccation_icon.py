#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mob effect icon textures/mob_effect/desiccation.png (18x18), see docs/art/desiccation_effect.md.

Reference: SBR chapter 32 (manga; the anime has not reached it yet): Gyro spins himself with the
Steel Ball and wrings the water out of his body in a spray. Spin rings take the anime look of the
other Spin icons (green ball, #3f8f4a / #7fcf7a). The water drop is squeezed by two spin rings,
has a sandy dry crust (particle colour 0xc2a36b from DesiccationEffect) and sheds droplets below.

Shapes are 8x supersampled masks, reduced with a hard threshold (no anti-aliasing), shaded with
light from the top-left and outlined with #1e2a33.

Usage (project root):  python3 docs/art/tools/gen_desiccation_icon.py [--sheet] [--textures <dir>]
"""
import math, os, sys
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.dirname(os.path.dirname(os.path.dirname(HERE)))
TEX = os.path.join(PROJECT, 'src', 'main', 'resources', 'assets', 'rotp_spin', 'textures')
K = 8
N = 18


def C(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


OL = C('1e2a33')
WATER = (C('4a8fd8'), C('bfe3ff'), C('2a5a9a'))   # base, highlight, shadow
SAND = C('c2a36b')
SPIN = (C('3f8f4a'), C('7fcf7a'))                  # ring base, ring highlight


def cells(draw_fn):
    m = Image.new('L', (N * K, N * K), 0)
    draw_fn(ImageDraw.Draw(m))
    m = m.resize((N, N), Image.BOX).point(lambda v: 255 if v >= 128 else 0)
    px = m.load()
    return {(x, y) for y in range(N) for x in range(N) if px[x, y]}


def drop_shape(d):
    cx, cy, r = 9.0, 10.2, 4.5
    pts = [(cx * K, 1.4 * K)]
    for i in range(0, 181, 6):                       # round bottom half + flanks up to the tip
        a = math.radians(-20 + i * (220 / 180.0))
        pts.append(((cx + r * math.cos(a)) * K, (cy + r * math.sin(a)) * K))
    d.polygon(pts, fill=255)


def ring(a0, a1, tilt=-18, rx=7.8, ry=2.0, cy=10.4, w=1.1):
    def f(d):
        t = math.radians(tilt)
        pts = []
        for i in range(41):
            a = math.radians(a0 + (a1 - a0) * i / 40)
            x, y = rx * math.cos(a), ry * math.sin(a)
            pts.append(((9 + x * math.cos(t) - y * math.sin(t)) * K, (cy + x * math.sin(t) + y * math.cos(t)) * K))
        d.line(pts, fill=255, width=int(w * K), joint='curve')
    return f


def outline(img, cs, only_empty=False):
    for (x, y) in cs:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            p = (x + dx, y + dy)
            if p not in cs and 0 <= p[0] < N and 0 <= p[1] < N:
                if not only_empty or img.getpixel(p)[3] == 0:
                    img.putpixel(p, OL)


def paint_ring(img, cs):
    outline(img, cs, only_empty=True)              # thin ring: never ink over the drop
    for (x, y) in cs:
        img.putpixel((x, y), SPIN[1] if (x, y - 1) not in cs else SPIN[0])


def icon():
    img = Image.new('RGBA', (N, N), (0, 0, 0, 0))
    back = cells(ring(200, 340))                     # far half of the ring, behind the drop
    paint_ring(img, back)
    drop = cells(drop_shape)
    outline(img, drop)
    base, hi, sh = WATER
    for (x, y) in drop:
        c = base
        if (x + 1, y) not in drop or (x, y + 1) not in drop:
            c = SAND if y >= 9 else sh               # dry sandy crust on the lower right rim
        elif (x + 2, y) not in drop or (x, y + 2) not in drop:
            c = sh
        img.putpixel((x, y), c)
    for (x, y) in ((8, 5), (7, 7), (6, 8), (6, 9), (7, 8)):  # highlight top-left
        if (x, y) in drop:
            img.putpixel((x, y), hi)
    front = cells(ring(20, 160))                     # near half, over the drop: the squeeze
    paint_ring(img, front)
    front2 = cells(ring(40, 140, tilt=12, rx=5.0, ry=1.4, cy=6.6, w=1.0))
    paint_ring(img, front2)
    for (x, y, c) in ((3, 16, WATER[0]), (4, 15, WATER[1]), (14, 16, WATER[0]), (13, 17, WATER[2]), (9, 17, WATER[1])):
        img.putpixel((x, y), c)                      # squeezed-out droplets
    return img


if __name__ == '__main__':
    root = TEX
    if '--textures' in sys.argv:
        root = sys.argv[sys.argv.index('--textures') + 1]
    img = icon()
    path = os.path.join(root, 'mob_effect', 'desiccation.png')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print('->', path, img.size)
    if '--sheet' in sys.argv:
        S = 12
        sheet = Image.new('RGBA', (N * S + 12, N * S + 12), (70, 72, 82, 255))
        sheet.alpha_composite(img.resize((N * S, N * S), Image.NEAREST), (6, 6))
        out = os.path.join(PROJECT, '.agent', 'preview')
        os.makedirs(out, exist_ok=True)
        sheet.convert('RGB').save(os.path.join(out, 'desiccation.png'))
