# ТЗ: иконка действия «Бросок с вращением» (`rotp_spin:spin_item_throw`)

Лор: ✅ урок 3 «Верь во вращение»: вращение переходит на неживую материю (пробка → ногти → Tusk ACT1),
`docs/spin-lore.md`.

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_item_throw.png`
- Где видна: хотбар ЛКМ в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: винная **пробка** ≈10×12 px, наклонённая на 30°, в центре-справа; вокруг неё
2 золотых кольца-орбиты (1 px), закрученные спиралью; слева от пробки 2 коротких линии движения.
Не шар: иконка должна отличаться от «Броска стального шара» с первого взгляда.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Пробка | `#b88a55` | `#7d5a33` | `#d9b27f` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A wine cork about 10x12 pixels tilted 30 degrees,
center-right, wrapped by two thin 1-pixel golden orbit rings twisted like a spiral, two short
motion lines on the left. Cork #b88a55, shadow #7d5a33, highlight #d9b27f; gold #d4a017,
shadow #8a6a0e, highlight #f2d36b. Clean flat sprite, bold readable silhouette, no text,
no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
