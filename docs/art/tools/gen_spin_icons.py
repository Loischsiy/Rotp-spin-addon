#!/usr/bin/env python3
"""Перекраска стального шара на иконках Спина: серая сталь -> зелёный металл с шестиугольной пластиной
(как у `textures/item/steel_ball.png`, лор: docs/art/steel_ball.md, палитра там же).

Затрагивает 4 иконки, где нарисован Стальной шар:
  power/spin.png            (шар в «глазу» спирали, 5x5 — только перекраска тонов)
  action/spin_ball_throw.png
  action/spin_ball_steer.png
  action/spin_healing.png    (крест — светлый, с тёмной обводкой: зелёный крест на зелёном шаре не читался)
Остальное (золотые спирали/дуги/траектории, контуры) остаётся как в исходниках.

Исходники (серые версии) лежат в docs/art/tools/spin_icons_src/, поэтому скрипт можно запускать повторно.
Запуск из корня проекта:
  python3 docs/art/tools/gen_spin_icons.py \
      --out src/main/resources/assets/rotp_spin/textures [--preview .agent/preview/spin_icons.png]
Зависимости: python3, Pillow, numpy. Шар рисуется тем же кодом, что иконка предмета (gen_steel_ball.icon).
"""
import argparse
import os
import sys

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_steel_ball as sb  # noqa: E402

SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'spin_icons_src')


def rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


# серая сталь (старая палитра ТЗ) -> зелёный металл (ICON_PALETTE из gen_steel_ball)
STEEL_BASE, STEEL_SHADE, STEEL_LIGHT = rgb('8a949e'), rgb('4a5058'), rgb('d8dde2')
G_OUTLINE, G_DARK, G_MID, G_BASE, G_LIGHT, G_SPEC = (rgb(x) for x in
                                                     ('06240a', '0f5a12', '1c8a1c', '33b12a', '72d944', 'cdf99a'))
REMAP = {STEEL_BASE: G_BASE, STEEL_SHADE: G_MID, STEEL_LIGHT: G_LIGHT}
CROSS_FILL, CROSS_SHADE = rgb('f2ffe6'), rgb('b8e6a0')   # крест лечения (светлый на зелёном шаре)
CROSS_OLD = {rgb('6fcf5a'), rgb('3f8f35')}
OUTLINE = rgb('1e140b')


def load(name):
    return np.array(Image.open(os.path.join(SRC, name)).convert('RGBA'))


def remap(a):
    out = a.copy()
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            c = tuple(a[y, x, :3])
            if a[y, x, 3] and c in REMAP:
                out[y, x, :3] = REMAP[c]
    return out


def largest_component(mask):
    h, w = mask.shape
    seen = np.zeros_like(mask, dtype=bool)
    best = []
    for sy in range(h):
        for sx in range(w):
            if not mask[sy, sx] or seen[sy, sx]:
                continue
            comp, stack = [], [(sy, sx)]
            seen[sy, sx] = True
            while stack:
                y, x = stack.pop()
                comp.append((y, x))
                for dy in (-1, 0, 1):
                    for dx in (-1, 0, 1):
                        ny, nx = y + dy, x + dx
                        if 0 <= ny < h and 0 <= nx < w and mask[ny, nx] and not seen[ny, nx]:
                            seen[ny, nx] = True
                            stack.append((ny, nx))
            if len(comp) > len(best):
                best = comp
    out = np.zeros_like(mask, dtype=bool)
    for y, x in best:
        out[y, x] = True
    return out


def plain_features():
    """Отключить шестиугольник и прорези (для иконки с крестом сверху — иначе каша)."""
    orig = sb.feature_layers

    def none(p, center, ex, ey, off):
        L = orig(p, center, ex, ey, off)
        z = np.zeros(L['slit'].shape, dtype=bool)
        return dict(L, groove=z, frame=z, plate=z, slit=z, slit_rim=z)
    sb.feature_layers = none
    return orig


