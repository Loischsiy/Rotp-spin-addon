# ТЗ: иконка действия «Бросок стального шара» (`rotp_spin:spin_ball_throw`)

Лор: ✅ фирменный бросок семьи Цеппели (`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_ball_throw.png`
- Где видна: хотбар ЛКМ (и быстрый доступ — средняя кнопка) в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: Зелёный Стальной шар ≈14×14 px, с шестиугольной пластиной и прорезями (как на `steel_ball.md`) в правой половине кадра; за ним 3 тонкие (1 px) золотые дуги-«следа» вращения, закрученные по спирали влево. Шар — главный элемент, дуги вторичны.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Зелёный металл шара (как `steel_ball.md`) | `#33b12a` | `#1c8a1c`, глубокая тень `#0f5a12`, контур/прорези `#06240a` | `#72d944`, зеркальный `#cdf99a` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A polished emerald-green metallic steel ball about 14x14 pixels with a hexagonal plate and short curved slits on the right side, with three thin 1-pixel golden motion arcs curling behind it in a spiral. emerald green metal #33b12a, shadow #1c8a1c, deep shadow #0f5a12, highlight #72d944, specular #cdf99a, dark slits #06240a; Gold #d4a017, shadow #8a6a0e, highlight #f2d36b;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
