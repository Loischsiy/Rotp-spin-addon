#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generator of docs/art/ball_breaker.bbmodel (Blockbench, GeckoLib Animation Utils format).

Why a script: the model-guide forbids hand-typing coordinates; symmetry must be mirrored, not copied.
Hierarchy and bone names are the same as in example_stand.geo.bbmodel (RotP-Addon-example,
branch new-model-anim-import); only pivots are moved to the new proportions.
Front of the model is -Z (like the template: the pickaxe extends to -Z).

Usage (from the project root):
    python3 docs/art/tools/gen_ball_breaker.py            # writes .bbmodel + placeholder PNG
    python3 docs/art/tools/gen_ball_breaker.py --preview  # + .agent/preview/*.png (self-check renders)

The PNG is only a PLACEHOLDER for checking the model in Blockbench (flat colours + studs).
The final texture is drawn by the artist (see ball_breaker.md).
"""
import base64, io, json, math, os, sys, uuid
import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ART = os.path.dirname(HERE)
PROJECT = os.path.dirname(os.path.dirname(ART))
OUT_MODEL = os.path.join(ART, 'ball_breaker.bbmodel')
OUT_PNG = os.path.join(ART, 'ball_breaker_placeholder.png')
PREVIEW_DIR = os.path.join(PROJECT, '.agent', 'preview')
TEX = 128
FACES = ['north', 'east', 'south', 'west', 'up', 'down']

# ---------------------------------------------------------------- palette (docs/art/ball_breaker.md)
LIME = (122, 199, 79, 255); LIME_S = (74, 122, 46, 255); LIME_L = (184, 230, 154, 255)
DARK = (63, 74, 68, 255); DARK_S = (35, 42, 39, 255); DARK_L = (106, 118, 112, 255)
PINK = (224, 96, 168, 255); PINK_S = (150, 56, 108, 255); PINK_L = (245, 168, 208, 255)
WHITE = (232, 244, 226, 255)

# ---------------------------------------------------------------- bones: name, parent, pivot, rotation
BONES = {}
ORDER = []


def bone(name, parent, origin, rot=None):
    BONES[name] = dict(parent=parent, origin=origin, rot=rot)
    ORDER.append(name)


def mirror_name(n):
    return n.replace('right', 'left', 1)


bone('stand_pos', None, [0, 24, 0])
bone('body_rot', 'stand_pos', [0, 24, 0])
bone('root', 'body_rot', [0, 16, 0])
bone('head', 'root', [0, 30, 0])
bone('head_rot', 'head', [0, 30, 0])
bone('rightEar', 'head_rot', [5.5, 36.5, -0.5], [-30, 0, -22])
bone('leftEar', 'head_rot', [-5.5, 36.5, -0.5], [-30, 0, 22])
bone('body', 'root', [0, 28, 0])
bone('upperPart', 'body', [0, 16, 0])
bone('torso', 'upperPart', [0, 28, 0])
for side, sx in (('left', -1), ('right', 1)):
    bone(side + 'ArmXRot', 'upperPart', [6.5 * sx, 27, 0])
    bone(side + 'Arm', side + 'ArmXRot', [6.5 * sx, 27, 0])
    bone(side + 'ArmJoint', side + 'Arm', [6.5 * sx, 21, 0])
    bone(side + 'ForeArm', side + 'Arm', [6.5 * sx, 21, 0])
for side, sx in (('left', -1), ('right', 1)):
    bone(side + 'LegXRot', 'body', [2.5 * sx, 16, 0])
    bone(side + 'Leg', side + 'LegXRot', [2.5 * sx, 16, 0])
    bone(side + 'LegJoint', side + 'Leg', [2.5 * sx, 9, 0])
    bone(side + 'LowerLeg', side + 'Leg', [2.5 * sx, 9, 0])

# ---------------------------------------------------------------- painters (placeholder texture)


def new(w, h, c):
    return Image.new('RGBA', (max(w, 1), max(h, 1)), c)


def put(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


def stud(im, x, y):
    """pink 2x2 oval stud with highlight and shadow"""
    put(im, x, y, PINK_L); put(im, x + 1, y, PINK)
    put(im, x, y + 1, PINK); put(im, x + 1, y + 1, PINK_S)


def grid_px(x, y):
    if x % 3 == 0 or y % 3 == 0:
        return DARK_S
    if x % 3 == 1 and y % 3 == 1:
        return DARK_L
    return DARK


def fill_grid(im, x0=0, x1=None):
    x1 = im.width if x1 is None else x1
    for y in range(im.height):
        for x in range(x0, x1):
            put(im, x, y, grid_px(x, y))


def base_face(kind, face, w, h):
    if kind == 'dark':
        im = new(w, h, DARK)
        fill_grid(im)
        return im
    tone = {'up': LIME_L, 'down': LIME_S, 'south': (100, 170, 64, 255)}.get(face, LIME)
    if kind == 'lime_l':
        tone = LIME_L if face != 'down' else LIME
    im = new(w, h, tone)
    if face in ('north', 'east', 'south', 'west') and h >= 3:
        for x in range(im.width):
            put(im, x, im.height - 1, LIME_S if kind != 'lime_l' else LIME)
    return im


# per-cube custom painters: f(face, im)
def p_pelvis(face, im):
    if face in ('north', 'south'):
        fill_grid(im, 0, 2)
        fill_grid(im, im.width - 2, im.width)
        if face == 'north':
            stud(im, 3, 0)


def p_chest(face, im):
    if face in ('north', 'south'):
        for base_y in (0, 3):
            for x in range(im.width):
                put(im, x, base_y + (0, 1, 1, 0)[x % 4], WHITE)


def p_chest_lower(face, im):
    if face == 'north':
        for x in range(im.width):
            put(im, x, (0, 1, 1, 0)[x % 4], WHITE)


def p_inner_dark(face, im):
    if face == 'west':  # inner side of the RIGHT limb (mirrored automatically for the left one)
        fill_grid(im)


def p_head(face, im):
    if face == 'north':
        for (x, y) in ((1, 1), (3, 0), (4, 2), (0, 3), (2, 3), (5, 4)):
            put(im, x, y, PINK)
            put(im, x, y + 1, PINK_S)
    if face in ('east', 'west'):
        stud(im, 2, 1)


def p_ear(face, im):
    if face == 'up':
        im.paste(LIME_S, (0, 0, im.width, im.height))
        for x in range(im.width):
            put(im, x, 0, LIME_L); put(im, x, im.height - 1, LIME_L)
        for y in range(im.height):
            put(im, 0, y, LIME_L); put(im, im.width - 1, y, LIME_L)
    elif face == 'down':
        im.paste(LIME, (0, 0, im.width, im.height))


def p_fingers(face, im):
    if face in ('north', 'south'):
        for y in range(im.height):
            put(im, 0, y, LIME_S)


# ---------------------------------------------------------------- cubes (RIGHT side is described, LEFT is mirrored)
CUBES = []


def C(name, bone_name, a, b, kind='lime', dots=None, paint=None, rot=None, org=None, sym=True):
    CUBES.append(dict(name=name, bone=bone_name, a=a, b=b, kind=kind, dots=dots or {}, paint=paint,
                      rot=rot, org=org, sym=sym, src=None))


# --- legs (hip pivot y=16, knee pivot y=9)
C('rightThigh', 'rightLeg', [0.5, 9, -1.5], [3.5, 16, 1.5], dots={'north': [(0, 0), (1, 3)]})
C('rightThighSide', 'rightLeg', [3.5, 9, -2], [4.5, 16, 2], kind='dark')
C('rightLegJoint', 'rightLegJoint', [1, 8, -1], [4, 10, 1], kind='dark')
C('rightShin', 'rightLowerLeg', [1, 2, -1.5], [4, 7, 1.5], dots={'north': [(1, 0), (0, 3)]})
C('rightShinSide', 'rightLowerLeg', [4, 3, -1.5], [5, 7, 1.5], kind='dark')
C('rightBootCuff', 'rightLowerLeg', [0.5, 7, -2], [4.5, 9, 2], kind='lime_l')
C('rightFoot', 'rightLowerLeg', [1, 0, -2], [4, 2, 2])
C('rightToe', 'rightLowerLeg', [1.5, 0, -4], [3.5, 1, -2])
C('rightFinOut', 'rightLowerLeg', [4, 0, -3], [5, 3, 1], kind='lime_l')
C('rightFinIn', 'rightLowerLeg', [0, 0, -3], [1, 3, 1], kind='lime_l')

# --- torso
C('pelvis', 'torso', [-4, 16, -2], [4, 18, 2], paint=p_pelvis, sym=False)
C('waist', 'torso', [-2, 18, -1.5], [2, 21, 1.5], dots={'north': [(0, 0), (2, 1)]}, sym=False)
C('chestLower', 'torso', [-3, 21, -2], [3, 23, 2], paint=p_chest_lower, sym=False)
C('chest', 'torso', [-5, 23, -2], [5, 28, 2], paint=p_chest, sym=False)
C('neck', 'torso', [-1.5, 28, -1.5], [1.5, 30, 1.5], kind='lime_l', sym=False)

# --- arms (shoulder pivot y=27, elbow y=21)
C('rightPauldron', 'rightArm', [5, 26, -2.5], [9, 29, 2.5], dots={'east': [(0, 0), (3, 1)], 'north': [(1, 0)]})
C('rightArm', 'rightArm', [5, 21, -1.5], [8, 26, 1.5], paint=p_inner_dark, dots={'east': [(0, 0), (1, 3)], 'north': [(1, 0)]})
C('rightArmJoint', 'rightArmJoint', [5.5, 20, -1], [7.5, 22, 1], kind='dark')
C('rightForeArm', 'rightForeArm', [5, 16, -1.5], [8, 21, 1.5], paint=p_inner_dark, dots={'east': [(1, 0), (0, 3)]})
C('rightPalm', 'rightForeArm', [5, 14, -2], [8, 16, 2])
C('rightFingerA', 'rightForeArm', [5, 11, -1], [6, 14, 1], paint=p_fingers)
C('rightFingerB', 'rightForeArm', [6, 10, -1], [7, 14, 1], paint=p_fingers)
C('rightFingerC', 'rightForeArm', [7, 11, -1], [8, 14, 1], paint=p_fingers)
C('rightThumb', 'rightForeArm', [8, 12, -1], [9, 14, 1], rot=[0, 0, 20], org=[8, 14, 0])

# --- head (pushed 1 forward = slouch without rotating pivots), ears = discs on stalks
C('head', 'head_rot', [-3, 30, -3.5], [3, 36, 1.5], paint=p_head, sym=False)
C('rightEarStalk', 'head_rot', [2, 36, -1.5], [4, 38, 0.5], kind='lime_s')
C('rightEarDisc', 'rightEar', [2, 36, -3], [9, 37, 2], paint=p_ear)
C('rightEarDiscFront', 'rightEar', [4, 36, -4], [7, 37, -3], paint=p_ear)
C('rightEarDiscBack', 'rightEar', [4, 36, 2], [7, 37, 3], paint=p_ear)


def mirror_cube(c):
    m = dict(c)
    m['name'] = mirror_name(c['name'])
    m['bone'] = mirror_name(c['bone'])
    a, b = c['a'], c['b']
    m['a'] = [-b[0], a[1], a[2]]
    m['b'] = [-a[0], b[1], b[2]]
    if c['rot']:
        m['rot'] = [c['rot'][0], -c['rot'][1], -c['rot'][2]]
    if c['org']:
        m['org'] = [-c['org'][0], c['org'][1], c['org'][2]]
    m['src'] = c['name']
    return m


ALL = []
for c in CUBES:
    ALL.append(c)
    if c['sym']:
        ALL.append(mirror_cube(c))
BYNAME = {c['name']: c for c in ALL}


def size_of(c):
    s = [c['b'][i] - c['a'][i] for i in range(3)]
    assert all(abs(v - round(v)) < 1e-9 and v >= 1 for v in s), (c['name'], s)
    return [int(round(v)) for v in s]


def face_dims(c):
    w, h, d = size_of(c)
    return {'north': (w, h), 'south': (w, h), 'east': (d, h), 'west': (d, h), 'up': (w, d), 'down': (w, d)}


def paint_cube(c):
    """face images in 'as seen from outside' orientation"""
    if c['src']:  # mirrored copy: flip the source cube's faces
        s = paint_cube(BYNAME[c['src']])
        flip = lambda im: im.transpose(Image.FLIP_LEFT_RIGHT)
        return {'north': flip(s['north']), 'south': flip(s['south']), 'east': flip(s['west']),
                'west': flip(s['east']), 'up': flip(s['up']), 'down': flip(s['down'])}
    out = {}
    for f, (w, h) in face_dims(c).items():
        im = base_face(c['kind'], f, w, h)
        for (x, y) in c['dots'].get(f, []):
            stud(im, x, y)
        if c['paint']:
            c['paint'](f, im)
        out[f] = im
    return out


# ---------------------------------------------------------------- UV packing (box UV)
def pack():
    rects = []
    for c in ALL:
        w, h, d = size_of(c)
        rects.append((c, 2 * d + 2 * w, d + h))
    rects.sort(key=lambda r: (-r[2], -r[1]))
    x = y = shelf = 0
    for c, rw, rh in rects:
        if x + rw > TEX:
            x, y, shelf = 0, y + shelf, 0
        c['uv'] = [x, y]
        x += rw
        shelf = max(shelf, rh)
    assert y + shelf <= TEX, 'texture overflow: %d' % (y + shelf)
    return y + shelf


def face_uvs(c):
    w, h, d = size_of(c)
    u, v = c['uv']
    return {
        'north': [u + d, v + d, u + d + w, v + d + h],
        'east': [u, v + d, u + d, v + d + h],
        'south': [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        'west': [u + d + w, v + d, u + 2 * d + w, v + d + h],
        'up': [u + d + w, v + d, u + d, v],
        'down': [u + d + 2 * w, v, u + d + w, v + d],
    }


def build_texture():
    tex = Image.new('RGBA', (TEX, TEX), (0, 0, 0, 0))
    for c in ALL:
        imgs = paint_cube(c)
        c['imgs'] = imgs
        uv = face_uvs(c)
        for f in FACES:
            x1, y1, x2, y2 = uv[f]
            tex.paste(imgs[f], (min(x1, x2), min(y1, y2)))
    return tex


# ---------------------------------------------------------------- .bbmodel
def uid(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'rotp_spin/ball_breaker/' + name))


def build_bbmodel(tex):
    buf = io.BytesIO()
    tex.save(buf, 'PNG')
    b64 = base64.b64encode(buf.getvalue()).decode()
    elements = []
    for i, c in enumerate(ALL):
        uvs = face_uvs(c)
        e = {
            'name': c['name'], 'box_uv': True, 'rescale': False, 'locked': False, 'light_emission': 0,
            'render_order': 'default', 'allow_mirror_modeling': False,
            'from': c['a'], 'to': c['b'], 'autouv': 0, 'color': i % 8,
            'origin': c['org'] or [0, 0, 0], 'uv_offset': c['uv'],
            'faces': {f: {'uv': uvs[f], 'texture': 0} for f in FACES},
            'type': 'cube', 'uuid': uid('cube/' + c['name']),
        }
        if c['rot']:
            e['rotation'] = c['rot']
        elements.append(e)
    kids = {n: [] for n in ORDER}
    for c in ALL:
        kids[c['bone']].append(uid('cube/' + c['name']))

    def group(n):
        b = BONES[n]
        g = {'name': n, 'origin': b['origin'], 'color': 0, 'uuid': uid('bone/' + n), 'export': True,
             'mirror_uv': False, 'isOpen': True, 'locked': False, 'visibility': True, 'autouv': 0,
             'selected': False, 'children': list(kids[n])}
        if b['rot']:
            g['rotation'] = b['rot']
        for m in ORDER:
            if BONES[m]['parent'] == n:
                g['children'].append(group(m))
        return g

    outliner = [group('stand_pos')]
    texture = {
        'path': '', 'name': 'ball_breaker_placeholder.png', 'folder': '', 'namespace': '', 'id': '0',
        'width': TEX, 'height': TEX, 'uv_width': TEX, 'uv_height': TEX, 'particle': False,
        'use_as_default': False, 'layers_enabled': False, 'sync_to_project': '', 'render_mode': 'default',
        'render_sides': 'auto', 'frame_time': 1, 'frame_order_type': 'loop', 'frame_order': '',
        'frame_interpolate': False, 'visible': True, 'internal': True, 'saved': True,
        'uuid': uid('texture'), 'source': 'data:image/png;base64,' + b64,
    }
    return {
        'meta': {'format_version': '4.10', 'model_format': 'animated_entity_model', 'box_uv': True},
        'name': 'ball_breaker.geo', 'model_identifier': 'BallBreakerModelBlockbench',
        'visible_box': [4, 4.5, 1.75],
        'variable_placeholders': 'query.head_x_rotation = 0\nquery.head_y_rotation = 0\n',
        'variable_placeholder_buttons': [], 'timeline_setups': [], 'unhandled_root_fields': {},
        'geckolib_modid': '', 'geckolib_model_type': 'Entity',
        'geckolib_filepath_cache': {'model': 'assets/rotp_spin/geo/ball_breaker.geo.json'},
        'resolution': {'width': TEX, 'height': TEX},
        'elements': elements, 'outliner': outliner, 'textures': [texture], 'animations': [],
        'animation_variable_placeholders': '',
    }


# ---------------------------------------------------------------- self-check preview (software renderer)
def rotM(r):
    rx, ry, rz = [math.radians(v) for v in r]
    Rx = np.array([[1, 0, 0], [0, math.cos(rx), -math.sin(rx)], [0, math.sin(rx), math.cos(rx)]])
    Ry = np.array([[math.cos(ry), 0, math.sin(ry)], [0, 1, 0], [-math.sin(ry), 0, math.cos(ry)]])
    Rz = np.array([[math.cos(rz), -math.sin(rz), 0], [math.sin(rz), math.cos(rz), 0], [0, 0, 1]])
    return Rz @ Ry @ Rx  # same order as Blockbench / Minecraft ModelRenderer


def to_world(p, c):
    p = np.array(p, float)
    if c['rot']:
        o = np.array(c['org'], float)
        p = o + rotM(c['rot']) @ (p - o)
    b = c['bone']
    while b:
        bd = BONES[b]
        if bd['rot']:
            o = np.array(bd['origin'], float)
            p = o + rotM(bd['rot']) @ (p - o)
        b = bd['parent']
    return p


def face_corners(c, f):
    (x0, y0, z0), (x1, y1, z1) = c['a'], c['b']
    tbl = {
        'north': ((x1, y1, z0), (x0, y1, z0), (x1, y0, z0)),
        'south': ((x0, y1, z1), (x1, y1, z1), (x0, y0, z1)),
        'east': ((x1, y1, z1), (x1, y1, z0), (x1, y0, z1)),
        'west': ((x0, y1, z0), (x0, y1, z1), (x0, y0, z0)),
        'up': ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1)),
        'down': ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)),
    }
    return [to_world(p, c) for p in tbl[f]]


def render(yaw, elev, S=16):
    cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    e = math.radians(-elev)
    Rx = np.array([[1, 0, 0], [0, math.cos(e), -math.sin(e)], [0, math.sin(e), math.cos(e)]])
    V = Rx @ Ry
    light = np.array([0.3, 0.8, -0.5]); light /= np.linalg.norm(light)
    faces = []
    allpts = []
    for c in ALL:
        center = to_world([(c['a'][i] + c['b'][i]) / 2 for i in range(3)], c)
        for f in FACES:
            TL, TR, BL = [V @ p for p in face_corners(c, f)]
            n = np.cross(TR - TL, BL - TL)
            fc = (TR + BL) / 2
            if np.dot(n, fc - V @ center) < 0:
                n = -n
            if n[2] >= -1e-6:  # viewer at -Z
                continue
            n = n / np.linalg.norm(n)
            shade = 0.55 + 0.45 * max(0.0, float(np.dot(n, V @ light)))
            faces.append((float(fc[2]), c, f, TL, TR, BL, shade))
            allpts += [TL, TR, BL, TR + BL - TL]
    xs = [-p[0] for p in allpts]; ys = [p[1] for p in allpts]
    minx, maxx, miny, maxy = min(xs), max(xs), min(ys), max(ys)
    W = int((maxx - minx) * S) + 40; H = int((maxy - miny) * S) + 40
    canvas = Image.new('RGBA', (W, H), (24, 26, 30, 255))
    sc = lambda p: np.array([(-p[0] - minx) * S + 20, (maxy - p[1]) * S + 20])
    for depth, c, f, TL, TR, BL, shade in sorted(faces, key=lambda t: -t[0]):
        im = c['imgs'][f]
        w, h = im.size
        a, b, d = sc(TL), sc(TR), sc(BL)
        ex = (b - a) / w; ey = (d - a) / h
        M = np.array([[ex[0], ey[0]], [ex[1], ey[1]]])
        if abs(np.linalg.det(M)) < 1e-6:
            continue
        Mi = np.linalg.inv(M)
        pts = [a, b, d, b + d - a]
        x0 = int(math.floor(min(p[0] for p in pts))) - 1; y0 = int(math.floor(min(p[1] for p in pts))) - 1
        x1 = int(math.ceil(max(p[0] for p in pts))) + 1; y1 = int(math.ceil(max(p[1] for p in pts))) + 1
        cx = -(Mi[0][0] * a[0] + Mi[0][1] * a[1]) + Mi[0][0] * x0 + Mi[0][1] * y0
        cf = -(Mi[1][0] * a[0] + Mi[1][1] * a[1]) + Mi[1][0] * x0 + Mi[1][1] * y0
        tr = im.transform((x1 - x0, y1 - y0), Image.AFFINE, (Mi[0][0], Mi[0][1], cx, Mi[1][0], Mi[1][1], cf),
                          resample=Image.NEAREST)
        arr = np.array(tr).astype(float)
        arr[..., :3] *= shade
        tr = Image.fromarray(arr.clip(0, 255).astype('uint8'), 'RGBA')
        canvas.alpha_composite(tr, (x0, y0))
    return canvas


def preview():
    os.makedirs(PREVIEW_DIR, exist_ok=True)
    views = [render(0, 0), render(90, 0), render(-35, 20), render(180, 0)]
    H = max(v.height for v in views)
    sheet = Image.new('RGBA', (sum(v.width for v in views), H), (24, 26, 30, 255))
    x = 0
    for v in views:
        sheet.paste(v, (x, H - v.height)); x += v.width
    sheet.save(os.path.join(PREVIEW_DIR, 'sheet.png'))
    for name, v in zip(('front', 'side', 'three_quarter', 'back'), views):
        v.save(os.path.join(PREVIEW_DIR, name + '.png'))
    print('preview ->', PREVIEW_DIR)


def checks():
    n = len(ALL)
    print('cubes:', n, '(limit 60)')
    assert n <= 60
    # overlapping volumes of unrotated cubes (possible z-fighting), joints are allowed to sit inside limbs
    for i, p in enumerate(ALL):
        for q in ALL[i + 1:]:
            if p['rot'] or q['rot'] or p['bone'].endswith('Joint') or q['bone'].endswith('Joint'):
                continue
            if p['bone'] in ('rightEar', 'leftEar') or q['bone'] in ('rightEar', 'leftEar'):
                continue
            ov = [min(p['b'][k], q['b'][k]) - max(p['a'][k], q['a'][k]) for k in range(3)]
            if all(o > 1e-9 for o in ov):
                print('  OVERLAP', p['name'], q['name'], ov)
    tops = max(c['b'][1] for c in ALL if 'Ear' not in c['bone'])
    print('body height (without ears):', tops)


if __name__ == '__main__':
    used = pack()
    print('texture rows used: %d / %d' % (used, TEX))
    tex = build_texture()
    tex.save(OUT_PNG)
    with open(OUT_MODEL, 'w') as fh:
        json.dump(build_bbmodel(tex), fh, indent=1)
    checks()
    print('written', OUT_MODEL)
    if '--preview' in sys.argv:
        preview()
