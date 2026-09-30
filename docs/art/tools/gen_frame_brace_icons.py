#!/usr/bin/env python3
"""Action icons for Golden Rectangle and Spin Body Brace (32x32, flat pixel art).

TZ: docs/art/action_spin_golden_frame.md, docs/art/action_spin_body_brace.md (palettes are copied from there).
  action/spin_golden_frame.png  two hands framing a 21x13 golden rectangle with a 1 px Fibonacci spiral
  action/spin_body_brace.png    torso wrapped by two green spin arcs, flattened bullet + 3 golden rays

Shapes are drawn as material masks; shading is automatic (light from top-left: highlight on the
top/left edge of a material, shadow on the bottom/right edge) and every transparent pixel
4-adjacent to the sprite becomes the #1e140b outline. No semi-transparent pixels.

Run from the project root:
  python3 docs/art/tools/gen_frame_brace_icons.py \
      --out src/main/resources/assets/rotp_spin/textures [--preview .agent/preview/frame_brace.png]
Dependencies: python3, Pillow, numpy.
"""
import argparse
import math
import os

import numpy as np
from PIL import Image

N = 32


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


OUTLINE = rgb('1e140b')
# material -> (base, shadow, highlight[, deep shadow])
SKIN = (rgb('e0ac7e'), rgb('b07850'), rgb('f4cfa8'), rgb('7a4e30'))
GLOVE = (rgb('3a3a52'), rgb('24243a'), rgb('5a5a78'))
GOLD = (rgb('d4a017'), rgb('8a6a0e'), rgb('f2d36b'))
SPIN_GREEN = (rgb('3f8f4a'), rgb('25592d'), rgb('7fd18a'))     # steel ball colour (body brace TZ)
BULLET = (rgb('8c8c96'), rgb('55555e'), rgb('c8c8d2'))


