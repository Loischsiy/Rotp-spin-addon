#!/usr/bin/env python3
"""Action icons for Spinning Ball Strike and Spin Charge (32x32, flat pixel art).

TZ: docs/art/action_spin_ball_strike.md, docs/art/action_spin_ball_charge.md (palettes are copied from there).
  action/spin_ball_strike.png  fist gripping the green steel ball, 2 golden spin arcs with arrowheads,
                               3 golden impact rays and a dark figure knocked back up-right (no blood)
  action/spin_ball_charge.png  open palm under the green steel ball, tightening 2-turn golden spiral
                               ending at the ball, golden sparks above; no rays, no target

References (anime first, manga second):
  * anime ep. 1 ("1st STAGE"), JoJo Wiki File:Gyro_Holding_Steel_Ball_Anime.png: open palm, fingers
    spread and slightly cupped, the ball hovers just above the palm and spins inside thin vertical rings
    -> composition of spin_ball_charge (palm under the ball, rings tighten onto it);
  * anime OP "SPIN" (Kroi), File:JoJo_OP12_SPIN_Gyro_Holding_Steel_Ball.png: green ball with plate seams
    held by the fingertips -> fingers wrap over the ball's edge in spin_ball_strike;
  * manga SBR ch. 2-3 (duel with the thief, Johnny touches the spinning ball): Spin passes by contact.
The ball itself is the same sprite as textures/item/steel_ball.png (gen_steel_ball.icon via
gen_spin_icons.stamp_ball); shapes/shading use gen_frame_brace_icons.Canvas (light from top-left,
#1e140b outline on every transparent pixel next to the sprite, no semi-transparent pixels).

Run from the project root:
  python3 docs/art/tools/gen_ball_strike_charge_icons.py \\
      --out src/main/resources/assets/rotp_spin/textures [--preview .agent/preview/strike_charge.png]
Dependencies: python3, Pillow, numpy.
"""
import argparse
import math
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gen_frame_brace_icons import Canvas, N, rgb  # noqa: E402
from gen_spin_icons import stamp_ball  # noqa: E402

SKIN = (rgb('e0b48a'), rgb('a8744f'), rgb('e0b48a'))          # TZ: no separate highlight
GOLD = (rgb('d4a017'), rgb('8a6a0e'), rgb('f2d36b'))
TARGET = (rgb('4a3b2c'), rgb('2c2219'), rgb('4a3b2c'))


def overlay(dst, src):
    out = dst.copy()
    m = src[..., 3] > 0
    out[m] = src[m]
    return out


def line(c, m, x0, y0, x1, y1, w=1):
    steps = int(max(abs(x1 - x0), abs(y1 - y0)) * 4) + 1
    for i in range(steps + 1):
        t = i / steps
        x, y = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
        for dx in range(w):
            for dy in range(w):
                c.put(m, int(math.floor(x + dx - (w - 1) / 2.0)), int(math.floor(y + dy - (w - 1) / 2.0)))


def arc_pts(cx, cy, r, a0, a1):
    """Pixels of a circle arc, angles in screen space (y down), in drawing order a0 -> a1."""
    pts = []
    steps = max(24, int(abs(a1 - a0) * r * 6))
    for i in range(steps + 1):
        a = a0 + (a1 - a0) * i / steps
        p = (int(round(cx + r * math.cos(a))), int(round(cy + r * math.sin(a))))
        if not pts or pts[-1] != p:
            pts.append(p)
    return pts


def gold_lit(img, pts, cx, cy):
    """1 px gold line: highlight on the part facing the top-left light, shadow on the bottom-right."""
    for x, y in pts:
        if not (0 <= x < N and 0 <= y < N):
            continue
        k = (x - cx) + (y - cy)
        col = GOLD[2] if k < -3 else GOLD[1] if k > 4 else GOLD[0]
        img[y, x] = (*col, 255)


# ---------------------------------------------------------------- Spinning Ball Strike
BALL_S = (16.5, 15.5)      # ball centre (10 px ball)
# target knocked back up-right: head top-right, arm thrown back up-left, legs trailing (origin x=24, y=0)
TARGET_MAP = [
    '....##..',
    '...####.',
    '...####.',
    '#...##..',
    '.#.####.',
    '..######',
    '...####.',
    '...####.',
    '...###..',
    '..##.##.',
    '.##...#.',
    '##....##',
]


