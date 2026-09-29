#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Icons for Ball Breaker and the two mob effects (see docs/art/ball_breaker.md and wrecking_ball.md):
  textures/power/ball_breaker.png                 32x32  stand face + gold rim
  textures/action/ball_breaker_punch.png          32x32  lime fist, speed lines
  textures/action/ball_breaker_heavy_punch.png    32x32  fist + gold spiral
  textures/action/ball_breaker_block.png          32x32  crossed forearms
  textures/action/ball_breaker_senescence.png     32x32  palm with aging cracks on violet
  textures/mob_effect/senescence.png              18x18  cracked hourglass
  textures/mob_effect/hemispatial_neglect.png     18x18  half-erased head profile

Shapes are drawn as 8x supersampled masks, reduced to 32x32 with a hard threshold (no anti-aliasing),
then shaded per pixel (light from the top-left) and outlined with #1e140b, like the other action icons.

Usage (project root):  python3 docs/art/tools/gen_ball_breaker_icons.py [--sheet]
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


OL = C('1e140b')
LIME = (C('7ac74f'), C('b8e69a'), C('4a7a2e'))          # base, highlight, shadow
LIME_D = C('345c22')
DARK = (C('3f4a44'), C('6a7670'), C('232a27'))
GOLD = (C('b8860b'), C('e0b84a'), C('7a5a08'))
PINK = (C('e060a8'), C('f5a8d0'), C('96386c'))
VIOLET = (C('5b3f8c'), C('7b5cb0'), C('3e2a63'))
GLASS = (C('7a7a8a'), C('b8b8c8'), C('55556a'))
SAND = C('3a3a44')
CRACK_L = C('d8d8e8')
STEEL_PURPLE = (C('8a7bd8'), C('b3a8ee'), C('5a4f96'))
WHITE = C('ffffff')


