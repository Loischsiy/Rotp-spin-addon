# ТЗ: иконка действия «Спин на теле» (`rotp_spin:spin_body_brace`)

Лор: ✅ КАНОН — Спин на собственном теле делает его временно жёстким и передающим энергию
дальше (`docs/spin-lore.md`, «Общие свойства», гл. 22, 25, 54). Привязка к уроку 1 и числа — ⚠️.

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_body_brace.png`
- Где видна: хотбар ПКМ (удержание) в режиме не-стендовой силы (`J`) и меню раскладки RotP, с урока 1.
  Файл сгенерирован `docs/art/tools/gen_frame_brace_icons.py` (см. «Сделано»).
- Размер **32×32**, PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: торс человека в полуобороте (плечи и грудь, без лица), вокруг него две зелёные
дуги вращения 1–2 px, закрученные по часовой стрелке. Справа в грудь упирается пуля (3×2 px, серый
металл) — она сплющена и не входит; от точки удара — 3 коротких золотых луча «вибрации».
Без шара: иконка о теле, а не о броске.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Кожа / торс | `#e0ac7e` | `#b07850` | `#f4cfa8` |
| Дуги вращения (цвет стального шара) | `#3f8f4a` | `#25592d` | `#7fd18a` |
| Пуля | `#8c8c96` | `#55555e` | `#c8c8d2` |
| Вибрация | `#d4a017` | `#8a6a0e` | `#f2d36b` |
| Контур | `#1e140b` | — | — |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A human torso (shoulders and chest, no face) in three-quarter
view wrapped by two thin green spinning arcs turning clockwise (#3f8f4a, shadow #25592d, highlight #7fd18a).
A flattened grey bullet (#8c8c96, shadow #55555e, highlight #c8c8d2) stops against the chest on the right,
three short golden vibration rays (#d4a017, #8a6a0e, #f2d36b) from the impact point.
Skin #e0ac7e, shadow #b07850, highlight #f4cfa8. Clean flat sprite, bold readable silhouette,
no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре (торс + дуги + сплющенная пуля различимы);
нет полупрозрачных пикселей; размер ровно 32×32.

## Сделано
✅ Сгенерирована скриптом `docs/art/tools/gen_frame_brace_icons.py`: торс в полуобороте, две зелёные дуги
(задняя половина прячется за торсом, спереди разрыв со стрелкой влево — вращение по часовой при виде сверху),
пуля 3×2 у правого края груди, 3 золотых луча. Только цвета из палитры, без полупрозрачности.
```bash
python3 docs/art/tools/gen_frame_brace_icons.py \
    --out src/main/resources/assets/rotp_spin/textures --preview .agent/preview/frame_brace.png
```
Расписная версия от художника подменяется простой заменой PNG — код менять не нужно.
