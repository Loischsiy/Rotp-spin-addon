#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generator of assets/rotp_spin/animations/ball_breaker.animation.json (GeckoLib / Bedrock animation format).

All values below are written in BLOCKBENCH space (the same space as the .bbmodel), and converted on export
like Blockbench does: rotation -> [-x, -y, z], position -> [-x, y, z].
Cheat sheet in Blockbench space (front of the model is -Z):
  * +X on an arm / leg swings its end FORWARD; +X on the head / torso leans BACK, -X leans forward.
  * right side is +X: +Z on a right limb moves it OUTWARD, on a left limb -Z is outward.
  * elbows: +X on *ForeArm bends the hand up/forward; knees: -X on *LowerLeg bends the foot back.

Usage (project root):
    python3 docs/art/tools/gen_ball_breaker_anim.py            # writes the .animation.json
    python3 docs/art/tools/gen_ball_breaker_anim.py --preview  # + .agent/preview/anim_*.png
"""
import json, math, os, sys
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen_ball_breaker as gen  # noqa: E402

OUT = os.path.join(gen.PROJECT, 'src', 'main', 'resources', 'assets', 'rotp_spin', 'animations',
                   'ball_breaker.animation.json')
HEAD_LOOK = ['query.head_x_rotation', 'query.head_y_rotation', 0]
LEFT_RIGHT = ('left', 'right')


# ------------------------------------------------------------------ tiny animation DSL
class Anim:
    def __init__(self, name, length, loop, timeline=None):
        self.name, self.length, self.loop, self.timeline = name, length, loop, timeline
        self.tracks = {}  # (bone, channel) -> [(t, [x,y,z], easing)]

    def key(self, bone, ch, t, vec, ease=None):
        self.tracks.setdefault((bone, ch), []).append((round(t, 4), [round(float(v), 4) + 0.0 for v in vec], ease))

    def both(self, tmpl, ch, t, vec, ease=None, mirror=(1, 1, 1)):
        """tmpl like 'ArmXRot' -> leftArmXRot/rightArmXRot; mirror flips sign per axis for the LEFT side"""
        self.key('right' + tmpl, ch, t, vec, ease)
        self.key('left' + tmpl, ch, t, [v * m for v, m in zip(vec, mirror)], ease)


MIRROR_Z = (1, -1, -1)  # left limb = right limb with Y and Z rotation negated


def ease_fn(name, u):
    if name == 'easeInSine':
        return 1 - math.cos(u * math.pi / 2)
    if name == 'easeOutSine':
        return math.sin(u * math.pi / 2)
    if name == 'easeInOutSine':
        return -(math.cos(math.pi * u) - 1) / 2
    return u


def sample(anim, bone, ch, t, default):
    kfs = anim.tracks.get((bone, ch))
    if not kfs:
        return list(default)
    kfs = sorted(kfs, key=lambda k: k[0])
    if t <= kfs[0][0]:
        return list(kfs[0][1])
    for (t0, v0, _), (t1, v1, e1) in zip(kfs, kfs[1:]):
        if t0 <= t <= t1:
            u = ease_fn(e1, (t - t0) / (t1 - t0)) if t1 > t0 else 1
            return [a + (b - a) * u for a, b in zip(v0, v1)]
    return list(kfs[-1][1])


DEFAULTS = {'rot': [0, 0, 0], 'pos': [0, 0, 0], 'scale': [1, 1, 1]}

# ------------------------------------------------------------------ idle: 3.2 s seamless loop (hover + breathing)
T = 3.2
N = 8


def wave(anim, bone, ch, base, amp, phase, axis, ease='easeInOutSine'):
    """adds a looping sine on one axis; several axes of one track are merged by calling wave_multi"""
    pass


def sine(t, phase):
    return math.sin(2 * math.pi * (t / T + phase))


def looped(anim, bone, ch, fn, ease='easeInOutSine'):
    """fn(t) -> vec ; keyframes every T/N, last == first"""
    for k in range(N + 1):
        t = T * k / N
        vec = fn(T * (k % N) / N)
        anim.key(bone, ch, t, vec, ease if k else None)


def make_idle():
    a = Anim('idle', T, True)
    looped(a, 'root', 'pos', lambda t: [0, 0.6 * sine(t, 0), 0])
    looped(a, 'upperPart', 'rot', lambda t: [-8 + 1.5 * sine(t, 0.1), 0, 0])
    looped(a, 'head', 'rot', lambda t: [5 + 1.5 * sine(t, 0.2), 0, 1.5 * sine(t, 0.45)])
    for side, off in (('right', 0.0), ('left', 0.5)):
        sgn = 1 if side == 'right' else -1
        looped(a, side + 'Arm', 'rot',
               lambda t, s=sgn, o=off: [8 + 2 * sine(t, 0.3 + o), 0, s * (5 + 2 * sine(t, 0.1 + o))])
        looped(a, side + 'ForeArm', 'rot', lambda t, o=off: [14 + 4 * sine(t, 0.35 + o), 0, 0])
        looped(a, side + 'Ear', 'rot',
               lambda t, s=sgn, o=off: [3 * sine(t, 0.5 + o), 0, s * 4 * sine(t, 0.25 + o)])
    looped(a, 'leftLeg', 'rot', lambda t: [8 + 2 * sine(t, 0.5), 0, 0])
    looped(a, 'leftLowerLeg', 'rot', lambda t: [-14 - 3 * sine(t, 0.5), 0, 0])
    looped(a, 'rightLeg', 'rot', lambda t: [4 + 2 * sine(t, 0.0), 0, 0])
    looped(a, 'rightLowerLeg', 'rot', lambda t: [-8 - 3 * sine(t, 0.0), 0, 0])
    return a


IDLE = make_idle()


def idle0(bone, ch):
    return sample(IDLE, bone, ch, 0.0, DEFAULTS[ch])


def finish(anim, at_start=False, at_end=False):
    """dock to idle: every channel idle animates must start (or end) exactly on idle's t=0 value"""
    for (bone, ch) in IDLE.tracks:
        kfs = anim.tracks.setdefault((bone, ch), [])
        if at_end:
            kfs[:] = [k for k in kfs if k[0] < anim.length - 1e-6]
            kfs.append((anim.length, idle0(bone, ch), 'easeInOutSine'))
            if len(kfs) == 1:
                kfs.insert(0, (0.0, idle0(bone, ch), None))
        if at_start:
            kfs[:] = [k for k in kfs if k[0] > 1e-6]
            kfs.insert(0, (0.0, idle0(bone, ch), None))
            if len(kfs) == 1:
                kfs.append((anim.length, idle0(bone, ch), None))
    return anim