class Icon:
    def __init__(self, n):
        self.n = n
        self.img = Image.new('RGBA', (n, n), (0, 0, 0, 0))

    # -- shape masks (pixel coordinates, floats allowed) ------------------------------------------
    def mask(self, fn):
        m = Image.new('L', (self.n * K, self.n * K), 0)
        d = ImageDraw.Draw(m)
        fn(d)
        m = m.resize((self.n, self.n), Image.BOX)
        return m.point(lambda v: 255 if v >= 128 else 0)

    def put(self, x, y, c):
        if 0 <= x < self.n and 0 <= y < self.n:
            self.img.putpixel((int(x), int(y)), c)

    def part(self, m, ramp, outline=True):
        """paint a mask with base/highlight/shadow; outline drawn first, around the part (over anything below)"""
        px = m.load()
        n = self.n

        def inside(x, y):
            return 0 <= x < n and 0 <= y < n and px[x, y] > 0

        if outline:
            for y in range(n):
                for x in range(n):
                    if not inside(x, y) and (inside(x - 1, y) or inside(x + 1, y) or inside(x, y - 1) or inside(x, y + 1)):
                        self.put(x, y, OL)
        base, hi, sh = ramp
        for y in range(n):
            for x in range(n):
                if not inside(x, y):
                    continue
                if not inside(x - 1, y) or not inside(x, y - 1):
                    c = hi
                elif not inside(x + 1, y) or not inside(x, y + 1):
                    c = sh
                else:
                    c = base
                self.put(x, y, c)

    def flat(self, m, color):
        px = m.load()
        for y in range(self.n):
            for x in range(self.n):
                if px[x, y] > 0:
                    self.put(x, y, color)

    def bulb(self, x, y, w=2, h=2, ramp=PINK):
        base, hi, sh = ramp
        for yy in range(h):
            for xx in range(w):
                if w >= 3 and h >= 3 and xx in (0, w - 1) and yy in (0, h - 1):
                    continue
                c = base
                if xx == 0 and yy == 0:
                    c = hi
                elif xx == w - 1 and yy == h - 1:
                    c = sh
                self.put(x + xx, y + yy, c)

    def save(self, rel):
        path = os.path.join(TEX, rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.img.save(path)
        return path


# -- drawing helpers (pixel units -> supersampled) --------------------------------------------------
def s(v):
    return v * K


def ell(cx, cy, rx, ry, deg=0):
    def f(d):
        pts = []
        a = math.radians(deg)
        for i in range(48):
            t = 2 * math.pi * i / 48
            x, y = rx * math.cos(t), ry * math.sin(t)
            pts.append((s(cx + x * math.cos(a) - y * math.sin(a)), s(cy + x * math.sin(a) + y * math.cos(a))))
        d.polygon(pts, fill=255)
    return f


def capsule(p0, p1, w):
    def f(d):
        d.line([(s(p0[0]), s(p0[1])), (s(p1[0]), s(p1[1]))], fill=255, width=int(s(w)))
        for p in (p0, p1):
            r = w / 2
            d.ellipse([s(p[0] - r), s(p[1] - r), s(p[0] + r), s(p[1] + r)], fill=255)
    return f


def rrect(x0, y0, x1, y1, r):
    def f(d):
        d.rounded_rectangle([s(x0), s(y0), s(x1), s(y1)], radius=s(r), fill=255)
    return f


def poly(pts):
    def f(d):
        d.polygon([(s(x), s(y)) for x, y in pts], fill=255)
    return f


def union(*fs):
    def f(d):
        for g in fs:
            g(d)
    return f


def spiral(cx, cy, r0, r1, turns, w):
    def f(d):
        pts = []
        steps = 160
        for i in range(steps + 1):
            t = i / steps
            a = 2 * math.pi * turns * t
            r = r0 + (r1 - r0) * t
            pts.append((s(cx + r * math.cos(a)), s(cy + r * math.sin(a))))
        d.line(pts, fill=255, width=int(s(w)), joint='curve')
        for p in (pts[0], pts[-1]):
            d.ellipse([p[0] - s(w / 2), p[1] - s(w / 2), p[0] + s(w / 2), p[1] + s(w / 2)], fill=255)
    return f


def arc(cx, cy, rx, ry, a0, a1, w):
    def f(d):
        pts = []
        for i in range(61):
            a = math.radians(a0 + (a1 - a0) * i / 60)
            pts.append((s(cx + rx * math.cos(a)), s(cy + ry * math.sin(a))))
        d.line(pts, fill=255, width=int(s(w)), joint='curve')
        for p in (pts[0], pts[-1]):
            d.ellipse([p[0] - s(w / 2), p[1] - s(w / 2), p[0] + s(w / 2), p[1] + s(w / 2)], fill=255)
    return f


# ------------------------------------------------------------------------------------- power icon (face)
def icon_power():
    ic = Icon(32)
    ic.part(ic.mask(arc(16, 16, 14, 12, 192, 348, 2.4)), GOLD)                  # rim (gold arc behind the head)
    ic.part(ic.mask(rrect(7, 26, 25, 31, 2)), DARK)                              # shoulders
    for sx, deg in ((7.5, -18), (24.5, 18)):                                     # ear discs
        ic.part(ic.mask(ell(sx, 9, 6, 2.6, deg)), LIME)
        ic.flat(ic.mask(ell(sx, 9.4, 3.6, 1.1, deg)), LIME[2])
    ic.part(ic.mask(ell(16, 18, 8.6, 10.2)), LIME)                               # head
    for (x, y) in ((11, 13), (17, 11), (21, 15), (12, 19), (18, 20), (15, 25)):  # bulbs in random order
        ic.bulb(x, y, 2, 2)
    ic.img.putpixel((15, 6), LIME[1])
    return ic


# ------------------------------------------------------------------------------------- fists
def fist(ic, dx=0, dy=0, scale=1.0, knuckles=True):
    def T(x, y):
        return (16 + (x - 16) * scale + dx, 17 + (y - 17) * scale + dy)
    body = ic.mask(rrect(*T(11, 13), *T(26, 26), 3 * scale))
    ic.part(body, LIME)
    for i, fx in enumerate((13.2, 16.6, 20.0, 23.4)):                            # four curled fingers
        cx, cy = T(fx, 12.6 + (0.6 if i in (0, 3) else 0))
        ic.part(ic.mask(ell(cx, cy, 1.9 * scale, 2.4 * scale)), LIME)
        if knuckles:
            ic.bulb(int(round(cx - 1)), int(round(cy - 1)), 2, 2)
    tx, ty = T(11.6, 22.4)                                                        # thumb across the fingers
    ic.part(ic.mask(ell(tx + 1.2, ty, 3.6 * scale, 2.1 * scale, -20)), LIME)
    for y in range(int(T(0, 19)[1]), int(T(0, 25)[1])):                          # wrist cuff hint
        pass


def icon_punch():
    ic = Icon(32)
    for (y, x0, x1) in ((11, 1, 9), (17, 0, 10), (23, 2, 8)):                    # speed lines
        for x in range(x0, x1):
            ic.put(x, y + 1, OL)
        for x in range(x0, x1):
            ic.put(x, y, LIME[1])
    fist(ic, dx=2, dy=1)
    return ic


def icon_heavy():
    ic = Icon(32)
    ic.part(ic.mask(spiral(16, 16, 3, 15, 2.1, 2.6)), GOLD)                      # golden spiral around the fist
    fist(ic, dx=1, dy=2, scale=0.82)
    return ic


# ------------------------------------------------------------------------------------- block (crossed forearms)
def forearm(ic, p0, p1, hand_at_p1=True):
    ic.part(ic.mask(capsule(p0, p1, 7.4)), LIME)
    # dark gauntlet band about a third of the way along the arm
    a = (p0[0] + (p1[0] - p0[0]) * 0.28, p0[1] + (p1[1] - p0[1]) * 0.28)
    b = (p0[0] + (p1[0] - p0[0]) * 0.40, p0[1] + (p1[1] - p0[1]) * 0.40)
    ic.part(ic.mask(capsule(a, b, 7.4)), DARK, outline=False)
    for t in (0.55, 0.70):
        cx, cy = p0[0] + (p1[0] - p0[0]) * t, p0[1] + (p1[1] - p0[1]) * t
        ic.bulb(int(round(cx - 1)), int(round(cy - 1)), 2, 2)
    ic.part(ic.mask(ell(p1[0], p1[1], 4.4, 4.0)), LIME)                          # fist at the end
    ic.bulb(int(round(p1[0] - 1)), int(round(p1[1] - 2)), 2, 2)


def icon_block():
    ic = Icon(32)
    forearm(ic, (5.5, 26.5), (23, 7.5))
    forearm(ic, (26.5, 26.5), (9, 7.5))
    return ic


# ------------------------------------------------------------------------------------- senescence action
def line_px(ic, pts, light=CRACK_L, dark=SAND):
    d = ImageDraw.Draw(ic.img)
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        d.line([(x0 + 1, y0), (x1 + 1, y1)], fill=dark)
        d.line([(x0, y0), (x1, y1)], fill=light)


def hourglass_mini(ic, ox, oy):
    """tiny hourglass 7x10 at (ox, oy)"""
    rows = [3, 3, 2, 1, 0, 0, 1, 2, 3, 3]  # half widths
    for i, hw in enumerate(rows):
        x0, x1 = ox + 3 - hw, ox + 3 + hw
        for x in range(x0 - 1, x1 + 2):
            for yy in (-1, 0, 1):
                pass
        for x in range(x0 - 1, x1 + 2):
            ic.put(x, oy + i, OL)
    for i, hw in enumerate(rows):
        for x in range(ox + 3 - hw, ox + 3 + hw + 1):
            c = GLASS[0]
            if x == ox + 3 - hw:
                c = GLASS[1]
            elif x == ox + 3 + hw:
                c = GLASS[2]
            if i >= 7:
                c = SAND if hw > 0 and x not in (ox + 3 - hw, ox + 3 + hw) else c
            ic.put(x, oy + i, c)
    for x in range(ox, ox + 7):
        ic.put(x, oy - 1, OL); ic.put(x, oy + 10, OL)


def icon_senescence():
    ic = Icon(32)
    ic.part(ic.mask(ell(16, 16, 14.6, 14.6)), VIOLET)                             # violet background disc
    # open hand, fingers up
    ic.part(ic.mask(capsule((9.6, 24), (6.4, 17.5), 3.4)), LIME)                  # thumb
    for (fx, top) in ((11.2, 11), (14.7, 7.5), (18.2, 8.5), (21.7, 12)):
        ic.part(ic.mask(capsule((fx, 20.5), (fx, top), 3.2)), LIME)
    ic.part(ic.mask(rrect(9.2, 19, 23.8, 27.5, 3.2)), LIME)                       # palm
    ic.bulb(15, 22, 3, 2)                                                        # bulb on the palm
    line_px(ic, [(12, 28), (14, 24), (12, 21), (15, 17), (14, 13), (16, 9)])       # aging cracks
    line_px(ic, [(20, 27), (19, 23), (21, 20), (19, 16)])
    hourglass_mini(ic, 23, 3)
    return ic


# ------------------------------------------------------------------------------------- effect icons (18x18)
def effect_senescence():
    ic = Icon(18)
    hw = [4.6, 4.2, 3.5, 2.6, 1.6, 0.9, 0.9, 1.6, 2.6, 3.5, 4.2, 4.6]
    rows = {}
    for i, h in enumerate(hw):
        y = 3 + i
        rows[y] = (int(math.ceil(8.5 - h)), int(math.floor(8.5 + h)))
    cap = (C('5e5e70'), C('8a8a9c'), C('3e3e4e'))
    # outline of the whole silhouette
    sil = set()
    for y, (x0, x1) in rows.items():
        sil.update((x, y) for x in range(x0, x1 + 1))
    for y in (1, 2, 15, 16):
        sil.update((x, y) for x in range(3, 15))
    for (x, y) in sil:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if (x + dx, y + dy) not in sil:
                ic.put(x + dx, y + dy, OL)
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1 + 1):
            c = GLASS[0]
            if x <= x0:
                c = GLASS[1]
            elif x >= x1:
                c = GLASS[2]
            ic.put(x, y, c)
    for y in (1, 2, 15, 16):
        for x in range(3, 15):
            c = cap[0]
            if y in (1, 15):
                c = cap[1]
            elif y in (2, 16):
                c = cap[2] if x > 3 else cap[0]
            ic.put(x, y, c)
    # sand: pile in the lower bulb, some in the upper funnel, a falling grain
    for y in (12, 13, 14):
        x0, x1 = rows[y]
        for x in range(x0 + 1, x1):
            ic.put(x, y, SAND)
    for x in range(rows[11][0] + 1, rows[11][1]):
        if abs(x - 8.5) < 1.6:
            ic.put(x, 11, SAND)
    for y in (5, 6, 7):
        x0, x1 = rows[y]
        for x in range(x0 + 1, x1):
            ic.put(x, y, SAND)
    ic.put(8, 8, SAND); ic.put(9, 9, SAND)
    # two cracks
    for (x, y) in ((6, 3), (7, 4), (6, 5), (7, 6), (8, 7)):
        ic.put(x, y, CRACK_L)
    for (x, y) in ((7, 3), (8, 4), (7, 5), (8, 6)):
        pass
    for (x, y) in ((11, 10), (10, 11), (11, 12), (10, 13), (12, 14)):
        ic.put(x, y, CRACK_L)
    for (x, y) in ((12, 10), (11, 11), (12, 12), (11, 13)):
        ic.put(x, y, OL)
    return ic


