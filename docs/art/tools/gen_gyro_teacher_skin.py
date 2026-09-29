#!/usr/bin/env python3
"""Генератор скина Gyro Zeppeli (аниме-наряд, 2019 Steel Ball Run / Part 7) 64x64.
Запуск: python3 gen_gyro_teacher_skin.py <выходной.png>
Правки цветов/пикселей вносим здесь и перегенерируем. Раскладка — классический скин (руки 4 px)."""
import sys
from PIL import Image

def C(h, a=255):
    h = h.lstrip('#'); return (int(h[0:2],16), int(h[2:4],16), int(h[4:6],16), a)

# --- палитра (см. docs/art/gyro_teacher.md) ---
HAT, HAT_L, HAT_D = C('a0693a'), C('bd8650'), C('6b4426')
GOG, GOG_L, GOG_D = C('9aa1aa'), C('d3d8de'), C('4a4e56')
HAIR, HAIR_L, HAIR_D = C('e6cf86'), C('f5e6a8'), C('b39a52')
SKIN, SKIN_D, SKIN_L = C('dba677'), C('b57f56'), C('ecc094')
LIP, LIP_D = C('86c883'), C('5c9c5c')
GOLD = C('e0b84a')
EYE, EYE_W, BROW = C('5aa6d2'), C('eef2f0'), C('8a6a3a')
PUR, PUR_D, PUR_L = C('5b3f8c'), C('3e2a63'), C('7b5cb0')
BTN, BTN_L = C('c8683a'), C('ec9560')
ORG, DARK = C('d9803a'), C('2a2226')
BLUE, BLUE_D = C('4a6cc0'), C('33499a')
CAPE, CAPE_D, CAPE_L = C('7a9a4a'), C('52702f'), C('9ab866')
PANT, PANT_D, PANT_L = C('a8763a'), C('7d5326'), C('c48f4c')
BELT, BELT_D = C('c08a3e'), C('8a5e24')
BUCK, BUCK_D, BALL = C('c4c8d0'), C('7d848c'), C('5fa060')
BOOT, BOOT_D = C('6b4a26'), C('3a2814')
STEEL, STEEL_D = C('8a9098'), C('5a5f68')
CLEAR = (0,0,0,0)

img = Image.new('RGBA', (64,64), CLEAR)
px = img.load()

class Face:
    def __init__(s, x, y, w, h): s.x, s.y, s.w, s.h = x, y, w, h
    def put(s, c, r, col):
        if 0 <= c < s.w and 0 <= r < s.h: px[s.x+c, s.y+r] = col
    def fill(s, col):
        for r in range(s.h):
            for c in range(s.w): s.put(c, r, col)
    def rows(s, r0, r1, col, c0=0, c1=None):
        c1 = s.w-1 if c1 is None else c1
        for r in range(r0, r1+1):
            for c in range(c0, c1+1): s.put(c, r, col)
    def cols(s, c0, c1, r0, r1, col): s.rows(r0, r1, col, c0, c1)

def box(ox, oy, w, h, d):
    """Классическая развёртка. Возвращает грани: top,bottom,right,front,left,back."""
    return dict(top=Face(ox+d, oy, w, d), bottom=Face(ox+d+w, oy, w, d),
                right=Face(ox, oy+d, d, h), front=Face(ox+d, oy+d, w, h),
                left=Face(ox+d+w, oy+d, d, h), back=Face(ox+d+w+d, oy+d, w, h))

# ===================== ГОЛОВА (0,0) =====================
head = box(0, 0, 8, 8, 8)
head['top'].fill(HAIR); head['bottom'].fill(SKIN_D)
for k in ('right','left','back'): head[k].fill(HAIR)
for k in ('right','left'):                       # лицо сбоку: кожа спереди, ухо, баки
    f = head[k]; front_c = (7,6,5) if k=='right' else (0,1,2)
    for c in front_c:
        f.rows(3, 7, SKIN)
    ear = 4 if k=='right' else 3
    f.put(ear, 4, SKIN_D); f.put(ear, 5, GOLD)   # ухо + золотая серьга/манжета
    f.rows(3, 3, HAIR_D, 0 if k=='right' else 5, 4 if k=='right' else 7)
    f.rows(6, 7, HAIR_D, 0 if k=='right' else 6, 1 if k=='right' else 7) if False else None
head['back'].rows(5, 7, HAIR_D, 0, 7)
for c in (1,3,4,6): head['back'].put(c, 6, HAIR)
for c in (2,5): head['back'].put(c, 7, HAIR_L)
# лицо (front): шляпа перекрывает ряды 0–2
f = head['front']
f.fill(SKIN)
f.rows(0, 2, HAIR_D)
f.rows(2, 2, SKIN_D, 1, 6)                     # тень от полей
for r in range(2, 7):
    f.put(0, r, HAIR); f.put(7, r, HAIR)
