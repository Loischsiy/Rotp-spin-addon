#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Final texture painters for Ball Breaker (128x128 box-UV atlas of docs/art/tools/gen_ball_breaker.py).

Not run directly: gen_ball_breaker.py calls install(module) at import time, which replaces its placeholder
`paint_cube`. Run `python3 docs/art/tools/gen_ball_breaker.py` to write the PNG (+ .bbmodel + geo.json).

References (JoJo Wiki -> Ball Breaker; the SBR anime has not shown it yet, so manga/game art is used):
  * colour manga art (infobox) and the ASB game render: glossy lime body, dark grey "scaled" plating on the
    sides of limbs, pink/magenta oval "caps" with a light spot, pink zigzag lines on chest and forearms;
  * head: six pink bulbs in random order on the face; discs (ears) with a darker top and underside;
  * hand (manga close-up): big pink ovals on the back of the hand, pink lines along the forearm;
  * boots: flared cuff, side fins, dark rocket-like slits.
Everything is drawn pixel by pixel per cube face; the left side is the mirrored right side.
"""
import random
from PIL import Image

# lime ramp (5 tones) + dark plating ramp + pink bulb ramp
L_HI = (200, 238, 172, 255)
L_LIGHT = (184, 230, 154, 255)
L_MID_HI = (153, 214, 116, 255)
L_MID = (122, 199, 79, 255)
L_MID_LO = (98, 161, 62, 255)
L_SHADE = (74, 122, 46, 255)
L_DEEP = (52, 92, 34, 255)
D_HI = (106, 118, 112, 255)
D_MID = (63, 74, 68, 255)
D_LO = (35, 42, 39, 255)
P_HI = (245, 168, 208, 255)
P_MID = (224, 96, 168, 255)
P_LO = (150, 56, 108, 255)
P_DEEP = (110, 36, 80, 255)
CREAM = (232, 244, 226, 255)


def install(g):
    """monkey-patch the generator module `g` with the final painters"""
    R = random.Random

    def put(im, x, y, c):
        if 0 <= x < im.width and 0 <= y < im.height:
            im.putpixel((x, y), c)

    def get(im, x, y):
        return im.getpixel((x, y)) if 0 <= x < im.width and 0 <= y < im.height else None

    def mul(c, f):
        return (int(c[0] * f), int(c[1] * f), int(c[2] * f), c[3])

    # ------------------------------------------------------------------ base surfaces
    def lime_face(face, w, h, tag, light=False):
        """glossy lime: light from top-left, darker underside, sparse speckle like manga hatching"""
        im = Image.new('RGBA', (max(w, 1), max(h, 1)), L_MID)
        if face == 'up':
            im.paste(L_LIGHT, (0, 0, w, h))
            for x in range(w):
                put(im, x, 0, L_HI) if face == 'up' else None
            for y in range(h):
                put(im, 0, y, L_HI)
        elif face == 'down':
            im.paste(L_SHADE, (0, 0, w, h))
        else:
            base = {'north': L_MID, 'east': L_MID, 'west': L_MID_LO, 'south': L_MID_LO}[face]
            im.paste(base, (0, 0, w, h))
            for x in range(w):  # top gloss row
                put(im, x, 0, L_MID_HI if face in ('north', 'east') else L_MID)
            if h >= 3:
                for x in range(w):  # bottom shade row
                    put(im, x, h - 1, L_MID_LO if face in ('north', 'east') else L_SHADE)
            if h >= 6:
                for x in range(w):
                    put(im, x, h - 2, L_MID_LO if face in ('north', 'east') else L_SHADE)
            if w >= 3 and face in ('north', 'east'):  # left gloss column
                for y in range(1, h - 1):
                    put(im, 0, y, L_MID_HI)
            if w >= 3 and face in ('north', 'west'):  # right edge shade
                for y in range(h):
                    put(im, w - 1, y, L_MID_LO if face == 'north' else L_SHADE)
        rnd = R(tag)
        for y in range(h):
            for x in range(w):
                if rnd.random() < 0.09 and w * h > 6:
                    c = get(im, x, y)
                    put(im, x, y, L_MID_HI if c in (L_MID, L_MID_LO) else (L_MID if c == L_LIGHT else c))
        if light:  # lighter variant (cuffs, neck)
            for y in range(h):
                for x in range(w):
                    c = get(im, x, y)
                    put(im, x, y, {L_MID: L_LIGHT, L_MID_HI: L_HI, L_MID_LO: L_MID_HI, L_SHADE: L_MID_LO}.get(c, c))
        return im

    def dark_face(face, w, h, tag):
        """dark grey scaled plating: horizontal ridges with brick seams"""
        im = Image.new('RGBA', (max(w, 1), max(h, 1)), D_MID)
        for y in range(h):
            for x in range(w):
                r = y % 3
                c = D_HI if r == 0 else (D_MID if r == 1 else D_LO)
                if r == 1 and (x + 2 * (y // 3)) % 4 == 0:
                    c = D_LO
                put(im, x, y, c)
        if face in ('up', 'down'):
            for y in range(h):
                for x in range(w):
                    put(im, x, y, D_MID if (x + y) % 2 else D_LO)
        return im

    # ------------------------------------------------------------------ bulbs / details
    def shadow(im, x, y, f=0.72):
        c = get(im, x, y)
        if c and c not in (P_HI, P_MID, P_LO):
            put(im, x, y, mul(c, f))

    def bulb(im, x, y, w=2, h=2):
        """pink oval cap with a light spot (top-left), darker rim (bottom-right) and a soft drop shadow"""
        for yy in range(h):
            for xx in range(w):
                if w >= 3 and h >= 3 and xx in (0, w - 1) and yy in (0, h - 1):
                    continue  # rounded corners of bigger bulbs
                if xx == 0 and yy == 0:
                    c = P_HI
                elif xx == w - 1 and yy == h - 1:
                    c = P_LO
                elif xx == w - 1 or yy == h - 1:
                    c = P_LO if (xx + yy) % 2 == 0 else P_MID
                else:
                    c = P_MID
                put(im, x + xx, y + yy, c)
        for xx in range(1, w + 1):
            shadow(im, x + xx, y + h)
        for yy in range(1, h):
            shadow(im, x + w, y + yy)

    def bulbs(im, spots, w=2, h=2):
        for (x, y) in spots:
            bulb(im, x, y, w, h)

    def zigzag(im, base_y, amp=(0, 1, 2, 1), color=P_MID, glow=True):
        for x in range(im.width):
            y = base_y + amp[x % len(amp)]
            if glow:
                put(im, x, y - 1, L_LIGHT)
            put(im, x, y, color)
            if glow:
                put(im, x, y + 1, L_SHADE)

    def vline(im, x, y0, y1, color=P_MID):
        for y in range(y0, y1 + 1):
            put(im, x, y, color if (y - y0) % 3 != 2 else P_LO)

    def slits(im, ys, x0, x1):
        for y in ys:
            for x in range(x0, x1 + 1):
                put(im, x, y, D_LO)

    # ------------------------------------------------------------------ per-cube painters: f(face, im)
    def p_thigh(face, im):
        if face == 'north':
            bulb(im, 1, 1, 2, 2); bulb(im, 0, 4, 1, 2)
        if face == 'east':
            bulb(im, 1, 2, 1, 2)

    def p_shin(face, im):
        if face == 'north':
            bulb(im, 0, 0, 2, 2)
            slits(im, [4], 0, 2)
        if face == 'east':
            bulb(im, 1, 1, 1, 2)

    def p_foot(face, im):
        if face == 'east':
            bulb(im, 1, 0, 2, 1)
        if face == 'north':
            for x in range(im.width):
                put(im, x, im.height - 1, L_DEEP)

    def p_toe(face, im):
        if face == 'up':
            put(im, 0, 0, L_HI)

    def p_fin_out(face, im):
        if face == 'east':
            slits(im, [1], 0, im.width - 2)
            slits(im, [2], 1, im.width - 1)
            for x in range(im.width):
                put(im, x, 0, L_HI)

    def p_fin_in(face, im):
        if face == 'west':
            slits(im, [1], 1, im.width - 1)
            slits(im, [2], 0, im.width - 2)
            for x in range(im.width):
                put(im, x, 0, L_HI)

    def p_cuff(face, im):
        if face in ('north', 'east', 'south', 'west'):
            for x in range(im.width):
                put(im, x, im.height - 1, D_MID)
                put(im, x, 0, L_HI)
        if face == 'north':
            bulb(im, 1, 0, 2, 1) if False else None

    def p_pelvis(face, im):
        if face in ('north', 'south'):
            for y in range(im.height):
                for x in list(range(0, 2)) + list(range(im.width - 2, im.width)):
                    put(im, x, y, D_HI if y == 0 else D_MID if (x + y) % 2 else D_LO)
        if face == 'north':
            bulb(im, 3, 0, 2, 2)

    def p_waist(face, im):
        if face == 'north':
            bulb(im, 0, 0, 1, 2); bulb(im, 2, 1, 1, 2)

    def p_chest_lower(face, im):
        if face == 'north':
            zigzag(im, 0, (0, 1, 1, 0), P_MID, glow=False)
            for x in range(im.width):
                put(im, x, 1 if (0, 1, 1, 0)[x % 4] == 0 else 0, L_MID_LO) if False else None

    def p_chest(face, im):
        if face == 'north':
            zigzag(im, 1, (0, 1, 2, 1), P_MID)
            put(im, 0, 0, L_HI)
            for x in range(im.width):
                if get(im, x, 0) == L_MID_HI:
                    put(im, x, 0, L_LIGHT)
        if face == 'south':
            zigzag(im, 1, (2, 1, 0, 1), P_LO)
        if face == 'up':
            for x in range(im.width):
                put(im, x, im.height - 1, L_MID_HI)

    def p_neck(face, im):
        if face in ('north', 'east', 'west', 'south'):
            for x in range(im.width):
                put(im, x, 1, L_MID_HI)

    def p_pauldron(face, im):
        if face == 'up':
            bulb(im, 1, 1, 2, 2)
            for x in range(im.width):
                put(im, x, 0, L_HI)
        if face == 'east':
            bulb(im, 1, 0, 2, 2)
        if face == 'north':
            bulb(im, 1, 0, 1, 2)
        if face in ('north', 'south'):
            for x in range(im.width):
                put(im, x, im.height - 1, L_SHADE)

    def p_arm(face, im):
        if face == 'east':
            bulb(im, 1, 0, 2, 2); bulb(im, 0, 3, 1, 2)
        if face == 'north':
            bulb(im, 2, 1, 1, 2)
        if face == 'west':  # inner side of the right limb: dark plating
            im.paste(dark_face('west', im.width, im.height, 'armw'), (0, 0))

    def p_forearm(face, im):
        if face == 'east':
            bulb(im, 0, 0, 2, 2); bulb(im, 1, 3, 1, 2)
        if face == 'north':
            vline(im, 1, 0, 4, P_MID)
            put(im, 0, 0, L_HI)
        if face == 'south':
            vline(im, 1, 0, 4, P_LO)
        if face == 'west':
            im.paste(dark_face('west', im.width, im.height, 'forew'), (0, 0))

    def p_palm(face, im):
        if face == 'east':  # back of the hand: big pink ovals (manga close-up)
            bulb(im, 1, 0, 2, 2)
        if face == 'up':
            bulb(im, 0, 1, 1, 2)
        if face == 'north':
            for x in range(im.width):
                put(im, x, im.height - 1, L_SHADE)

    def p_finger(face, im):
        if face in ('north', 'south'):
            for y in range(im.height):
                put(im, 0, y, L_MID_LO)
            put(im, 0, im.height - 1, L_SHADE)
        if face in ('east', 'west'):
            put(im, 0, im.height - 1, L_SHADE) if False else None
        if face == 'down':
            im.paste(L_SHADE, (0, 0, im.width, im.height))

    def p_head(face, im):
        if face == 'north':  # six bulbs in random order (lore: "circles in random order")
            for (x, y) in ((0, 1), (2, 0), (4, 1), (1, 3), (3, 3), (5, 4)):
                bulb(im, x, y, 1, 2)
            for y in range(im.height):
                put(im, im.width - 1, y, L_MID_LO) if get(im, im.width - 1, y) == L_MID else None
        if face == 'south':
            bulb(im, 1, 1, 1, 2); bulb(im, 4, 2, 1, 2)
            for x in range(im.width):
                put(im, x, im.height - 1, L_DEEP)
        if face in ('east', 'west'):
            bulb(im, 1, 1, 1, 2); bulb(im, 3, 3, 1, 2)
        if face == 'up':
            for x in range(1, im.width - 1):
                put(im, x, 1, L_HI)

    def p_ear_stalk(face, im):
        if face in ('north', 'south', 'east', 'west'):
            for y in range(im.height):
                put(im, 0, y, L_MID_LO)
                put(im, im.width - 1, y, L_DEEP)

    def p_ear_disc(face, im):
        w, h = im.width, im.height
        if face == 'up':  # concave dark top with a bright rim
            im.paste(L_SHADE, (0, 0, w, h))
            for y in range(h):
                for x in range(w):
                    if x in (0, w - 1) or y in (0, h - 1):
                        put(im, x, y, L_HI if (x == 0 or y == 0) else L_LIGHT)
                    elif x == 1 or y == 1:
                        put(im, x, y, L_DEEP)
                    elif (x + y) % 5 == 0:
                        put(im, x, y, L_MID_LO)
        elif face == 'down':
            im.paste(L_MID_LO, (0, 0, w, h))
            for y in range(h):
                for x in range(w):
                    if x in (0, w - 1) or y in (0, h - 1):
                        put(im, x, y, L_SHADE)
            put(im, w // 2, h // 2, L_DEEP)
        else:
            for x in range(w):
                for y in range(h):
                    put(im, x, y, L_LIGHT if face in ('north', 'east') else L_MID_HI)

    PAINT = {
        'Thigh': ('lime', p_thigh), 'ThighSide': ('dark', None), 'LegJoint': ('dark', None),
        'Shin': ('lime', p_shin), 'ShinSide': ('dark', None), 'BootCuff': ('lime_l', p_cuff),
        'Foot': ('lime', p_foot), 'Toe': ('lime', p_toe), 'FinOut': ('lime_l', p_fin_out),
        'FinIn': ('lime_l', p_fin_in),
        'pelvis': ('lime', p_pelvis), 'waist': ('lime', p_waist), 'chestLower': ('lime', p_chest_lower),
        'chest': ('lime', p_chest), 'neck': ('lime_l', p_neck),
        'Pauldron': ('lime', p_pauldron), 'Arm': ('lime', p_arm), 'ArmJoint': ('dark', None),
        'ForeArm': ('lime', p_forearm), 'Palm': ('lime', p_palm), 'FingerA': ('lime', p_finger),
        'FingerB': ('lime', p_finger), 'FingerC': ('lime', p_finger), 'Thumb': ('lime', p_finger),
        'head': ('lime', p_head), 'EarStalk': ('lime_s', p_ear_stalk), 'EarDisc': ('lime_l', p_ear_disc),
        'EarDiscFront': ('lime_l', p_ear_disc), 'EarDiscBack': ('lime_l', p_ear_disc),
    }

    def lookup(name):
        key = name
        for side in ('right', 'left'):
            if key.startswith(side):
                key = key[len(side):]
        if key in PAINT:
            return PAINT[key]
        if name in PAINT:
            return PAINT[name]
        raise KeyError(name)

    def paint_cube(c):
        """face images in 'as seen from outside' orientation (same contract as the placeholder painter)"""
        if c['src']:  # mirrored copy: flip the source cube's faces
            s = paint_cube(g.BYNAME[c['src']])
            flip = lambda im: im.transpose(Image.FLIP_LEFT_RIGHT)
            return {'north': flip(s['north']), 'south': flip(s['south']), 'east': flip(s['west']),
                    'west': flip(s['east']), 'up': flip(s['up']), 'down': flip(s['down'])}
        kind, fn = lookup(c['name'])
        out = {}
        for f, (w, h) in g.face_dims(c).items():
            tag = '%s/%s' % (c['name'], f)
            if kind == 'dark':
                im = dark_face(f, w, h, tag)
            elif kind == 'lime_s':
                im = lime_face(f, w, h, tag)
                im = im.point(lambda v: int(v * 0.8)) if False else im
            elif kind == 'lime_l':
                im = lime_face(f, w, h, tag, light=True)
            else:
                im = lime_face(f, w, h, tag)
            if fn:
                fn(f, im)
            out[f] = im
        return out

    g.paint_cube = paint_cube
