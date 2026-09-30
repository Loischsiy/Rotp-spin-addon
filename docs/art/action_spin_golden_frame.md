# ТЗ: иконка действия «Золотой прямоугольник» (`rotp_spin:spin_golden_frame`)

Лор: ⚠️ ДОПУЩЕНИЕ по механике, ✅ по образу: золотой прямоугольник можно «сложить» руками —
Векапипо бил Джайро по рукам именно поэтому (`docs/spin-lore.md`, «Золотой Спин и среда», гл. 51–54).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_golden_frame.png`
- Где видна: хотбар ПКМ (удержание) в режиме не-стендовой силы (`J`) и меню раскладки RotP, с урока 4.
  Файл сгенерирован `docs/art/tools/gen_frame_brace_icons.py` (см. «Сделано»).
- Размер **32×32**, как у остальных иконок действий (`action_spin_healing.md`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: две кисти рук (вид от первого лица, ладонями от зрителя), большие и указательные
пальцы образуют рамку — левая кисть в левом нижнем углу, правая в правом верхнем, как «кадр» фотографа.
Внутри рамки — золотой прямоугольник **1:1.618** (≈ 13×21 px, горизонтальный: 21 px в ширину,
13 px в высоту) с тонкой золотой спиралью, закручивающейся от правого нижнего угла. Спираль — 1 px,
читается как «золотое сечение». Руки не должны перекрывать спираль. Без шара: иконка о калибровке,
а не о броске.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Кожа рук | `#e0ac7e` | `#b07850`, глубокая тень `#7a4e30` | `#f4cfa8` |
| Перчатки-обрезки Джайро (фаланги пальцев) | `#3a3a52` | `#24243a` | `#5a5a78` |
| Золото Спина (рамка и спираль) | `#d4a017` | `#8a6a0e` | `#f2d36b` |
| Контур | `#1e140b` | — | — |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. Two hands seen from first person framing a horizontal
golden rectangle with thumbs and index fingers like a photographer's frame: left hand bottom-left,
right hand top-right. Inside the frame a thin 1-pixel golden spiral of the golden ratio, rectangle
proportion 1:1.618 (about 21x13 pixels). Skin #e0ac7e, shadow #b07850, deep shadow #7a4e30,
highlight #f4cfa8; fingerless glove cuffs #3a3a52, shadow #24243a, highlight #5a5a78;
gold #d4a017, shadow #8a6a0e, highlight #f2d36b.
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре (рамка из пальцев + спираль внутри различимы);
пропорция прямоугольника близка к 1:1.618; нет полупрозрачных пикселей; размер ровно 32×32.

## Сделано
✅ Сгенерирована скриптом `docs/art/tools/gen_frame_brace_icons.py`: прямоугольник 21×13 с кромкой
(блик сверху-слева, тень снизу-справа) на тёмном поле, спираль Фибоначчи (квадраты 13-8-5-3-2-1) вписана
внутрь кромки, правая рука — поворот левой на 180°. Только цвета из палитры, без полупрозрачности.
```bash
python3 docs/art/tools/gen_frame_brace_icons.py \
    --out src/main/resources/assets/rotp_spin/textures --preview .agent/preview/frame_brace.png
```
Расписная версия от художника подменяется простой заменой PNG — код менять не нужно.