# ------------------------------------------------------------------ summon: 1.2 s, ends exactly on idle t=0
def make_summon():
    a = Anim('summon', 1.2, 'hold_on_last_frame')
    # 0.0  curled up, tiny, spinning in
    a.key('root', 'scale', 0.0, [0.2, 0.2, 0.2])
    a.key('root', 'scale', 0.55, [1.08, 1.08, 1.08], 'easeOutSine')
    a.key('root', 'scale', 0.85, [0.98, 0.98, 0.98], 'easeInOutSine')
    a.key('root', 'scale', 1.2, [1, 1, 1], 'easeInOutSine')
    a.key('root', 'pos', 0.0, [0, -4, 0])
    a.key('root', 'pos', 0.55, [0, 1.5, 0], 'easeOutSine')
    a.key('body', 'rot', 0.0, [0, -180, 0])
    a.key('body', 'rot', 0.55, [0, -8, 0], 'easeOutSine')
    a.key('body', 'rot', 1.2, [0, 0, 0], 'easeInOutSine')
    a.key('upperPart', 'rot', 0.0, [-30, 0, 0])
    a.key('upperPart', 'rot', 0.55, [6, 0, 0], 'easeOutSine')
    a.key('head', 'rot', 0.0, [20, 0, 0])
    a.key('head', 'rot', 0.55, [-6, 0, 0], 'easeOutSine')
    # arms: crossed on the chest -> flung out
    a.both('Arm', 'rot', 0.0, [40, 0, -20], mirror=MIRROR_Z)
    a.both('Arm', 'rot', 0.55, [5, 0, 55], 'easeOutSine', mirror=MIRROR_Z)
    a.both('ForeArm', 'rot', 0.0, [90, 0, 0])
    a.both('ForeArm', 'rot', 0.55, [25, 0, 0], 'easeOutSine')
    # legs: crouch -> extend
    a.both('Leg', 'rot', 0.0, [30, 0, 0])
    a.both('Leg', 'rot', 0.55, [0, 0, 0], 'easeOutSine')
    a.both('LowerLeg', 'rot', 0.0, [-60, 0, 0])
    a.both('LowerLeg', 'rot', 0.55, [-8, 0, 0], 'easeOutSine')
    # ears droop -> pop up
    a.key('rightEar', 'rot', 0.0, [0, 0, -25])
    a.key('rightEar', 'rot', 0.55, [0, 0, 25], 'easeOutSine')
    a.key('leftEar', 'rot', 0.0, [0, 0, 25])
    a.key('leftEar', 'rot', 0.55, [0, 0, -25], 'easeOutSine')
    for b in ('rightEar', 'leftEar', 'rightArm', 'leftArm', 'rightForeArm', 'leftForeArm', 'rightLeg', 'leftLeg',
              'rightLowerLeg', 'leftLowerLeg', 'upperPart', 'head'):
        pass
    # end keys on idle t=0 for everything touched or animated by idle
    for (bone, ch) in list(a.tracks) + list(IDLE.tracks):
        kfs = a.tracks.setdefault((bone, ch), [])
        kfs[:] = [k for k in kfs if k[0] < a.length - 1e-6]
        kfs.append((a.length, idle0(bone, ch), 'easeInOutSine'))
    return a


