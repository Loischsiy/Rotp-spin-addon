#!/usr/bin/env python3
"""Генератор Стального шара по референсам (docs/art/steel_ball.md):
зелёная металлическая сфера, шестиугольная пластина с рамкой и короткие изогнутые прорези вокруг неё.

Выдаёт три вещи из ОДНОЙ геометрии (никакого рисунка «на глаз»):
  --icon  PNG   иконка предмета 16x16                (textures/item/steel_ball.png)
  --wrap  PNG   обёртка для 3D-сферы 256x128 (equirect) (textures/entity/steel_ball_wrapped.png)
  --preview PNG контрольный рендер сферы (обёртка на шаре, два ракурса) — только для проверки глазами
Запуск из корня проекта:
  python3 docs/art/tools/gen_steel_ball.py \
    --icon src/main/resources/assets/rotp_spin/textures/item/steel_ball.png \
    --wrap src/main/resources/assets/rotp_spin/textures/entity/steel_ball_wrapped.png \
    --preview .agent/preview/steel_ball.png
Координаты сферы совпадают с client/render/SpinSphere.java: x=sinθ·cosφ, y=cosθ, z=sinθ·sinφ,
u=φ/2π, v=θ/π (v=0 — верхняя строка PNG). Центр шестиугольника — направление +Z (u=0.25, экватор).
"""
import argparse
import math

import numpy as np
from PIL import Image


