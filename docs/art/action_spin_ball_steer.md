# ТЗ: иконка действия «Управление шаром» (`rotp_spin:spin_ball_steer`)

Лор: ⚠️ довод шара взглядом — геймплей (`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_ball_steer.png`
- Где видна: хотбар ПКМ (удержание) в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: Шар ≈8×8 px на изогнутой пунктирной золотой траектории-«S» из левого нижнего угла в правый верхний; на конце траектории — маленькая стрелка.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Сталь шара | `#8a949e` | `#4a5058` | `#d8dde2` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A small steel ball about 8x8 pixels riding a dashed golden S-shaped trajectory from the bottom-left corner to the top-right corner, ending with a small arrow head. steel #8a949e, shadow #4a5058, highlight #d8dde2; Gold #d4a017, shadow #8a6a0e, highlight #f2d36b;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