# ------------------------------------------------------------------ dissipate: 1.2 s, starts exactly on idle t=0
def make_dissipate():
    a = Anim('dissipate', 1.2, 'hold_on_last_frame')
    a.key('body', 'rot', 0.0, [0, 0, 0])
    a.key('body', 'rot', 0.3, [0, 20, 0], 'easeOutSine')
    a.key('body', 'rot', 1.2, [0, 540, 0], 'easeInSine')
    a.key('root', 'scale', 0.0, [1, 1, 1])
    a.key('root', 'scale', 0.3, [1.06, 1.06, 1.06], 'easeOutSine')
    a.key('root', 'scale', 1.2, [0.01, 0.01, 0.01], 'easeInSine')
    a.key('root', 'pos', 0.0, [0, 0, 0])
    a.key('root', 'pos', 0.3, [0, -1, 0], 'easeOutSine')
    a.key('root', 'pos', 1.2, [0, 10, 0], 'easeInSine')
    a.key('upperPart', 'rot', 0.0, [0, 0, 0])
    a.key('upperPart', 'rot', 0.3, [8, 0, 0], 'easeOutSine')
    a.key('upperPart', 'rot', 1.2, [-12, 0, 0], 'easeInSine')
    a.key('head', 'rot', 0.3, [-8, 0, 0], 'easeOutSine')
    a.key('head', 'rot', 1.2, [-15, 0, 0], 'easeInSine')
    a.both('Arm', 'rot', 0.3, [-5, 0, 45], 'easeOutSine', mirror=MIRROR_Z)
    a.both('Arm', 'rot', 1.2, [0, 0, 85], 'easeInSine', mirror=MIRROR_Z)
    a.both('ForeArm', 'rot', 0.3, [10, 0, 0], 'easeOutSine')
    a.both('ForeArm', 'rot', 1.2, [0, 0, 0], 'easeInSine')
    a.both('Leg', 'rot', 1.2, [-5, 0, 0], 'easeInSine')
    a.both('LowerLeg', 'rot', 1.2, [-40, 0, 0], 'easeInSine')
    a.key('rightEar', 'rot', 0.3, [0, 0, 20], 'easeOutSine')
    a.key('rightEar', 'rot', 1.2, [0, 0, -20], 'easeInSine')
    a.key('leftEar', 'rot', 0.3, [0, 0, -20], 'easeOutSine')
    a.key('leftEar', 'rot', 1.2, [0, 0, 20], 'easeInSine')
    for (bone, ch) in list(a.tracks) + list(IDLE.tracks):
        kfs = a.tracks.setdefault((bone, ch), [])
        kfs[:] = [k for k in kfs if k[0] > 1e-6]
        kfs.insert(0, (0.0, idle0(bone, ch), None))
        if len(kfs) == 1:
            kfs.append((a.length, idle0(bone, ch), None))
    return a


