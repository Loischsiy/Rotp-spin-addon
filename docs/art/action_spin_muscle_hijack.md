# ТЗ: иконка действия «Перехват мышц (урок 2)» (`rotp_spin:spin_muscle_hijack`)

Лор: ✅ урок 2 «Работай мышцами» (`docs/spin-lore.md`).

- Путь: `src/main/resources/assets/rotp_spin/textures/action/spin_muscle_hijack.png`
- Где видна: хотбар ЛКМ в режиме не-стендовой силы (`J`) и меню раскладки RotP. Пока файла нет —
  фиолетово-чёрная заглушка.
- Размер **32×32**, как у иконок действий RotP (`assets/jojo/textures/action/hamon_healing.png`).
- PNG 32-bit RGBA, без потерь, прозрачный фон, без сглаживания и полупрозрачных краёв.
- Стиль: плоский пиксель-арт в духе иконок RotP, свет сверху-слева, 2–3 оттенка на материал,
  контур `#1e140b`.

Силуэт и детали: Раскрытая ладонь ≈14×16 px (кожа) в профиль слева; от кончиков пальцев вправо к силуэту напряжённого мускула/предплечья идут 2–3 концентрические золотые волны микровибраций. Без крови.

Палитра (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Кожа руки | `#e0b48a` | `#a8744f` | — |
| Золото Спина | `#d4a017` | `#8a6a0e` | `#f2d36b` |

Промпт для nano banana:
```
Pixel art game ability icon, 32x32 pixels, transparent background, no anti-aliasing,
dark outline #1e140b, light from top-left. An open human palm in profile on the left, about 14x16 pixels, sending two or three concentric golden vibration waves from the fingertips towards a tensed muscle silhouette on the right, no blood. skin #e0b48a, shadow #a8744f; Gold #d4a017, shadow #8a6a0e, highlight #f2d36b;
Clean flat sprite, bold readable silhouette, no text, no background, JoJo Steel Ball Run theme.
```
После генерации: уменьшить до 32×32 методом **Nearest Neighbor**, фон — в настоящую альфу,
цвета подогнать к палитре.

Приёмка: читается на 32×32 и в уменьшенном хотбаре; нет полупрозрачных пикселей; размер ровно 32×32.
