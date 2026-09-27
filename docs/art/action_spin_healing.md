# ТЗ: иконка действия «Медицина Цеппели» (`rotp_spin:spin_healing`)

Лор: ✅ медицинское применение Спина (`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_healing.png`
- Где видна: хотбар ПКМ (удержание) в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: Стальной шар ≈10×10 px по центру; вокруг него золотая спираль; поверх шара — светло-зелёный крест 6×6 px. Читается как «вращение + лечение».

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Сталь шара | `#8a949e` | `#4a5058` | `#d8dde2` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |
| Лечение (крест) | `#6fcf5a` | `#3f8f35` | — |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A steel ball about 10x10 pixels in the center wrapped by a golden spiral, with a light green 6x6 pixel medical cross over the ball. steel #8a949e, shadow #4a5058, highlight #d8dde2; Gold #d4a017, shadow #8a6a0e, highlight #f2d36b; cross #6fcf5a, shadow #3f8f35;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