f.put(0, 6, HAIR_D); f.put(7, 6, HAIR_D)
f.put(1, 3, BROW); f.put(2, 3, BROW); f.put(5, 3, BROW); f.put(6, 3, BROW)
f.put(1, 4, EYE_W); f.put(2, 4, EYE); f.put(5, 4, EYE); f.put(6, 4, EYE_W)
f.put(3, 5, SKIN_L); f.put(4, 5, SKIN_D)      # нос
f.put(2, 6, LIP); f.put(3, 6, GOLD); f.put(4, 6, GOLD); f.put(5, 6, LIP)   # зелёные губы + золотые грилзы
f.put(3, 7, LIP_D); f.put(4, 7, LIP_D)        # нижняя губа
f.put(1, 7, GOLD); f.put(6, 7, GOLD)          # золотая застёжка у челюсти
f.put(2, 7, SKIN_D); f.put(5, 7, SKIN_D)

# ---- шляпа (слой головы, +32 по X) ----
hat = box(32, 0, 8, 8, 8)
hat['top'].fill(HAT)
hat['top'].rows(0, 0, HAT_L); hat['top'].rows(7, 7, HAT_D)
hat['top'].rows(2, 2, HAT_D, 1, 6); hat['top'].rows(5, 5, HAT_D, 1, 6)   # прорези на тулье
for k in ('right','left','back'):
    hat[k].rows(0, 1, HAT); hat[k].rows(2, 2, HAT_D)
    hat[k].rows(0, 0, HAT_L)
hat['right'].put(3, 0, GOG_D); hat['right'].put(4, 0, GOG_D)   # ремешок очков
hat['left'].put(3, 0, GOG_D); hat['left'].put(4, 0, GOG_D)
hat['back'].rows(1, 1, GOG_D, 0, 7) if False else None
fh = hat['front']
fh.rows(0, 1, HAT); fh.rows(0, 0, HAT_L); fh.rows(2, 2, HAT_D)
fh.cols(3, 4, 0, 1, GOG_D)                                       # ремень очков
for c0 in (1, 5):                                                # два «щелевых» стекла
    fh.cols(c0, c0+1, 0, 1, GOG); fh.put(c0, 0, GOG_L); fh.put(c0+1, 0, GOG_L)
    fh.cols(c0, c0+1, 1, 1, GOG_D); fh.put(c0+1, 1, GOG)
# длинные волосы из-под шляпы на затылке/у щёк (поверх головы)
hat['back'].rows(3, 7, HAIR, 0, 7)
hat['back'].rows(5, 7, HAIR_D, 0, 0); hat['back'].rows(5, 7, HAIR_D, 7, 7)
for c in (2, 5): hat['back'].rows(4, 7, HAIR_L, c, c)
hat['right'].rows(3, 7, HAIR, 0, 3); hat['left'].rows(3, 7, HAIR, 4, 7)

# ===================== ТОРС (16,16) =====================
body = box(16, 16, 8, 12, 4)
for k in ('right','left','back','top'): body[k].fill(PUR)
body['bottom'].fill(BELT_D)
fb = body['front']
fb.fill(PUR)
fb.rows(0, 0, PUR_D)                                   # воротник
fb.cols(0, 0, 1, 9, PUR_D); fb.cols(7, 7, 1, 9, PUR_D)
fb.cols(3, 3, 1, 9, PUR_D)                             # центральный шов
fb.cols(1, 2, 3, 3, PUR_L); fb.cols(5, 6, 3, 3, PUR_L) # грудные мышцы
for (r, c) in ((1,2),(1,5),(3,1),(3,6),(5,2),(5,5),(7,1),(7,6)):
    fb.put(c, r, BTN)
for (r, c) in ((1,2),(1,5),(3,1),(3,6)): pass
fb.rows(10, 11, BELT); fb.rows(11, 11, BELT_D)         # ремень
fb.cols(3, 4, 10, 10, BUCK); fb.put(3, 11, BALL); fb.put(4, 11, BUCK_D)   # пряжка (серебро + зелёный)
for k in ('right','left'):
    body[k].rows(10, 11, BELT); body[k].rows(11, 11, BELT_D)
    body[k].rows(0, 0, PUR_D)
body['back'].rows(10, 11, BELT); body['back'].rows(11, 11, BELT_D)
body['back'].rows(0, 0, PUR_D)

# ---- «куртка» (слой торса, y+16): плащ + волосы + кобуры ----
jk = box(16, 32, 8, 12, 4)
cb = jk['back']                                        # плащ сзади
cb.fill(CAPE)
for c in (2, 5): cb.cols(c, c, 1, 11, CAPE_D)
for c in (3, 6): cb.cols(c, c, 4, 10, CAPE_L)
cb.rows(0, 0, CAPE_D); cb.rows(11, 11, CAPE_D)
for k, cols in (('right', (0,1)), ('left', (2,3))):    # края плаща у боков
    jk[k].cols(cols[0], cols[1], 1, 11, CAPE); jk[k].cols(cols[0], cols[0], 1, 11, CAPE_D) if k=='right' else jk[k].cols(cols[1], cols[1], 1, 11, CAPE_D)
