#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gyro's cloak textures (docs/art/gyros_cloak.md):
  textures/item/gyros_cloak.png     16x16  item icon: shoulder cape seen from the back, three tails
  textures/entity/gyros_cloak.png   64x32  Box UV for client/render/GyrosCloakModel

Reference: the official Steel Ball Run anime (2026) character sheet of Gyro Zeppeli (JoJo Wiki /
JoJo Fandom "Gyro anime.png"): a long sage/olive cape held by two green straps over the shoulders,
splitting into three tails with pointed ends, near-black inner side, copper-orange studs where the
straps meet the shirt. Colours below were sampled from that sheet (pipette, 6-step quantised).

Usage (project root):  python3 docs/art/tools/gen_gyros_cloak.py [--sheet] [--textures <dir>]
"""
import math, os, sys
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.dirname(os.path.dirname(os.path.dirname(HERE)))
TEX = os.path.join(PROJECT, 'src', 'main', 'resources', 'assets', 'rotp_spin', 'textures')
K = 8


def C(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


# -- palette (anime sheet) ------------------------------------------------------------------------
OL = C('14160a')                                     # ink outline
CLOTH = (C('96a866'), C('b4c078'), C('5a663c'))     # base, highlight, shadow
FOLD = C('7a8a50')                                   # soft fold between base and shadow
LINING = (C('4e5a36'), C('5a663c'), C('363c24'))    # inner side (dark olive)
STRAP = (C('8a9c58'), C('a2ae6c'), C('54603c'))     # shoulder straps, a step darker than the cloth
COPPER = (C('c8683a'), C('ea905a'), C('8a4424'))    # studs
CLEAR = (0, 0, 0, 0)


class Canvas:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.img = Image.new('RGBA', (w, h), CLEAR)

    def put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.img.putpixel((int(x), int(y)), c)

    def mask(self, pts):
        m = Image.new('L', (self.w * K, self.h * K), 0)
        ImageDraw.Draw(m).polygon([(x * K, y * K) for x, y in pts], fill=255)
        m = m.resize((self.w, self.h), Image.BOX).point(lambda v: 255 if v >= 128 else 0)
        px = m.load()
        return {(x, y) for y in range(self.h) for x in range(self.w) if px[x, y]}

    def part(self, cells, ramp, outline=True):
        base, hi, sh = ramp
        if outline:
            for (x, y) in cells:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if (x + dx, y + dy) not in cells:
                        self.put(x + dx, y + dy, OL)
        for (x, y) in cells:
            c = base
            if (x - 1, y) not in cells or (x, y - 1) not in cells:
                c = hi
            elif (x + 1, y) not in cells or (x, y + 1) not in cells:
                c = sh
            self.put(x, y, c)

    def save(self, root, rel):
        path = os.path.join(root, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.img.save(path)
        return path


# ------------------------------------------------------------------------------- item icon 16x16
def item_icon():
    cv = Canvas(16, 16)
    left = cv.mask([(3.2, 4), (6.6, 4), (5.4, 12), (1.6, 15.4), (1.2, 11)])
    mid = cv.mask([(7.1, 4), (8.9, 4), (9.6, 13.4), (8.0, 16.0), (6.4, 13.4)])
    right = cv.mask([(9.4, 4), (12.8, 4), (14.8, 11), (14.6, 15.4), (10.8, 12)])   # flipped by the wind
    cv.part(left, CLOTH)
    cv.part(mid, CLOTH)
    cv.part(right, LINING)
    for (x, y) in ((4, 7), (4, 8), (4, 9), (3, 11), (8, 8), (8, 9), (8, 10), (8, 11), (8, 12)):  # cloth folds
        if (x, y) in left or (x, y) in mid:
            cv.put(x, y, FOLD)
    for (x, y) in right:                                                             # outer cloth on the turned edge
        if (x + 1, y) not in right and y <= 12:
            cv.put(x, y, CLOTH[0])
    shoulders = cv.mask([(3.0, 5.0), (3.8, 2.2), (6.0, 1.0), (10.0, 1.0), (12.2, 2.2), (13.0, 5.0)])
    cv.part(shoulders, CLOTH)
    for x in range(7, 9):                                                            # neck notch
        cv.put(x, 1, OL)
    for x in range(4, 12):                                                           # hem shadow
        if (x, 4) in shoulders:
            cv.put(x, 4, CLOTH[2])
    for sx in (5, 10):                                                               # copper studs of the straps
        cv.put(sx, 2, COPPER[1]); cv.put(sx, 3, COPPER[2])
    return cv


# ------------------------------------------------------------------------------- entity 64x32, Box UV
def box_faces(u, v, w, h, d):
    """vanilla Box UV layout: name -> (x0, y0, width, height)"""
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h),
    }


def fill(cv, rect, fn):
    x0, y0, w, h = rect
    for j in range(h):
        for i in range(w):
            c = fn(i, j, w, h)
            if c is not None:
                cv.put(x0 + i, y0 + j, c)


def tail_profile(w, h):
    """(i, j) -> visible? Pointed end like the anime tails: the last rows narrow to the middle."""
    def vis(i, j):
        from_bottom = h - 1 - j
        if from_bottom == 0:
            return w % 2 == 1 and i == w // 2 or w % 2 == 0 and i in (w // 2 - 1, w // 2)
        if from_bottom == 1:
            return 0 < i < w - 1 or w <= 2
        return True
    return vis


def segment(cv, u, v, w, h):
    f = box_faces(u, v, w, h, 1)
    vis = tail_profile(w, h)
    mid = (w - 1) / 2.0

    def outer(i, j, W, H):                     # 'back' face, seen from behind the wearer
        if not vis(i, j):
            return None
        if j == 0:
            return STRAP[2]                    # seam under the shoulder piece
        if not vis(i - 1, j) and i > 0 or i == 0:
            return CLOTH[1]
        if i == W - 1 or not vis(i + 1, j):
            return CLOTH[2]
        if abs(i - mid) < 0.6 and 3 <= j < H - 3:
            return FOLD                        # central fold
        return CLOTH[0]

    def inner(i, j, W, H):                     # 'front' face, against the back
        if not vis(i, j):
            return None
        return LINING[2] if j == 0 or i in (0, W - 1) else LINING[0]

    def side(i, j, W, H):                      # 1 px edges follow the pointed profile
        return CLOTH[2] if vis(0, j) else None

    fill(cv, f['back'], outer)
    fill(cv, f['front'], inner)
    fill(cv, f['right'], side)
    fill(cv, f['left'], side)
    fill(cv, f['top'], lambda i, j, W, H: CLOTH[2])
    # bottom stays transparent: the tail ends in a point


def shoulders(cv):
    f = box_faces(0, 0, 9, 3, 5)

    def top(i, j, W, H):                       # over the shoulders; neck area in the middle is the strap
        if 3 <= i <= 5 and j <= 3:
            return CLOTH[2] if j == 3 else LINING[0]
        if i in (1, 7):
            return STRAP[0]
        return CLOTH[1] if j == 0 else CLOTH[0]

    def front(i, j, W, H):                     # chest: only the two straps and their copper studs
        if i in (1, 2, 6, 7):
            if j == H - 1 and i in (2, 6):
                return COPPER[0]
            if j == H - 1:
                return COPPER[2] if i == 1 else COPPER[1]
            return STRAP[1] if i in (1, 6) else STRAP[0]
        if i in (0, 8):
            return CLOTH[2]
        return None                            # open chest, the shirt shows through

    def back(i, j, W, H):
        if j == 0:
            return CLOTH[1]
        if j == H - 1:
            return CLOTH[2]
        return FOLD if i in (2, 6) else CLOTH[0]

    def side(i, j, W, H):
        return CLOTH[1] if j == 0 else (CLOTH[2] if j == H - 1 else CLOTH[0])

    fill(cv, f['top'], top)
    fill(cv, f['bottom'], lambda i, j, W, H: LINING[2] if j >= 3 else None)   # underside only at the back
    fill(cv, f['front'], front)
    fill(cv, f['back'], back)
    fill(cv, f['right'], side)
    fill(cv, f['left'], side)


def entity_texture():
    cv = Canvas(64, 32)
    shoulders(cv)
    segment(cv, 0, 8, 3, 11)    # left, the right one mirrors this area
    segment(cv, 8, 8, 4, 13)    # middle
    return cv


if __name__ == '__main__':
    root = TEX
    if '--textures' in sys.argv:
        root = sys.argv[sys.argv.index('--textures') + 1]
    made = []
    for rel, fn in (('item/gyros_cloak.png', item_icon), ('entity/gyros_cloak.png', entity_texture)):
        cv = fn()
        print('->', cv.save(root, rel), cv.img.size)
        made.append(cv)
    if '--sheet' in sys.argv:
        S = 10
        W = sum(c.w * S + 12 for c in made) + 6
        sheet = Image.new('RGBA', (W, 32 * S + 12), (70, 72, 82, 255))
        x = 6
        for c in made:
            sheet.alpha_composite(c.img.resize((c.w * S, c.h * S), Image.NEAREST), (x, 6))
            x += c.w * S + 12
        out = os.path.join(PROJECT, '.agent', 'preview')
        os.makedirs(out, exist_ok=True)
        sheet.convert('RGB').save(os.path.join(out, 'gyros_cloak.png'))
        print('sheet', sheet.size)