# ------------------------------------------------------------------ combat helpers
def start_on_idle(a):
    """t=0 of every touched/idle channel == idle t=0 (no pop when switching from idle)"""
    for (bone, ch) in list(a.tracks) + list(IDLE.tracks):
        kfs = a.tracks.setdefault((bone, ch), [])
        kfs[:] = [k for k in kfs if k[0] > 1e-6]
        kfs.insert(0, (0.0, idle0(bone, ch), None))


def end_on_idle(a, ease='easeInOutSine'):
    """last frame of every touched/idle channel == idle t=0 (seamless return)"""
    for (bone, ch) in list(a.tracks) + list(IDLE.tracks):
        kfs = a.tracks.setdefault((bone, ch), [])
        kfs[:] = [k for k in kfs if k[0] < a.length - 1e-6]
        kfs.append((a.length, idle0(bone, ch), ease))


def guard_left(a, times, vec_arm=(40, 0, 15), vec_fore=(100, 0, 0)):
    for t in times:
        a.key('leftArm', 'rot', t, list(vec_arm), 'easeOutSine')
        a.key('leftForeArm', 'rot', t, list(vec_fore), 'easeOutSine')


# punch_light: fast right jab (WINDUP 0.0 -> PERFORM 0.12 -> RECOVERY 0.3)
def make_punch_light():
    a = Anim('punch_light', 0.8, 'hold_on_last_frame',
             {'0.0': 'phase = WINDUP;', '0.12': 'phase = PERFORM;', '0.3': 'phase = RECOVERY;'})
    for t, (by, ux, ra, rf, rz, lz, pz, ep) in {
        0.12: (-20, -14, -18, 110, 14, -14, 0.0, 0),    # coil: right side back, fist at the chest
        0.2: (26, -20, 88, 4, 6, 4, -4.0, 14),          # jab: shoulder through, lunge forward
        0.3: (26, -20, 88, 4, 6, 4, -4.0, 14),
    }.items():
        e = 'easeOutSine'
        a.key('body', 'rot', t, [0, by, 0], e)
        a.key('upperPart', 'rot', t, [ux, 0, 0], e)
        a.key('rightArm', 'rot', t, [ra, 0, rz], e)
        a.key('rightForeArm', 'rot', t, [rf, 0, 0], e)
        a.key('root', 'pos', t, [0, 0, pz], e)
        a.key('rightEar', 'rot', t, [ep, 0, 0], e)
        a.key('leftEar', 'rot', t, [ep, 0, 0], e)
    guard_left(a, (0.12, 0.2, 0.3))
    a.key('rightLeg', 'rot', 0.12, [-6, 0, 0], 'easeOutSine'); a.key('rightLeg', 'rot', 0.2, [-14, 0, 0], 'easeOutSine')
    a.key('leftLeg', 'rot', 0.12, [10, 0, 0], 'easeOutSine'); a.key('leftLeg', 'rot', 0.2, [22, 0, 0], 'easeOutSine')
    a.key('leftLowerLeg', 'rot', 0.2, [-26, 0, 0], 'easeOutSine')
    start_on_idle(a); end_on_idle(a)
    return a


