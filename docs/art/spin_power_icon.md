# ТЗ: иконка силы «Спин» (`rotp_spin:spin`)

Где видна: меню сил RotP и HUD способностей (рядом с иконками Хамона, Вампиризма и т.д.).
Лор: ✅ Спин — дисциплина вращения по золотому сечению, не «магия» (`docs/spin-lore.md` → «Природа Спина»).

- Файл: `src/main/resources/assets/rotp_spin/textures/power/spin.png`
- Размер: **32×32** — как у иконок сил RotP (`assets/jojo/textures/power/hamon.png` — 32×32).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Силуэт: **Золотая спираль**, вписанная в золотой прямоугольник 1:1.618 (≈ 20×32 или 32×20 px,
  отцентрировать), спираль закручивается к зелёному Стальному шару в «глазу» спирали (≈ 6×6 px).
  Читается с расстояния как «спираль + шар», без мелких деталей.
- Палитра (hex): золото `#d4a017` (база, совпадает с полоской энергии), `#8a6a0e` (тень),
  `#f2d36b` (блик); зелёный металл шара `#33b12a`, `#1c8a1c`, `#72d944` (+ `#0f5a12`, `#06240a`, `#cdf99a`); контур `#1e140b`.
- Стиль: плоская пиксельная иконка в духе иконок RotP; свет сверху-слева; 2–3 оттенка на материал.

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A golden spiral (Fibonacci / golden ratio spiral)
inscribed in a golden rectangle with 1:1.618 proportions, spiralling into a small polished
emerald-green metallic steel ball at its center. Gold #d4a017 with shadow #8a6a0e and highlight #f2d36b;
emerald green metal #33b12a, shadow #1c8a1c, deep shadow #0f5a12, highlight #72d944, specular #cdf99a. Clean flat sprite, bold readable silhouette,
no text, no background, JoJo Steel Ball Run theme.
```
После генерации: Nearest Neighbor до 32×32, фон — в настоящую альфу, цвета — к палитре.