class Canvas:
    def __init__(self):
        self.mat = np.full((N, N), -1, dtype=int)     # material id per pixel, -1 = empty
        self.fixed = {}                                # (x, y) -> explicit colour (no auto shading)
        self.mats = []
        self.vs_empty = []                             # True: shade only against empty pixels

    def material(self, pal, vs_empty=False):
        self.mats.append(pal)
        self.vs_empty.append(vs_empty)
        return len(self.mats) - 1

    def put(self, m, x, y):
        if 0 <= x < N and 0 <= y < N:
            self.mat[y, x] = m
            self.fixed.pop((x, y), None)

    def rect(self, m, x0, y0, x1, y1):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.put(m, x, y)

    def color(self, x, y, c):
        """Explicit colour on an already filled pixel (details: knuckle lines, spiral)."""
        self.fixed[(x, y)] = c

    def render(self):
        out = np.zeros((N, N, 4), dtype=np.uint8)
        filled = self.mat >= 0

        def same(x, y, m):
            if not (0 <= x < N and 0 <= y < N):
                return False
            return self.mat[y, x] >= 0 if self.vs_empty[m] else self.mat[y, x] == m

        for y in range(N):
            for x in range(N):
                m = self.mat[y, x]
                if m < 0:
                    continue
                base, shadow, light = self.mats[m][:3]
                c = base
                if not same(x + 1, y, m) or not same(x, y + 1, m):
                    c = shadow
                if not same(x - 1, y, m) or not same(x, y - 1, m):
                    c = light if c is base else base   # a 1 px sliver gets base, not a stripe of both
                out[y, x] = (*self.fixed.get((x, y), c), 255)
        for y in range(N):
            for x in range(N):
                if filled[y, x]:
                    continue
                if any(0 <= x + dx < N and 0 <= y + dy < N and filled[y + dy, x + dx]
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    out[y, x] = (*OUTLINE, 255)
        return out


def arc_pixels(cx, cy, r, a0, a1, ox, oy, w, h, inset=1.5):
    """Pixels of a circle arc given in golden-rectangle space (u right, v down, 21 x 13), squeezed inside
    the 1 px border so no turn of the spiral merges with it."""
    pts = []
    steps = max(16, int(abs(a1 - a0) * r * 6))
    su, sv = (w - 2 * inset) / w, (h - 2 * inset) / h
    for i in range(steps + 1):
        a = a0 + (a1 - a0) * i / steps
        u = inset + (cx + r * math.cos(a)) * su
        v = inset + (cy + r * math.sin(a)) * sv
        px = ox + min(w - 2, max(1, int(math.floor(u))))
        py = oy + min(h - 2, max(1, int(math.floor(v))))
        if not pts or pts[-1] != (px, py):
            pts.append((px, py))
    return pts


# ---------------------------------------------------------------- Golden Rectangle
RX, RY, RW, RH = 5, 9, 21, 13          # golden rectangle 21 x 13 (13 * 1.618 = 21.03)


def left_hand(c, skin, glove):
    """Bottom-left hand: index finger up along the left edge, thumb right along the bottom edge."""
    c.rect(skin, 2, 12, 3, 18)          # index finger (skin phalanges)
    c.rect(glove, 2, 19, 3, 21)         # fingerless glove on the lower phalanx
    c.rect(skin, 8, 23, 14, 24)         # thumb
    c.rect(glove, 5, 23, 7, 24)
    c.rect(glove, 1, 21, 6, 27)         # back of the hand (glove)
    c.rect(glove, 7, 25, 8, 26)
    c.rect(skin, 0, 27, 4, 30)          # wrist leaving the icon
    c.put(-1, 1, 21)                    # round the knuckle corner
    c.put(-1, 6, 27)


def golden_frame():
    c = Canvas()
    skin = c.material(SKIN)
    glove = c.material(GLOVE)
    gold = c.material(GOLD)
    inner = c.material((OUTLINE, OUTLINE, OUTLINE))

    # rectangle: gold border, dark field so the spiral reads on any hotbar background
    c.rect(gold, RX, RY, RX + RW - 1, RY + RH - 1)
    c.rect(inner, RX + 1, RY + 1, RX + RW - 2, RY + RH - 2)

    # hands: the right one is the left one turned 180 degrees about the rectangle centre
    tmp = Canvas()
    ts, tg = tmp.material(SKIN), tmp.material(GLOVE)
    left_hand(tmp, ts, tg)
    cx2, cy2 = 2 * RX + RW - 1, 2 * RY + RH - 1          # x' = cx2 - x, y' = cy2 - y
    for y in range(N):
        for x in range(N):
            m = tmp.mat[y, x]
            if m < 0:
                continue
            mm = skin if m == ts else glove
            c.put(mm, x, y)
            c.put(mm, cx2 - x, cy2 - y)

    # Fibonacci spiral (squares 13, 8, 5, 3, 2, 1), outer end in the bottom-right corner
    arcs = [((8, 13), 13, 0.0, -math.pi / 2),        # (21,13) -> (8,0)
            ((8, 8), 8, -math.pi / 2, -math.pi),     # (8,0)  -> (0,8)
            ((5, 8), 5, math.pi, math.pi / 2),       # (0,8)  -> (5,13)
            ((5, 10), 3, math.pi / 2, 0.0),          # (5,13) -> (8,10)
            ((6, 10), 2, 0.0, -math.pi / 2),         # (8,10) -> (6,8)
            ((6, 9), 1, -math.pi / 2, -math.pi)]     # (6,8)  -> (5,9)
    spiral = []
    for (acx, acy), r, a0, a1 in arcs:
        for p in arc_pixels(acx, acy, r, a0, a1, RX, RY, RW, RH):
            if p not in spiral:
                spiral.append(p)
    img = c.render()
    # gold border: lit top/left, shaded bottom/right, base on the two off-light corners
    x1, y1 = RX + RW - 1, RY + RH - 1
    for x in range(RX, x1 + 1):
        img[RY, x] = (*GOLD[2], 255)
        img[y1, x] = (*GOLD[1], 255)
    for y in range(RY, y1 + 1):
        img[y, RX] = (*GOLD[2], 255)
        img[y, x1] = (*GOLD[1], 255)
    img[RY, x1] = (*GOLD[0], 255)
    img[y1, RX] = (*GOLD[0], 255)
    # deep skin shadow where each thumb meets the glove (reads as a knuckle crease)
    for x, y in ((8, 24), (cx2 - 8, cy2 - 24)):
        img[y, x] = (*SKIN[3], 255)
    for i, (x, y) in enumerate(spiral):
        on_border = x in (RX, RX + RW - 1) or y in (RY, RY + RH - 1)
        if on_border:
            continue                                   # the border itself is the tangent there
        col = GOLD[2] if (x - RX) + (y - RY) < 12 else GOLD[0]   # lit part top-left
        img[y, x] = (*col, 255)
    return img


# ---------------------------------------------------------------- Spin Body Brace
def torso_mask(c, skin):
    rows = {                     # y: (x_from, x_to) — shoulders and chest, turned slightly to the right
        7: (13, 16), 8: (13, 16),
        9: (8, 20), 10: (6, 21), 11: (5, 21), 12: (5, 21),
        13: (6, 20), 14: (6, 20), 15: (7, 20), 16: (7, 20), 17: (7, 20), 18: (7, 20),
        19: (8, 20), 20: (8, 19), 21: (8, 19), 22: (8, 19), 23: (9, 19), 24: (9, 19),
        25: (9, 19), 26: (9, 18), 27: (10, 18), 28: (10, 18),
    }
    for y, (a, b) in rows.items():
        c.rect(skin, a, y, b, y)


def ellipse_ring(c, m, cx, cy, rx, ry, torso, front_only_on_torso=True, gap=None):
    """2 px thick ellipse; the back half is hidden where it passes behind the torso."""
    pts = set()
    for i in range(720):
        a = 2 * math.pi * i / 720
        if gap and gap[0] <= a <= gap[1]:
            continue
        for t in (0.0, 1.0):
            x = int(round(cx + (rx - t * 0.0) * math.cos(a)))
            y = int(round(cy + ry * math.sin(a) + t))
            back = math.sin(a) < 0
            if back and torso[y, x]:
                continue
            pts.add((x, y))
    for x, y in pts:
        c.put(m, x, y)


def body_brace():
    c = Canvas()
    skin = c.material(SKIN, vs_empty=True)     # arcs lie on the skin, they must not re-light it
    green = c.material(SPIN_GREEN)
    bullet = c.material(BULLET)
    gold = c.material(GOLD)

    torso_mask(c, skin)
    torso = c.mat == skin
    # two clockwise spin arcs (viewed from above: the front half runs right -> left)
    gap_upper = (math.pi * 0.55, math.pi * 0.95)          # opening at front-left, the arrow sits there
    ellipse_ring(c, green, 13, 12, 11, 3, torso, gap=gap_upper)
    ellipse_ring(c, green, 13.5, 22, 10, 3, torso, gap=(math.pi * 0.55, math.pi * 0.95))
    # arrowheads pointing left at the front end of each arc (a = 0.55 pi)
    for ax, ay in ((10, 15), (11, 25)):
        c.rect(green, ax, ay - 1, ax, ay + 2)
        c.rect(green, ax - 1, ay, ax - 1, ay + 1)

    # flattened bullet against the right side of the chest + three golden vibration rays
    c.rect(bullet, 21, 17, 23, 18)
    for x, y in ((25, 16), (26, 15), (27, 14),
                 (25, 18), (26, 18), (27, 18), (28, 18),
                 (25, 20), (26, 21), (27, 22)):
        c.put(gold, x, y)

    img = c.render()
    # details after shading
    pec = SKIN[1]
    for x, y in ((9, 17), (10, 18), (11, 18), (12, 17), (14, 17), (15, 18), (16, 18), (17, 17)):
        if (img[y, x, :3] == SKIN[0]).all():
            img[y, x] = (*pec, 255)                  # pectoral line
    for x, y in ((13, 19), (13, 21), (13, 23), (13, 25)):
        if (img[y, x, :3] == SKIN[0]).all():
            img[y, x] = (*pec, 255)                  # sternum / abs line
    # bullet: light nose, dark flattened tail against the skin
    img[17, 23] = (*BULLET[2], 255)
    img[17, 21] = (*BULLET[1], 255)
    img[18, 21] = (*BULLET[1], 255)
    img[18, 22] = (*BULLET[0], 255)
    img[18, 23] = (*BULLET[0], 255)
    # ray tips glint
    for x, y in ((27, 14), (28, 18), (27, 22)):
        img[y, x] = (*GOLD[2], 255)
    return img


JOBS = [('action/spin_golden_frame.png', golden_frame), ('action/spin_body_brace.png', body_brace)]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', required=True, help='textures/ folder')
    ap.add_argument('--preview')
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
        s = 10
        sheet = Image.new('RGBA', (len(imgs) * (32 * s + 8) + 8 + 2 * (32 + 8), 32 * s + 16), (58, 60, 64, 255))
        for i, im in enumerate(imgs):
            sheet.alpha_composite(im.resize((32 * s, 32 * s), Image.NEAREST), (8 + i * (32 * s + 8), 8))
            sheet.alpha_composite(im, (8 + len(imgs) * (32 * s + 8) + i * 40, 8))   # 1:1 hotbar size
        os.makedirs(os.path.dirname(os.path.abspath(args.preview)), exist_ok=True)
        sheet.save(args.preview)


if __name__ == '__main__':
    main()