# punch_heavy: big coil, then a full-body right hook-straight (WINDUP 0.0 -> PERFORM 0.6 -> RECOVERY 0.85)
def make_punch_heavy():
    a = Anim('punch_heavy', 1.4, 'hold_on_last_frame',
             {'0.0': 'phase = WINDUP;', '0.6': 'phase = PERFORM;', '0.85': 'phase = RECOVERY;'})
    # coil (0.0 -> 0.55): slow, low, right fist pulled all the way back
    a.key('body', 'rot', 0.55, [0, -38, 0], 'easeInOutSine')
    a.key('upperPart', 'rot', 0.55, [6, 0, 0], 'easeInOutSine')
    a.key('head', 'rot', 0.55, [-4, 0, 0], 'easeInOutSine')
    a.key('rightArm', 'rot', 0.55, [-45, 0, 22], 'easeInOutSine')
    a.key('rightForeArm', 'rot', 0.55, [115, 0, 0], 'easeInOutSine')
    guard_left(a, (0.55,), (55, 0, 25), (95, 0, 0))
    a.both('Leg', 'rot', 0.55, [26, 0, 0], 'easeInOutSine')
    a.both('LowerLeg', 'rot', 0.55, [-50, 0, 0], 'easeInOutSine')
    a.key('root', 'pos', 0.55, [0, -2.5, 1.5], 'easeInOutSine')
    a.key('rightEar', 'rot', 0.55, [0, 0, -12], 'easeInOutSine')
    a.key('leftEar', 'rot', 0.55, [0, 0, 12], 'easeInOutSine')
    # strike (0.55 -> 0.68): explosive
    a.key('body', 'rot', 0.68, [0, 34, 0], 'easeOutSine')
    a.key('upperPart', 'rot', 0.68, [-26, 0, 0], 'easeOutSine')
    a.key('head', 'rot', 0.68, [10, 0, 0], 'easeOutSine')
    a.key('rightArm', 'rot', 0.68, [92, 0, 4], 'easeOutSine')
    a.key('rightForeArm', 'rot', 0.68, [0, 0, 0], 'easeOutSine')
    guard_left(a, (0.68,), (-25, 0, 10), (20, 0, 0))
    a.key('rightLeg', 'rot', 0.68, [-22, 0, 0], 'easeOutSine')
    a.key('rightLowerLeg', 'rot', 0.68, [-10, 0, 0], 'easeOutSine')
    a.key('leftLeg', 'rot', 0.68, [34, 0, 0], 'easeOutSine')
    a.key('leftLowerLeg', 'rot', 0.68, [-40, 0, 0], 'easeOutSine')
    a.key('root', 'pos', 0.68, [0, 0.5, -6], 'easeOutSine')
    a.key('rightEar', 'rot', 0.68, [22, 0, 8], 'easeOutSine')
    a.key('leftEar', 'rot', 0.68, [22, 0, -8], 'easeOutSine')
    # hold the follow-through until 0.85, then return
    for ch_bone, vec in (('body', [0, 34, 0]), ('upperPart', [-26, 0, 0]), ('head', [10, 0, 0]),
                         ('rightArm', [92, 0, 4]), ('rightForeArm', [0, 0, 0]), ('root', [0, 0.5, -6]),
                         ('rightLeg', [-22, 0, 0]), ('rightLowerLeg', [-10, 0, 0]),
                         ('leftLeg', [34, 0, 0]), ('leftLowerLeg', [-40, 0, 0]),
                         ('leftArm', [-25, 0, 10]), ('leftForeArm', [20, 0, 0])):
        ch = 'pos' if ch_bone == 'root' else 'rot'
        a.key(ch_bone, ch, 0.85, vec, None)
    start_on_idle(a); end_on_idle(a)
    return a


# block: both forearms crossed in front of the face; ramp 0.25 s, then hold
def make_block():
    a = Anim('block', 0.25, 'hold_on_last_frame')
    e = 'easeOutSine'
    a.key('upperPart', 'rot', 0.25, [-12, 0, 0], e)
    a.key('head', 'rot', 0.25, [12, 0, 0], e)
    a.both('Arm', 'rot', 0.25, [62, 0, -28], e, mirror=MIRROR_Z)
    a.both('ForeArm', 'rot', 0.25, [105, 0, 0], e)
    a.both('Leg', 'rot', 0.25, [12, 0, 6], e, mirror=MIRROR_Z)
    a.both('LowerLeg', 'rot', 0.25, [-24, 0, 0], e)
    a.key('root', 'pos', 0.25, [0, -1, 0], e)
    a.key('rightEar', 'rot', 0.25, [0, 0, -30], e)
    a.key('leftEar', 'rot', 0.25, [0, 0, 30], e)
    # cross the wrists: forearms rotate inward a little around the vertical axis
    a.key('rightForeArm', 'rot', 0.25, [105, -32, 0], e)
    a.key('leftForeArm', 'rot', 0.25, [105, 32, 0], e)
    start_on_idle(a)
    return a