def stamp_ball(a, center, d, features=True):
    """Нарисовать зелёный шар диаметром d (без контура) с центром в center=(cx, cy); контур шара — в спрайте."""
    n = d + 4
    orig = None if features else plain_features()
    try:
        spr = np.array(sb.icon(size=n, radius=d / 2.0, ss=8))
    finally:
        if orig is not None:
            sb.feature_layers = orig
    ox = int(round(center[0] - (n - 1) / 2.0))
    oy = int(round(center[1] - (n - 1) / 2.0))
    out = a.copy()
    for sy in range(n):
        for sx in range(n):
            y, x = oy + sy, ox + sx
            if spr[sy, sx, 3] and 0 <= y < a.shape[0] and 0 <= x < a.shape[1]:
                out[y, x] = spr[sy, sx]
    return out


def steel_mask(a):
    m = np.zeros(a.shape[:2], dtype=bool)
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            if a[y, x, 3] and tuple(a[y, x, :3]) in REMAP:
                m[y, x] = True
    return m


def erase_ball(a, mask):
    """Убрать старый серый шар: его пиксели и прилегающий тёмный контур."""
    out = a.copy()
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                out[y, x] = (0, 0, 0, 0)
                continue
            if a[y, x, 3] and tuple(a[y, x, :3]) == OUTLINE and any(
                    0 <= y + dy < h and 0 <= x + dx < w and mask[y + dy, x + dx]
                    for dy in (-1, 0, 1) for dx in (-1, 0, 1)):
                out[y, x] = (0, 0, 0, 0)
    return out


def bbox_center(mask):
    ys, xs = np.where(mask)
    return (xs.min() + xs.max()) / 2.0, (ys.min() + ys.max()) / 2.0, xs.max() - xs.min() + 1, ys.max() - ys.min() + 1


def do_power():
    return remap(load('power_spin.png'))          # шар 5x5: только тона, признаки не помещаются


def redraw(name, d):
    a = load(name)
    m = largest_component(steel_mask(a))
    cx, cy, _, _ = bbox_center(m)
    return stamp_ball(erase_ball(remap(a), m), (cx, cy), d)


def do_throw():
    return redraw('spin_ball_throw.png', 14)


def do_steer():
    return redraw('spin_ball_steer.png', 11)


def do_healing():
    a = load('spin_healing.png')
    cross = np.zeros(a.shape[:2], dtype=bool)
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            if a[y, x, 3] and tuple(a[y, x, :3]) in CROSS_OLD:
                cross[y, x] = True
    disc = np.zeros(a.shape[:2], dtype=bool)
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            disc[y, x] = (x - 15.5) ** 2 + (y - 15.5) ** 2 <= 6.7 ** 2
    out = stamp_ball(erase_ball(remap(a), disc), (15.5, 15.5), 13, features=False)
    # крест: старая форма, светлая заливка + тёмная обводка по 4-соседям
    h, w = cross.shape
    for y in range(h):
        for x in range(w):
            if cross[y, x]:
                continue
            if any(0 <= y + dy < h and 0 <= x + dx < w and cross[y + dy, x + dx]
                   for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1))) and (x - 15.5) ** 2 + (y - 15.5) ** 2 <= 6.7 ** 2:
                out[y, x] = (*G_OUTLINE, 255)
    for y in range(h):
        for x in range(w):
            if cross[y, x]:
                out[y, x] = (*(CROSS_FILL if (x + y) % 5 else CROSS_FILL), 255)
    return out


JOBS = [('power/spin.png', do_power), ('action/spin_ball_throw.png', do_throw),
        ('action/spin_ball_steer.png', do_steer), ('action/spin_healing.png', do_healing)]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--out', required=True, help='папка textures/')
    ap.add_argument('--preview')
    args = ap.parse_args()
    imgs = []
    for rel, fn in JOBS:
        res = fn()
        assert res.shape == (32, 32, 4)
        assert set(np.unique(res[..., 3])) <= {0, 255}, rel + ': полупрозрачные пиксели'
        Image.fromarray(res, 'RGBA').save(os.path.join(args.out, rel))
        imgs.append(Image.fromarray(res, 'RGBA'))
    if args.preview:
        s = 10
        sheet = Image.new('RGBA', (len(imgs) * (32 * s + 8) + 8, 32 * s + 16), (58, 60, 64, 255))
        for i, im in enumerate(imgs):
            sheet.alpha_composite(im.resize((32 * s, 32 * s), Image.NEAREST), (8 + i * (32 * s + 8), 8))
        sheet.save(args.preview)


if __name__ == '__main__':
    main()