def strike():
    back = Canvas()
    skin = back.material(SKIN)
    gold = back.material(GOLD)
    target = back.material(TARGET)

    # forearm leaving the bottom-left corner + back of the fist (knuckles to the right)
    line(back, skin, 2.0, 30.5, 5.0, 21.0, w=5)
    back.rect(skin, 1, 11, 9, 20)
    for p in ((1, 11), (1, 20)):
        back.put(-1, *p)

    # two clockwise spin arcs around the ball, each with an arrowhead at its leading end
    cx, cy = BALL_S
    upper = arc_pts(cx, cy, 8.0, math.radians(215), math.radians(330))
    lower = arc_pts(cx, cy, 8.0, math.radians(35), math.radians(150))
    for x, y in upper + lower:
        back.put(gold, x, y)
    (ux, uy), (lx, ly) = upper[-1], lower[-1]
    for x, y in ((ux - 1, uy - 1), (ux - 1, uy + 1), (ux, uy + 1)):      # tip points down-right
        back.put(gold, x, y)
    for x, y in ((lx + 1, ly + 1), (lx + 1, ly - 1), (lx, ly - 1)):      # tip points up-left
        back.put(gold, x, y)

    # three short impact rays fanning out to the right of the ball
    for x, y in ((25, 14), (26, 13), (26, 16), (27, 16), (28, 16), (25, 19), (26, 20)):
        back.put(gold, x, y)

    for j, row in enumerate(TARGET_MAP):
        for i, ch in enumerate(row):
            if ch == '#':
                back.put(target, 24 + i, j)

    img = back.render()
    gold_lit(img, upper + lower, cx, cy)
    for x, y in ((26, 13), (28, 16), (26, 20)):
        img[y, x] = (*GOLD[2], 255)                      # ray tips glint

    img = stamp_ball(img, BALL_S, 10)

    # fingers curled over the left edge of the ball, thumb on top (front layer)
    front = Canvas()
    fs = front.material(SKIN)
    front.rect(fs, 10, 12, 12, 19)                         # curled fingers
    front.rect(fs, 3, 9, 10, 10)                           # thumb along the top
    fimg = front.render()
    for y in (14, 16):                                     # gaps between the fingers
        for x in (10, 11):
            fimg[y, x] = (*SKIN[1], 255)
    img = overlay(img, fimg)
    for y in range(12, 20):                                # crease between fingers and the back of the fist
        img[y, 9] = (*SKIN[1], 255)
    return img


# ---------------------------------------------------------------- Spin Charge
BALL_C = (15.5, 12.5)      # ball centre (12 px ball)
TURNS = 2   # TZ asks for 3, but r 13 -> 7 in 3 turns leaves 1 px gaps and merges into a web at 32x32


def spiral_pts(cx, cy, r0, r1, turns, a_end):
    """Archimedean spiral from r0 (outer) to r1 (inner), clockwise on screen, ending at angle a_end."""
    pts = []
    total = 2 * math.pi * turns
    steps = int(total * r0 * 3)
    for i in range(steps + 1):
        t = i / steps
        a = a_end - total * (1 - t)
        r = r0 + (r1 - r0) * t
        p = (int(round(cx + r * math.cos(a))), int(round(cy + r * math.sin(a) * 0.92)))
        if not pts or pts[-1] != p:
            pts.append(p)
    return pts


def charge():
    back = Canvas()
    gold = back.material(GOLD)
    cx, cy = BALL_C
    # TURNS turns from 26 px (r 13) down to 14 px (r 7, the ball's outline): the end touches the ball
    spiral = spiral_pts(cx, cy, 13.0, 7.0, TURNS, math.radians(-45))
    for x, y in spiral:
        back.put(gold, x, y)
    # sparks above the ball, in the free top corners
    sparks = [(27, 2), (4, 3), (29, 6)]
    for sx, sy in sparks[:2]:
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            back.put(gold, sx + dx, sy + dy)
    back.put(gold, *sparks[2])
    img = back.render()
    gold_lit(img, spiral, cx, cy)
    for sx, sy in sparks:
        img[sy, sx] = (*GOLD[2], 255)

    img = stamp_ball(img, BALL_C, 12)

    # open, slightly cupped palm under the ball (anime ep. 1): thumb up on the left, fingers up on the right
    front = Canvas()
    s = front.material(SKIN)
    rows = {23: [(8, 9), (22, 23)], 24: [(8, 9), (21, 23)], 25: [(8, 10), (20, 23)],
            26: [(8, 23)], 27: [(9, 22)], 28: [(10, 21)], 29: [(12, 19)], 30: [(13, 18)], 31: [(13, 18)]}
    for y, spans in rows.items():
        for a, b in spans:
            front.rect(s, a, y, b, y)
    fimg = front.render()
    for x, y in ((21, 25), (22, 24), (21, 26)):           # gap between the fingertips
        fimg[y, x] = (*SKIN[1], 255)
    for x in range(12, 20):                                # palm crease under the ball
        fimg[27, x] = (*SKIN[1], 255)
    return overlay(img, fimg)


JOBS = [('action/spin_ball_strike.png', strike), ('action/spin_ball_charge.png', charge)]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', required=True, help='textures/ folder')
    ap.add_argument('--preview')
    ap.add_argument('--compare', nargs='*', default=[], help='extra 32x32 icons shown next to the new ones')
    args = ap.parse_args()
    imgs = []
    for rel, fn in JOBS:
        res = fn()
        assert res.shape == (32, 32, 4)
        assert set(np.unique(res[..., 3])) <= {0, 255}, rel + ': semi-transparent pixels'
        im = Image.fromarray(res, 'RGBA')
        im.save(os.path.join(args.out, rel))
        imgs.append(im)
    if args.preview:
        imgs += [Image.open(p).convert('RGBA') for p in args.compare]
        s = 10
        sheet = Image.new('RGBA', (len(imgs) * (32 * s + 8) + 8 + len(imgs) * 40, 32 * s + 16), (58, 60, 64, 255))
        for i, im in enumerate(imgs):
            sheet.alpha_composite(im.resize((32 * s, 32 * s), Image.NEAREST), (8 + i * (32 * s + 8), 8))
            sheet.alpha_composite(im, (8 + len(imgs) * (32 * s + 8) + i * 40, 8))   # 1:1 hotbar size
        os.makedirs(os.path.dirname(os.path.abspath(args.preview)), exist_ok=True)
        sheet.save(args.preview)


if __name__ == '__main__':
    main()
