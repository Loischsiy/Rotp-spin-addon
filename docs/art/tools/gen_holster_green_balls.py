#!/usr/bin/env python3
"""Gyro's holster: grey steel balls -> green steel balls, as in the lore (docs/art/steel_ball.md).

Touches only the ball pixels; leather, brass and the model geometry stay as they are.
  item/gyros_holster.png    (32x32) the two half balls in the holster cups are re-lit as green spheres
  entity/gyros_holster.png  (64x64) Box UV of ball_right (32, 8) and ball_left (40, 8), 2x2x2 each,
                            plus the palette swatch strip at the bottom of the sheet
Palette = ICON_PALETTE of gen_steel_ball.py (same green as the steel ball item and the Spin icons).
Idempotent: grey and green ball pixels are both recognised, so the script can be run again.

Run from the project root:
  python3 docs/art/tools/gen_holster_green_balls.py --textures src/main/resources/assets/rotp_spin/textures
Dependencies: python3, Pillow, numpy.
"""
import argparse
import math
import os

import numpy as np
from PIL import Image


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


G_OUTLINE, G_DARK, G_MID, G_BASE, G_LIGHT, G_SPEC = (rgb(x) for x in
                                                     ('06240a', '0f5a12', '1c8a1c', '33b12a', '72d944', 'cdf99a'))
GREEN = {G_OUTLINE, G_DARK, G_MID, G_BASE, G_LIGHT, G_SPEC}
STEEL_BASE, STEEL_SHADE, STEEL_LIGHT = rgb('8a949e'), rgb('4a5058'), rgb('d8dde2')
STEEL = {STEEL_BASE, STEEL_SHADE, STEEL_LIGHT}
REMAP = {STEEL_BASE: G_BASE, STEEL_SHADE: G_MID, STEEL_LIGHT: G_LIGHT}   # swatches / stray pixels
LIGHT = (-0.55, -0.65, 0.52)                                             # top-left, towards the viewer


def components(mask):
    h, w = mask.shape
    seen = np.zeros_like(mask)
    out = []
    for sy in range(h):
        for sx in range(w):
            if mask[sy, sx] and not seen[sy, sx]:
                comp, stack = [], [(sy, sx)]
                seen[sy, sx] = True
                while stack:
                    y, x = stack.pop()
                    comp.append((y, x))
                    for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        ny, nx = y + dy, x + dx
                        if 0 <= ny < h and 0 <= nx < w and mask[ny, nx] and not seen[ny, nx]:
                            seen[ny, nx] = True
                            stack.append((ny, nx))
                out.append(comp)
    return out


def item_icon(a):
    """Each half ball (top half above the cup lip) is re-lit as a sphere whose centre sits on the lip."""
    mask = np.zeros(a.shape[:2], dtype=bool)
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            if a[y, x, 3] and tuple(a[y, x, :3]) in STEEL | GREEN:
                mask[y, x] = True
    out = a.copy()
    for comp in components(mask):
        if len(comp) < 6:
            continue
        ys = [p[0] for p in comp]
        xs = [p[1] for p in comp]
        cx = (min(xs) + max(xs) + 1) / 2.0
        r = (max(xs) - min(xs) + 1) / 2.0 + 0.3
        cy = min(ys) + r - 0.3                 # the lower part of the ball is hidden by the cup lip
        best, spec = -9.0, None
        for y, x in comp:
            nx, ny = (x + 0.5 - cx) / r, (y + 0.5 - cy) / r
            k = math.hypot(nx, ny)
            if k > 0.98:
                nx, ny = nx * 0.98 / k, ny * 0.98 / k
            nz = math.sqrt(max(0.0, 1.0 - nx * nx - ny * ny))
            d = nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2]
            edge = nx * nx + ny * ny > 0.72
            if d > 0.80:
                c = G_LIGHT
            elif d > 0.45:
                c = G_BASE
            elif d > 0.15:
                c = G_MID
            else:
                c = G_DARK
            if edge and c in (G_BASE, G_LIGHT) and (nx > 0 or ny > 0.2):
                c = G_MID                      # rim away from the light
            out[y, x] = (*c, 255)
            if d > best:
                best, spec = d, (y, x)
        out[spec[0], spec[1]] = (*G_SPEC, 255)  # one mirror glint top-left
    return out


def box_uv(u, v, w, h, d):
    """Vanilla ModelRenderer Box UV faces."""
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'east': (u, v + d, d, h), 'north': (u + d, v + d, w, h),
        'west': (u + d + w, v + d, d, h), 'south': (u + 2 * d + w, v + d, w, h),
    }


BALL_FACES = {                                  # 2x2 per face, rows top -> bottom
    'top': ((G_LIGHT, G_SPEC), (G_BASE, G_LIGHT)),
    'bottom': ((G_MID, G_DARK), (G_DARK, G_DARK)),
    'north': ((G_LIGHT, G_BASE), (G_BASE, G_MID)),    # front: faces the viewer, brightest side
    'east': ((G_BASE, G_BASE), (G_MID, G_DARK)),
    'west': ((G_BASE, G_LIGHT), (G_MID, G_MID)),
    'south': ((G_BASE, G_MID), (G_MID, G_DARK)),
}
BALLS = [(32, 8), (40, 8)]                      # texOffs of ball_right / ball_left in GyrosHolsterModel


def entity_texture(a):
    out = a.copy()
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            c = tuple(a[y, x, :3])
            if a[y, x, 3] and c in REMAP:
                out[y, x, :3] = REMAP[c]
    for u, v in BALLS:
        for face, (fu, fv, fw, fh) in box_uv(u, v, 2, 2, 2).items():
            for j in range(fh):
                for i in range(fw):
                    out[fv + j, fu + i] = (*BALL_FACES[face][j][i], 255)
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--textures', required=True, help='assets/rotp_spin/textures folder')
    args = ap.parse_args()
    for rel, fn, size in (('item/gyros_holster.png', item_icon, 32), ('entity/gyros_holster.png', entity_texture, 64)):
        path = os.path.join(args.textures, rel)
        a = np.array(Image.open(path).convert('RGBA'))
        res = fn(a)
        assert res.shape == (size, size, 4)
        assert set(np.unique(res[..., 3])) <= {0, 255}, rel + ': semi-transparent pixels'
        left = {tuple(p[:3]) for p in res.reshape(-1, 4) if p[3]} & STEEL
        assert not left, rel + ': grey steel left: %s' % left
        Image.fromarray(res, 'RGBA').save(path)


if __name__ == '__main__':
    main()