# senescence_touch: reach, palm rests on the target (aging), fingers tremble, release
# WINDUP 0.0 -> PERFORM 0.45 (palm contact) -> RECOVERY 1.2
def make_senescence_touch():
    a = Anim('senescence_touch', 1.7, 'hold_on_last_frame',
             {'0.0': 'phase = WINDUP;', '0.45': 'phase = PERFORM;', '1.2': 'phase = RECOVERY;'})
    e = 'easeInOutSine'
    a.key('body', 'rot', 0.45, [0, 14, 0], e)
    a.key('upperPart', 'rot', 0.45, [-18, 0, 0], e)
    a.key('head', 'rot', 0.45, [8, 0, 0], e)
    a.key('rightArm', 'rot', 0.45, [92, 0, 8], e)
    a.key('rightForeArm', 'rot', 0.45, [6, 0, 0], e)
    guard_left(a, (0.45,), (-12, 0, 14), (30, 0, 0))
    a.key('root', 'pos', 0.45, [0, 0.4, -3], e)
    a.key('leftLeg', 'rot', 0.45, [22, 0, 0], e)
    a.key('leftLowerLeg', 'rot', 0.45, [-30, 0, 0], e)
    a.key('rightLeg', 'rot', 0.45, [-8, 0, 0], e)
    a.key('rightEar', 'rot', 0.45, [10, 0, 6], e)
    a.key('leftEar', 'rot', 0.45, [10, 0, -6], e)
    # contact: hand keeps pressure and trembles, ears buzz (stored rotation discharges)
    tremble = [(0.6, 1), (0.75, -1), (0.9, 1), (1.05, -1), (1.2, 0)]
    for t, s_ in tremble:
        a.key('rightForeArm', 'rot', t, [6 + 4 * s_, 0, 2 * s_], 'linear')
        a.key('rightArm', 'rot', t, [92, 0, 8 + 1.5 * s_], 'linear')
        a.key('rightEar', 'rot', t, [10, 0, 6 + 8 * s_], 'linear')
        a.key('leftEar', 'rot', t, [10, 0, -6 - 8 * s_], 'linear')
        for bone, vec, ch in (('body', [0, 14, 0], 'rot'), ('upperPart', [-18, 0, 0], 'rot'), ('head', [8, 0, 0], 'rot'),
                              ('root', [0, 0.4, -3], 'pos'), ('leftLeg', [22, 0, 0], 'rot'),
                              ('leftLowerLeg', [-30, 0, 0], 'rot'), ('rightLeg', [-8, 0, 0], 'rot'),
                              ('leftArm', [-12, 0, 14], 'rot'), ('leftForeArm', [30, 0, 0], 'rot')):
            a.key(bone, ch, t, vec, None)
    start_on_idle(a); end_on_idle(a)
    return a


ANIMS = [IDLE, make_summon(), make_dissipate(), make_punch_light(), make_punch_heavy(), make_block(),
         make_senescence_touch()]


# ------------------------------------------------------------------ neck follow
# `head` is a child of `root` (like example_stand), NOT of `upperPart`/`body`, so it does not follow the torso
# when the torso bends or twists. We add a `head` position track that carries the neck point along with the
# torso chain (upperPart -> body). Head ROTATION stays free (head_rot looks at the target).
def neck_offset(anim, t):
    n = np.array(gen.BONES['head']['origin'], float)
    p = n.copy()
    for b in ('upperPart', 'body'):
        bd = gen.BONES[b]
        o = np.array(bd['origin'], float)
        rot = np.array(bd['rot'] or [0, 0, 0], float) + np.array(sample(anim, b, 'rot', t, DEFAULTS['rot']), float)
        pos = np.array(sample(anim, b, 'pos', t, DEFAULTS['pos']), float)
        p = o + gen.rotM(rot) @ (p - o) + pos
    return p - n


def follow_neck(anim, step=0.05):
    times = {0.0, round(anim.length, 4)}
    k = 1
    while k * step < anim.length - 1e-6:
        times.add(round(k * step, 4)); k += 1
    for b in ('upperPart', 'body'):
        for ch in ('rot', 'pos'):
            times.update(kf[0] for kf in anim.tracks.get((b, ch), []))
    anim.tracks[('head', 'pos')] = [(t, list(neck_offset(anim, t)), None) for t in sorted(times)]
    for i, (t, v, e) in enumerate(anim.tracks[('head', 'pos')]):
        anim.tracks[('head', 'pos')][i] = (t, [round(float(x), 4) + 0.0 for x in v], e)