def hexc(h):
    return np.array([int(h[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float64) / 255.0


# ---- палитра: зелёный металл (с референсов) --------------------------------------------------
G_DEEP = hexc('06300a')    # контур, дно прорезей
G_DARK = hexc('0f5a12')    # тень
G_MID = hexc('1f8a1c')     # основной тон в тени
G_BASE = hexc('33b12a')    # база
G_LIGHT = hexc('7fe04a')   # блик
G_SPEC = hexc('d5ff9a')    # зеркальный блик
RAMP = [G_DEEP, G_DARK, G_MID, G_BASE, G_LIGHT, G_SPEC]

# ---- геометрия --------------------------------------------------------------------------------
CENTER = np.array([0.0, 0.0, 1.0])       # центр шестиугольника
EX = np.array([1.0, 0.0, 0.0])           # «вправо» в касательной плоскости
EY = np.array([0.0, 1.0, 0.0])           # «вверх»
HEX_R = 0.52       # вписанный радиус шестиугольника, рад (~30°)
FRAME = 0.075      # ширина внешней рамки (рельеф)
GROOVE = 0.024     # ширина тёмных канавок вокруг рамки
SLIT_RING = 1.05   # угловое расстояние прорезей от центра, рад (~60°)
SLIT_HALF = 0.34   # половина длины прорези (по дуге), рад
SLIT_W = 0.048     # полуширина прорези, рад
SLITS_PER_HEX = 6
ICON = False       # режим иконки: признаки крупнее, без шума


def ramp(t):
    """t in [0,1] -> цвет по палитре (кусочно-линейно)."""
    t = np.clip(t, 0.0, 1.0) * (len(RAMP) - 1)
    i = np.clip(np.floor(t).astype(int), 0, len(RAMP) - 2)
    f = (t - i)[..., None]
    stack = np.stack(RAMP)
    return stack[i] * (1 - f) + stack[i + 1] * f


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0.0, 1.0)
    return t * t * (3 - 2 * t)


def hash3(ix, iy, iz):
    h = np.sin(ix * 127.1 + iy * 311.7 + iz * 74.7) * 43758.5453
    return h - np.floor(h)


def vnoise(p, freq):
    q = p * freq
    i = np.floor(q)
    f = q - i
    f = f * f * (3 - 2 * f)
    out = 0.0
    for dx in (0, 1):
        for dy in (0, 1):
            for dz in (0, 1):
                w = ((f[..., 0] if dx else 1 - f[..., 0]) * (f[..., 1] if dy else 1 - f[..., 1])
                     * (f[..., 2] if dz else 1 - f[..., 2]))
                out = out + w * hash3(i[..., 0] + dx, i[..., 1] + dy, i[..., 2] + dz)
    return out


def sd_hex_pointy(qx, qy, r):
    """Знаковое расстояние до правильного шестиугольника (вершина вверх), r — вписанный радиус."""
    x, y = np.abs(qy), np.abs(qx)          # поменяли оси: flat-top -> pointy-top
    kx, ky, kz = -0.8660254, 0.5, 0.57735027
    d = np.minimum(kx * x + ky * y, 0.0)
    x = x - 2.0 * d * kx
    y = y - 2.0 * d * ky
    x = x - np.clip(x, -kz * r, kz * r)
    y = y - r
    return np.hypot(x, y) * np.sign(y)


def local_frame(p, center, ex, ey):
    """Азимутальная равнопромежуточная проекция направления p вокруг center."""
    cos_a = np.clip(p @ center, -1.0, 1.0)
    alpha = np.arccos(cos_a)
    psi = np.arctan2(p @ ey, p @ ex)
    return alpha, psi


def feature_layers(p, center, ex, ey, azimuth_offset):
    """Один шестиугольник с прорезями. Возвращает слои: bevel (-1..1), plate, groove, slit, slit_wall."""
    alpha, psi = local_frame(p, center, ex, ey)
    qx, qy = alpha * np.cos(psi), alpha * np.sin(psi)
    near = alpha < 1.35

    s = sd_hex_pointy(qx, qy, HEX_R)
    eps = 0.004
    gx = (sd_hex_pointy(qx + eps, qy, HEX_R) - sd_hex_pointy(qx - eps, qy, HEX_R)) / (2 * eps)
    gy = (sd_hex_pointy(qx, qy + eps, HEX_R) - sd_hex_pointy(qx, qy - eps, HEX_R)) / (2 * eps)
    # рельеф: свет сверху-слева; рамка выпуклая, потому что кромка светлая сверху-слева
    facing = -(gx * -0.7071 + gy * 0.7071)            # >0: склон смотрит на свет

    groove_outer = near & (s > 0.0) & (s < GROOVE)
    frame = near & (s <= 0.0) & (s > -FRAME)
    inner = 0.0 if ICON else GROOVE           # в иконке внутренняя канавка не помещается
    groove_inner = near & (s <= -FRAME) & (s > -FRAME - inner)
    plate = near & (s <= -FRAME - inner)

    slit = np.zeros(alpha.shape, dtype=bool)
    slit_wall = np.zeros(alpha.shape, dtype=float)
    slit_rim = np.zeros(alpha.shape, dtype=bool)
    for k in range(SLITS_PER_HEX):
        psi_k = azimuth_offset + 2 * math.pi * k / SLITS_PER_HEX
        dpsi = (psi - psi_k + math.pi) % (2 * math.pi) - math.pi
        along = dpsi * math.sin(SLIT_RING)             # длина по дуге кольца
        radial = alpha - SLIT_RING
        d = np.hypot(np.maximum(np.abs(along) - SLIT_HALF, 0.0), radial) - SLIT_W
        inside = d < 0.0
        slit |= inside
        slit_rim |= (d >= 0.0) & (d < 0.02)
        # нижняя-правая стенка прорези светлее (выпуклый край), верхняя — тень
        slit_wall = np.where(inside, np.clip(radial / SLIT_W, -1, 1), slit_wall)
    return dict(facing=np.clip(facing, -1, 1), groove=(groove_outer | groove_inner), frame=frame, plate=plate,
                slit=slit, slit_rim=slit_rim, slit_wall=slit_wall)


def surface(p):
    """Цвет металла в точках p (N,3) на единичной сфере, освещение запечено только слегка."""
    n1 = vnoise(p, 5.0)
    n2 = vnoise(p + 11.3, 14.0)
    n3 = vnoise(p + 5.7, 34.0)
    # мягкая «отражённая среда»: большое светлое окно + узкий блик, как на референсе-рендере
    l1 = np.array([-0.40, 0.80, 0.45]); l1 /= np.linalg.norm(l1)
    l2 = np.array([-0.30, 0.55, 0.78]); l2 /= np.linalg.norm(l2)
    env = 0.26 * smoothstep(-0.2, 0.9, p @ l1) + 0.34 * np.exp(-(1 - p @ l2) * 16.0)
    t = 0.34 + 0.14 * (n1 - 0.5) + 0.10 * (n2 - 0.5) + env
    if ICON:                       # в 16x16 нет места для пятен и бликов: только форма и свет
        n1 = n2 = n3 = np.full_like(n1, 0.5)
        env = 0.0 * env
        t = 0.52 + 0.0 * n1
    # мелкие потёртости/пятна (как «испорченное» напыление на референсе)
    t = t - 0.16 * smoothstep(0.72, 0.85, n3) + 0.05 * smoothstep(0.70, 0.9, n2)
    col = ramp(t)

    # два шестиугольника: на +Z и на -Z, прорези второго повёрнуты на полшага
    for center, ex, ey, off in ((CENTER, EX, EY, math.pi / 6), (-CENTER, -EX, EY, 0.0)):
        L = feature_layers(p, center, ex, ey, off)
        # плита: чуть светлее и ровнее, без пятен
        col = np.where(L['plate'][..., None], ramp(0.50 + 0.08 * (n1 - 0.5) + 0.55 * env), col)
        # рамка: выпуклый бортик — светлый со стороны света, тёмный с противоположной
        col = np.where(L['frame'][..., None], ramp(0.50 + 0.34 * L['facing'] + 0.3 * env), col)
        col = np.where(L['groove'][..., None], G_DEEP * 0.9 + G_DARK * 0.1, col)
        col = np.where(L['slit_rim'][..., None], ramp(0.66 + 0.15 * env), col)
        wall = np.where(L['slit_wall'] > 0.15, 0.28, 0.06)[..., None]
        col = np.where(L['slit'][..., None], G_DEEP * (1 - wall) + G_MID * wall, col)
    return np.clip(col, 0.0, 1.0)


# ---- обёртка ----------------------------------------------------------------------------------
def wrap(width=256, height=128, ss=2):
    acc = np.zeros((height, width, 3))
    for sy in range(ss):
        for sx in range(ss):
            u = (np.arange(width) + (sx + 0.5) / ss) / width
            v = (np.arange(height) + (sy + 0.5) / ss) / height
            phi = 2 * math.pi * u[None, :]
            theta = math.pi * v[:, None]
            p = np.stack([np.sin(theta) * np.cos(phi), np.cos(theta) * np.ones_like(phi),
                          np.sin(theta) * np.sin(phi)], axis=-1)
            acc += surface(p)
    rgb = (acc / (ss * ss) * 255 + 0.5).astype(np.uint8)
    img = np.dstack([rgb, np.full((height, width), 255, np.uint8)])
    return Image.fromarray(img, 'RGBA')


# ---- иконка 16x16 -----------------------------------------------------------------------------
ICON_PALETTE = [hexc('06240a'), hexc('0f5a12'), hexc('1c8a1c'), hexc('33b12a'), hexc('72d944'), hexc('cdf99a')]


def rot_y(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def rot_x(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def icon_index(p, lam):
    """Индекс цвета иконки (0..5) для точки сферы p: плоские тона по свету, признаки — контрастно."""
    tone = 4 if lam > 0.75 else 3 if lam > 0.45 else 2 if lam > 0.15 else 1
    pp = p[None, :]
    for center, ex, ey, off in ((CENTER, EX, EY, math.pi / 6), (-CENTER, -EX, EY, 0.0)):
        L = feature_layers(pp, center, ex, ey, off)
        if L['slit'][0]:
            return 0
        if L['groove'][0]:
            return 0
        if L['frame'][0]:
            return min(5, tone + 1) if L['facing'][0] > 0 else max(1, tone - 1)
        if L['plate'][0]:
            return tone
    return 5 if lam > 0.93 else tone


def icon(size=16, radius=7.3, ss=8):
    global ICON, HEX_R, FRAME, GROOVE, SLIT_W, SLIT_HALF, SLIT_RING
    ICON, HEX_R, FRAME, GROOVE, SLIT_W, SLIT_HALF, SLIT_RING = True, 0.66, 0.15, 0.10, 0.075, 0.40, 1.18
    c = (size - 1) / 2.0
    light = np.array([-0.5, 0.6, 0.62]); light /= np.linalg.norm(light)
    # камера смотрит на точку левее и чуть ниже центра шестиугольника: он оказывается справа-выше
    view = rot_y(-math.radians(20)) @ rot_x(math.radians(-8))
    out = np.zeros((size, size, 4), dtype=np.uint8)
    for j in range(size):
        for i in range(size):
            votes = {}
            hits = 0
            for sy in range(ss):
                for sx in range(ss):
                    dx = (i - c + (sx + 0.5) / ss - 0.5) / radius
                    dy = (j - c + (sy + 0.5) / ss - 0.5) / radius
                    d2 = dx * dx + dy * dy
                    if d2 > 1.0:
                        continue
                    hits += 1
                    n_view = np.array([dx, -dy, math.sqrt(1 - d2)])
                    p = view @ n_view
                    lam = max(0.0, float(n_view @ light))
                    idx = icon_index(p, lam)
                    votes[idx] = votes.get(idx, 0) + 1
            if hits * 2 >= ss * ss:                     # пиксель по большей части внутри шара
                idx = max(votes, key=votes.get)
                out[j, i] = (*(ICON_PALETTE[idx] * 255).round().astype(int), 255)
    # контур: прозрачные пиксели рядом с шаром
    solid = out[..., 3] > 0
    for j in range(size):
        for i in range(size):
            if solid[j, i]:
                continue
            for di, dj in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                a, b = i + di, j + dj
                if 0 <= a < size and 0 <= b < size and solid[b, a] and (i - c) ** 2 + (j - c) ** 2 < (radius + 1.3) ** 2:
                    out[j, i] = (*(ICON_PALETTE[0] * 255).round().astype(int), 255)
                    break
    return Image.fromarray(out, 'RGBA')


# ---- контрольный рендер -----------------------------------------------------------------------
def preview(wrap_img, size=360):
    tex = np.asarray(wrap_img.convert('RGB')).astype(np.float64) / 255.0
    h, w, _ = tex.shape
    light = np.array([-0.5, 0.6, 0.62]); light /= np.linalg.norm(light)
    canvas = Image.new('RGB', (size * 2, size), (58, 60, 64))
    for k, (yaw, tilt) in enumerate(((28, -14), (-150, 20))):
        view = rot_y(-math.radians(yaw)) @ rot_x(math.radians(tilt))
        img = np.zeros((size, size, 3)) + np.array([58, 60, 64]) / 255.0
        ys, xs = np.mgrid[0:size, 0:size]
        dx = (xs - size / 2 + 0.5) / (size * 0.42)
        dy = (ys - size / 2 + 0.5) / (size * 0.42)
        d2 = dx * dx + dy * dy
        mask = d2 <= 1
        nv = np.stack([dx, -dy, np.sqrt(np.clip(1 - d2, 0, 1))], axis=-1)
        p = nv @ view.T
        theta = np.arccos(np.clip(p[..., 1], -1, 1))
        phi = np.arctan2(p[..., 2], p[..., 0]) % (2 * math.pi)
        tx = np.clip((phi / (2 * math.pi) * w).astype(int), 0, w - 1)
        ty = np.clip((theta / math.pi * h).astype(int), 0, h - 1)
        col = tex[ty, tx] * (0.7 + 0.45 * np.clip(nv @ light, 0, 1))[..., None]
        img[mask] = np.clip(col[mask], 0, 1)
        canvas.paste(Image.fromarray((img * 255).astype(np.uint8)), (k * size, 0))
    return canvas


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--icon')
    ap.add_argument('--wrap')
    ap.add_argument('--preview')
    args = ap.parse_args()
    wrap_img = wrap()
    if args.wrap:
        wrap_img.save(args.wrap)
    if args.icon:
        icon().save(args.icon)
    if args.preview:
        preview(wrap_img).save(args.preview)


if __name__ == '__main__':
    main()
