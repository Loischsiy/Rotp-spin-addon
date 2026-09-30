#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
textures/item/stand_disc_ball_breaker.png  16x16  (see docs/art/stand_disc_ball_breaker.md)

Silhouette = alpha mask of jojo:item/stand_disc (read from .refs/rotp, or the built-in copy below),
recoloured with the Ball Breaker palette: dark body, lime rim, pink dots, gold centre, outline #1e140b.
No anti-aliasing, only alpha 0 / 255.

Usage (project root):  python3 docs/art/tools/gen_stand_disc_ball_breaker.py [--preview]
"""
import os, sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.dirname(os.path.dirname(os.path.dirname(HERE)))
OUT = os.path.join(PROJECT, 'src', 'main', 'resources', 'assets', 'rotp_spin', 'textures', 'item',
                   'stand_disc_ball_breaker.png')

# alpha mask of the base RotP disc (X = opaque), 16x16
MASK = [
    '................',
    '................',
    '................',
    '.....XXXXX......',
    '..XXXXXXXXXXX...',
    '.XXXXXXXXXXXXX..',
    'XXXXXXXXXXXXXXX.',
    'XXXXXXXXXXXXXXX.',
    'XXXXXXXXXXXXXXX.',
    'XXXXXXXXXXXXXXX.',
    '.XXXXXXXXXXXXX..',
    '..XXXXXXXXXXX...',
    '.....XXXXX......',
    '................',
    '................',
    '................',
]


def C(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


OL = C('1e140b')
LIME, LIME_H, LIME_S = C('7ac74f'), C('b8e69a'), C('4a7a2e')
DARK, DARK_H, DARK_S = C('3f4a44'), C('6a7670'), C('232a27')
PINK = C('e060a8')
GOLD, GOLD_H = C('b8860b'), C('e0b84a')

W = H = 16
op = {(x, y) for y in range(H) for x in range(W) if MASK[y][x] == 'X'}


def erode(s):
    return {(x, y) for (x, y) in s
            if all((x + dx, y + dy) in s for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))}


l1 = erode(op)          # inside the outline
l2 = erode(l1)          # inside the rim
l3 = erode(l2)          # inner dark field
cx, cy = 7, 7

img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
px = img.load()
for (x, y) in op:
    px[x, y] = OL
RIM = l1 - l3                       # 2 px wide lime ring
for (x, y) in RIM:                  # light from the top-left
    if x + y <= cx + cy - 3:
        px[x, y] = LIME_H
    elif x + y >= cx + cy + 3:
        px[x, y] = LIME_S
    else:
        px[x, y] = LIME
for (x, y) in l3:                   # dark body
    px[x, y] = DARK_S if x + y >= cx + cy + 4 else DARK
for (x, y) in ((5, 6), (6, 6)):     # soft highlight on the body, top-left
    if (x, y) in l3:
        px[x, y] = DARK_H
# centre hole: golden ring around the hole
for (x, y) in ((cx, cy), (cx - 1, cy), (cx + 1, cy), (cx, cy + 1), (cx, cy - 1)):
    px[x, y] = GOLD
px[cx - 1, cy - 1] = GOLD_H
px[cx, cy] = OL                     # the hole itself stays dark, like the base disc
# pink 'eyes' on the rim
for (x, y) in ((6, 4), (11, 6), (2, 8), (8, 11)):
    assert (x, y) in RIM, (x, y)
    px[x, y] = PINK

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT)
print('wrote', os.path.relpath(OUT, PROJECT))

legend = {OL: 'o', LIME: 'L', LIME_H: 'l', LIME_S: 's', DARK: 'd', DARK_H: 'h', DARK_S: 'z',
          PINK: 'P', GOLD: 'G', GOLD_H: 'g'}
for y in range(H):
    print(''.join('.' if px[x, y][3] == 0 else legend.get(px[x, y], '?') for x in range(W)))

if '--preview' in sys.argv:
    pv = os.path.join(PROJECT, '.agent', 'preview')
    os.makedirs(pv, exist_ok=True)
    sheet = Image.new('RGBA', (16 * 12 * 2 + 12, 16 * 12), (110, 110, 110, 255))
    sheet.paste(img.resize((192, 192), Image.NEAREST), (0, 0), img.resize((192, 192), Image.NEAREST))
    small = img.resize((32, 32), Image.NEAREST)
    sheet.paste(small, (200, 8), small)
    sheet.save(os.path.join(pv, 'stand_disc_ball_breaker.png'))
