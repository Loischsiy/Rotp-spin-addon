# ТЗ: иконка действия «Бросок блока» (`rotp_spin:spin_block_throw`)

Лор: ✅ урок 3 «Верь во вращение»: вращение переходит на неживую материю (`docs/spin-lore.md`).
Грубая материя — не идеальная сфера, поэтому Golden-бонуса у блоков нет (⚠️ геймплей).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_block_throw.png`
- Где видна: хотбар ЛКМ в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: куб земли/камня ≈12×12 px в центре-справа, наклонённый на ~15° (полёт);
вокруг него 2 золотых кольца-орбиты (1 px); слева 2 короткие линии движения.
Не шар и не пробка: читается как «тяжёлый куб».

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Камень куба | `#7d7d7d` | `#4a4a4a` | `#a8a8a8` |
| Земля (низ куба) | `#8a5f3c` | `#5a3d24` | — |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A heavy stone-and-dirt cube about 12x12 pixels,
tilted 15 degrees as if flying, center-right, wrapped by two thin 1-pixel golden orbit rings,
two short motion lines on the left. Stone #7d7d7d, shadow #4a4a4a, highlight #a8a8a8;
dirt #8a5f3c, shadow #5a3d24; gold #d4a017, shadow #8a6a0e, highlight #f2d36b.
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
