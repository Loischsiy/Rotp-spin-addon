# ТЗ: калибровочная пряжка (`rotp_spin:calibration_buckle`)

Лор: ✅ урок 4 «Отдай дань уважения. Вращай пули по золотому сечению» (`docs/spin-lore.md` →
«Золотой Спин и среда»): пряжка ремня Джайро в пропорциях золотого прямоугольника — эталон
калибровки. В «мёртвых» биомах (замёрзший пролив, безжизненная местность) калибровать нечем —
эффективность падает; пряжка в инвентаре заменяет природные маркеры (⚠️ геймплей).

- Файл: `src/main/resources/assets/rotp_spin/textures/item/calibration_buckle.png`
- Модель: `models/item/calibration_buckle.json` (`parent: item/generated`). Своей PNG пока нет —
  заглушка.
- Рецепт: `data/rotp_spin/recipes/calibration_buckle.json` (8 золотых самородков + кожа).
- Размер: **16×16** (мелкий предмет, как самородок).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Силуэт: латунная прямоугольная пряжка 10×16 px (пропорция 1:1.618 — золотой прямоугольник),
  внутри гравировка золотой спирали 1–2 px; сбоку маленькое кожаное ушко-крепление.
- Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Латунь | `#b8860b` | `#7a5a08` | `#e0b84a` |
| Кожа | `#5a3f28` | `#3a2817` | `#7a5a3c` |
| Контур | `#1e140b` | — | — |

Промпт для nano banana:
```
Pixel art Minecraft item icon, 16x16 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A brass belt buckle, 10x16 pixels, golden
rectangle proportions 1:1.618, with a thin engraved golden spiral inside and a small
leather strap loop on the side. Brass #b8860b with shadow #7a5a08 and highlight #e0b84a;
leather #5a3f28, shadow #3a2817. Flat sprite, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 16×16 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 16×16; спираль видна хотя бы тенью; нет полупрозрачных пикселей.