for _a in ANIMS:
    follow_neck(_a, 0.1 if _a.name == 'idle' else 0.05)


# ------------------------------------------------------------------ export (Blockbench space -> Bedrock/GeckoLib)
def conv(ch, v):
    if ch == 'rot':
        return [-v[0] + 0.0, -v[1] + 0.0, v[2] + 0.0]
    if ch == 'pos':
        return [-v[0] + 0.0, v[1], v[2]]
    return list(v)


def tt(t):
    return '%.4g' % t if t != int(t) else '%.1f' % t


def export():
    out = {'format_version': '1.8.0', 'animations': {}}
    for a in ANIMS:
        bones = {}
        for (bone, ch), kfs in a.tracks.items():
            name = {'rot': 'rotation', 'pos': 'position', 'scale': 'scale'}[ch]
            kfs = sorted(kfs, key=lambda k: k[0])
            frames = {}
            for t, vec, ease in kfs:
                fr = {'vector': [round(x, 4) for x in conv(ch, vec)]}
                if ease:
                    fr['easing'] = ease
                frames[tt(t)] = fr
            bones.setdefault(bone, {})[name] = frames
        bones.setdefault('head_rot', {})['rotation'] = {'vector': HEAD_LOOK}
        entry = {'loop': a.loop, 'animation_length': a.length}
        if a.timeline:
            entry['timeline'] = a.timeline
        entry['bones'] = dict(sorted(bones.items()))
        out['animations'][a.name] = entry
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w') as fh:
        json.dump(out, fh, indent=2)
    print('animations ->', OUT, [(a.name, a.length) for a in ANIMS])


# ------------------------------------------------------------------ self-check preview
POSE = {}


def to_world_pose(p, c):
    p = np.array(p, float)
    if c['rot']:
        o = np.array(c['org'], float)
        p = o + gen.rotM(c['rot']) @ (p - o)
    b = c['bone']
    while b:
        bd = gen.BONES[b]
        o = np.array(bd['origin'], float)
        ps = POSE.get(b, {})
        rot = np.array(bd['rot'] or [0, 0, 0], float) + np.array(ps.get('rot', [0, 0, 0]), float)
        sc = np.array(ps.get('scale', [1, 1, 1]), float)
        p = o + gen.rotM(rot) @ (sc * (p - o)) + np.array(ps.get('pos', [0, 0, 0]), float)
        b = bd['parent']
    return p


def set_pose(anim, t):
    POSE.clear()
    for (bone, ch) in anim.tracks:
        POSE.setdefault(bone, {})[ch] = sample(anim, bone, ch, t, DEFAULTS[ch])


def preview():
    gen.pack(); gen.build_texture()
    gen.to_world = to_world_pose
    S = 6
    bounds = (-18, 18, -3, 54)
    cols = {'idle': [0, 0.8, 1.6, 2.4], 'summon': [0, 0.2, 0.4, 0.55, 0.85, 1.2],
            'dissipate': [0, 0.3, 0.6, 0.9, 1.1, 1.2],
            'punch_light': [0, 0.06, 0.12, 0.2, 0.4, 0.8], 'punch_heavy': [0, 0.3, 0.55, 0.68, 1.0, 1.4],
            'block': [0, 0.12, 0.25], 'senescence_touch': [0, 0.25, 0.45, 0.75, 1.2, 1.7]}
    os.makedirs(gen.PREVIEW_DIR, exist_ok=True)
    rows = []
    for a in ANIMS:
        row = []
        for t in cols[a.name]:
            set_pose(a, t)
            f = gen.render(0, 0, S, bounds); s = gen.render(90, 0, S, bounds)
            cell = Image.new('RGBA', (f.width + s.width, f.height), (24, 26, 30, 255))
            cell.paste(f, (0, 0)); cell.paste(s, (f.width, 0))
            row.append(cell)
        w = sum(c.width for c in row); h = row[0].height
        strip = Image.new('RGBA', (w, h), (24, 26, 30, 255))
        x = 0
        for c in row:
            strip.paste(c, (x, 0)); x += c.width
        strip.save(os.path.join(gen.PREVIEW_DIR, 'anim_%s.png' % a.name))
        print('preview', a.name, strip.size)


if __name__ == '__main__':
    export()
    if '--preview' in sys.argv:
        preview()
