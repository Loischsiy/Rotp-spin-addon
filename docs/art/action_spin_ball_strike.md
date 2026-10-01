# ТЗ: иконка действия «Удар вращением (урок 1)» (`rotp_spin:spin_ball_strike`)

Лор: ⚠️ ДОПУЩЕНИЕ на базе ✅ передачи Спина касанием (урок 1) и передачи энергии вибрациями
(`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_ball_strike.png`
- Где видна: хотбар ЛКМ в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: Слева кулак ≈12×12 px (кожа), сжимающий зелёный стальной шар ≈10×10 px; вокруг шара
2 золотые дуги вращения со стрелками. Справа от шара — веер из 3 коротких золотых лучей удара и
тёмный силуэт фигуры (≈8×14 px), отклонённый вправо-вверх (отброс). Без крови.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Кожа руки | `#e0b48a` | `#a8744f` | — |
| Зелёный металл шара (как `steel_ball.md`) | `#33b12a` | `#1c8a1c`, глубокая тень `#0f5a12`, прорези `#06240a` | `#72d944`, зеркальный `#cdf99a` |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |
| Силуэт цели | `#4a3b2c` | `#2c2219` | — |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A fist on the left, about 12x12 pixels, gripping a green
metal steel ball about 10x10 pixels with two golden spinning arcs with arrowheads around it; on the right
three short golden impact rays and a dark humanoid silhouette knocked back up and to the right, no blood.
skin #e0b48a, shadow #a8744f; emerald green metal #33b12a, shadow #1c8a1c, deep shadow #0f5a12, highlight #72d944, specular #cdf99a, dark slits #06240a;
Gold #d4a017, shadow #8a6a0e, highlight #f2d36b; silhouette #4a3b2c, shadow #2c2219;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; отличается от `spin_ball_throw` (шар в руке,
а не в полёте); нет полупрозрачных пикселей; размер ровно 32×32.
