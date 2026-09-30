# ТЗ: диск стенда Ball Breaker (`rotp_spin:stand_disc_ball_breaker`)

Лор: ✅ визуализация энергии Спина от высшей бросковой техники семьи Цеппели
(`docs/spin-lore.md` → «Ball Breaker»). Диск — носитель стенда, не способность;
свой вид нужен только чтобы не терялся среди дисков в инвентаре.

- Файл: `src/main/resources/assets/rotp_spin/textures/item/stand_disc_ball_breaker.png`
- Модель: `models/item/stand_disc_ball_breaker.json` (`parent: item/generated`). Пока слои
  указывают на `jojo:item/stand_disc` (фолбэк без варнинга); после приёмки PNG переключить
  `layer0`/`layer1` на `rotp_spin:item/stand_disc_ball_breaker`.
- Размер: **16×16** (как базовый диск).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Силуэт: **та же композиция, что `jojo:item/stand_disc`** (круглый диск, центральное отверстие,
  ободок) — только перекрас акцентов, чтобы диск остался узнаваемым диском RotP.
- Перекрас: основание диска тёмное `#3f4a44` (вставки стенда), кольцо обода лаймовое `#7ac74f`
  с тенью `#4a7a2e`, 3–4 розовые точки-«глаза» `#e060a8` по кольцу, центр отверстия золотой
  `#b8860b`. Контур `#1e140b`.
- Палитра (hex) — из `ball_breaker.md`:
| Материал | База | Тень | Блик |
|---|---|---|---|
| Тело (лайм) | `#7ac74f` | `#4a7a2e` | `#b8e69a` |
| Вставки (тёмные) | `#3f4a44` | `#232a27` | `#6a7670` |
| Узор (розовый) | `#e060a8` | `#96386c` | `#f5a8d0` |
| Центр (золото) | `#b8860b` | `#7a5a08` | `#e0b84a` |
| Контур | `#1e140b` | — | — |

Промпт для nano banana:
```
Pixel art Minecraft item icon, 16x16 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. A round stand disc like a vinyl record with
a center hole: dark body #3f4a44, lime-green rim ring #7ac74f with shadow #4a7a2e,
3 small pink dots #e060a8 on the ring, golden center #b8860b. JoJo Steel Ball Run theme.
Flat sprite, no text, no background.
```
После генерации: уменьшить до 16×16 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре. Затем в `models/item/stand_disc_ball_breaker.json` заменить оба слоя
на `rotp_spin:item/stand_disc_ball_breaker` и проверить в игре (диск в руке, варнингов нет).

Приёмка: на 16×16 читается как диск RotP; лаймовое кольцо видно в хотбаре; нет полупрозрачных пикселей.
