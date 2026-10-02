# ТЗ: иконка действия «Заряд Спина» (`rotp_spin:spin_ball_charge`)

Лор: ⚠️ ДОПУЩЕНИЕ (раскрутка шара в руке перед броском) на базе ✅ «Спин усиливает разрушительную
силу снаряда» (гл. 9) и ✅ «несовершенная сфера не держит высшее вращение» (гл. 84)
(`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_ball_charge.png`
- Где видна: хотбар ПКМ (способности) в режиме не-стендовой силы (`J`) и меню раскладки RotP.
  Пока файла нет — фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: В центре снизу раскрытая ладонь ≈14×8 px (кожа), над ней зелёный стальной шар
≈12×12 px. Вокруг шара — сужающаяся золотая спираль (3 витка, от ≈26 px к ≈14 px в диаметре),
конец спирали упирается в шар. Над шаром 2–3 золотые искры. Без лучей удара и без цели —
чтобы отличаться от `spin_ball_strike` (удар) и `spin_ball_throw` (полёт).

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Кожа руки | `#e0b48a` | `#a8744f` | — |
| Зелёный металл шара (как `steel_ball.md`) | `#33b12a` | `#1c8a1c`, глубокая тень `#0f5a12`, прорези `#06240a` | `#72d944`, зеркальный `#cdf99a` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. An open palm at the bottom center, about 14x8 pixels,
holding a green metal steel ball about 12x12 pixels above it; a tightening golden spiral of three turns
wraps around the ball and ends at it, two or three small golden sparks above the ball. No target, no impact rays.
skin #e0b48a, shadow #a8744f; emerald green metal #33b12a, shadow #1c8a1c, deep shadow #0f5a12, highlight #72d944, specular #cdf99a, dark slits #06240a;
Gold #d4a017, shadow #8a6a0e, highlight #f2d36b;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; спираль видна как «накопление», а не как
удар; отличается от `spin_ball_strike` и `spin_ball_throw`; нет полупрозрачных пикселей;
размер ровно 32×32.