def effect_hemispatial():
    ic = Icon(18)
    x_cut = 8
    # head profile facing right: block head + snout; only the right half (x >= x_cut+1) survives
    sil = {(x, y) for x in range(x_cut + 1, 14) for y in range(3, 15)}
    sil |= {(x, y) for x in (14, 15) for y in range(8, 12)}
    for (x, y) in list(sil):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if (x + dx, y + dy) not in sil:
                ic.put(x + dx, y + dy, OL)
    base, hi, sh = STEEL_PURPLE
    for (x, y) in sil:
        c = base
        if (x - 1, y) not in sil or (x, y - 1) not in sil:
            c = hi
        elif (x + 1, y) not in sil or (x, y + 1) not in sil:
            c = sh
        ic.put(x, y, c)
    # eyes on the right side
    ic.put(11, 7, WHITE)
    ic.put(13, 7, WHITE)
    # the erased left half: only a few dissolving specks near the cut
    for (x, y, c) in ((7, 5, base), (6, 9, sh), (7, 12, base), (5, 7, hi), (6, 4, sh), (4, 11, sh)):
        ic.put(x, y, c)
    return ic


ICONS = {
    'power/ball_breaker.png': icon_power,
    'action/ball_breaker_punch.png': icon_punch,
    'action/ball_breaker_heavy_punch.png': icon_heavy,
    'action/ball_breaker_block.png': icon_block,
    'action/ball_breaker_senescence.png': icon_senescence,
    'mob_effect/senescence.png': effect_senescence,
    'mob_effect/hemispatial_neglect.png': effect_hemispatial,
}


if __name__ == '__main__':
    made = []
    for rel, fn in ICONS.items():
        ic = fn()
        ic.save(rel)
        made.append((rel, ic))
        print('icon ->', rel, ic.img.size)
    if '--sheet' in sys.argv:
        S = 10
        W = sum(ic.n * S + 12 for _, ic in made)
        sheet = Image.new('RGBA', (W, 32 * S + 12), (70, 72, 82, 255))
        x = 6
        for rel, ic in made:
            big = ic.img.resize((ic.n * S, ic.n * S), Image.NEAREST)
            sheet.alpha_composite(big, (x, 6))
            x += ic.n * S + 12
        out = os.path.join(PROJECT, '.agent', 'preview')
        os.makedirs(out, exist_ok=True)
        sheet.convert('RGB').save(os.path.join(out, 'icons.png'))
        print('sheet', sheet.size)