jk['top'].cols(0, 7, 0, 1, CAPE); jk['top'].rows(2, 3, CAPE_D, 0, 7) if False else None
# волосы поверх плаща и на груди
for c in range(1, 7): cb.put(c, 0, HAIR); cb.put(c, 1, HAIR)
for c in range(2, 6): cb.put(c, 2, HAIR_D)
cb.put(3, 3, HAIR_D); cb.put(4, 3, HAIR_D)
jf = jk['front']
for r, col in ((0, HAIR), (1, HAIR), (2, HAIR), (3, HAIR_D), (4, HAIR_D)):
    jf.put(1, r, col); jf.put(6, r, col)
jf.put(1, 2, HAIR_L); jf.put(6, 2, HAIR_L)
# кобуры со Стальными шарами на бёдрах (спереди по бокам ремня)
for c0 in (0, 6):
    jf.cols(c0, c0+1, 9, 11, BOOT); jf.cols(c0, c0+1, 11, 11, BOOT_D)
    jf.put(c0 if c0==0 else c0+1, 9, BALL)
    jf.put(c0+1 if c0==0 else c0, 9, BOOT_D)

# ===================== ПРАВАЯ РУКА (40,16) / ЛЕВАЯ (32,48) =====================
def arm(ox, oy, oox, ooy, outer_face, outer_col_front):
    a = box(ox, oy, 4, 12, 4)
    for k, f in a.items():
        if k in ('top',): f.fill(PUR); continue
        if k == 'bottom': f.fill(SKIN_D); continue
        f.fill(SKIN)
        f.rows(0, 3, PUR); f.rows(3, 3, PUR_D)               # короткий рукав
        f.rows(4, 4, ORG); f.rows(5, 5, DARK); f.rows(6, 6, ORG)   # полосатый напульсник
        f.rows(8, 8, BLUE_D); f.rows(9, 10, BLUE)             # синий бинт на запястье
        f.rows(11, 11, SKIN); f.rows(7, 7, SKIN_D)
    o = box(oox, ooy, 4, 12, 4)
    for k in ('right','front','left','back'): pass
    # зелёный наплечник плаща на внешней стороне
    o['top'].fill(CAPE)
    o['front'].rows(0, 1, CAPE, *( (0,1) if outer_col_front==0 else (2,3) ))
    o[outer_face].rows(0, 2, CAPE); o[outer_face].rows(2, 2, CAPE_D)
    o['back'].rows(0, 1, CAPE, *( (0,1) if outer_col_front==0 else (2,3) ))
    return a, o
arm(40, 16, 40, 32, 'right', 0)      # правая рука: внешняя — right, слева на виде спереди
arm(32, 48, 48, 48, 'left', 3)       # левая рука: внешняя — left, справа на виде спереди

# ===================== НОГИ =====================
def leg(ox, oy, oox, ooy):
    l = box(ox, oy, 4, 12, 4)
    for k, f in l.items():
        if k == 'top': f.fill(BELT); continue
        if k == 'bottom': f.fill(BOOT_D); continue
        f.fill(PANT)
        f.cols(0, 0, 0, 8, PANT_D) if k in ('front','back') else None
        f.rows(0, 0, BELT_D)
        f.rows(7, 7, PANT_D)                                  # колено
        f.rows(8, 8, BOOT_D); f.rows(9, 10, BOOT); f.rows(11, 11, BOOT_D)   # сапог
    # сталь на носке, шпора на пятке
    l['front'].cols(1, 2, 10, 11, STEEL); l['front'].rows(11, 11, STEEL_D, 1, 2)
    l['right'].cols(3, 3, 10, 11, STEEL); l['left'].cols(0, 0, 10, 11, STEEL)
    l['back'].put(1, 10, BUCK); l['back'].put(2, 10, BUCK_D)
    l['right'].put(0, 10, BUCK); l['left'].put(3, 10, BUCK)
    # фиолетовые наколенники-голенища
    o = box(oox, ooy, 4, 12, 4)
    o['front'].cols(1, 2, 7, 8, PUR); o['front'].put(1, 7, PUR_L)
    o['right'].cols(1, 2, 6, 8, PUR); o['left'].cols(1, 2, 6, 8, PUR)
    o['back'].cols(1, 2, 7, 8, PUR_D)
leg(0, 16, 0, 32)
leg(16, 48, 0, 48)

img.save(sys.argv[1] if len(sys.argv) > 1 else 'gyro_teacher.png')
print('ok')
