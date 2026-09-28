# ТЗ: Учитель Джайро — NPC-наставник + скин

Лор: ✅ мастер Спина семьи Цеппели учит урокам 1–5 (`docs/spin-lore.md`).
NPC выдаёт силу `rotp_spin:spin` вместо команды `/jojopower give` и ведёт учёт практики
(комментарий в `InitPowers`: «learning from the Zeppeli family comes later» — это оно).

## Канон внешности (JoJo Wiki → Gyro Zeppeli, базовый наряд)
- Широкополая шляпа с прорезями на тулье и полях + очки-«щёлки» на тулье.
- Короткорукавная чёрная кожаная куртка с заклёпками, кожаные штаны, ковбойские сапоги.
- Ремень с двумя боковыми кобурами под Стальные шары.
- Длинные светлые волосы (помпадур + маллет), золотистые грилзы (зубы).
- Палитра манги гуляет — берём базовый тёмный наряд (узнаваемее всего).

Палитра скина (hex):
| Материал | База | Тень | Блик |
|---|---|---|---|
| Шляпа (тёмно-коричневая) | `#4a3626` | `#2c1f14` | `#6e5238` |
| Очки (серебро) | `#8a949e` | `#4a5058` | `#d8dde2` |
| Куртка/штаны (чёрная кожа) | `#2b2b30` | `#151518` | `#4a4a52` |
| Заклёпки (латунь) | `#b8860b` | `#7a5a08` | `#e0b84a` |
| Волосы (блонд) | `#d8c27a` | `#9a8548` | `#f2e2ac` |
| Кожа | `#e0b08a` | `#a87858` | `#f5d0b0` |
| Грилзы (золото, 2–3 px на лице) | `#e0b84a` | — | — |
| Сапоги | `#3a2817` | `#1e140b` | `#5a3f28` |

## Скин 64×64 (стандартный формат игрока)
- Файл: `assets/rotp_spin/textures/entity/gyro_teacher.png` (64×64, 32-bit RGBA).
- Голова: лицо + светлые баки/волосы по бокам, золотые зубы 2 px; шляпа рисуется
  верхним слоем головы (hat layer) тёмно-коричневым с прорезями тенью.
- Торс (jacket layer): чёрная куртка, латунные заклёпки по плечам, ремень с пряжкой спереди.
- Ноги: чёрные штаны; сапоги — нижние 4 px тёмно-коричневые.
- Руки: короткие рукава (верх — куртка), ниже — кожа.

Промпт для nano banana:
```
Minecraft player skin, 64x64 pixels, standard skin layout with hat and jacket overlay
layers, transparent background where unworn, no anti-aliasing, flat pixels.
Gyro Zeppeli from Steel Ball Run: wide-brimmed dark brown hat (#4a3626, shadow #2c1f14)
with slits and silver slit goggles (#8a949e) on the crown; long blond hair (#d8c27a)
on the sides; gold teeth grill (2 gold pixels #e0b84a on the face); short-sleeved
black studded leather jacket (#2b2b30 with brass rivets #b8860b); belt with brass
buckle; black leather pants; dark brown cowboy boots. Cowboy gunslinger, 1890s.
Front view must read: hat, goggles, blond hair, black jacket, belt. No text.
```
После генерации: проверить раскладку 64×64 (голова 8×8 сверху-спереди по стандарту),
прозрачность слоёв, цвета по палитре; прогнать в игре и прислать скриншот.

## Поведение NPC (сделаю я после скина, коротко для понимания масштаба)
- Сущность `gyro_teacher` (сидит/стоит у спавна структур? просто спавн-яйцо + натуральный
  спавн в равнинах, конфиг).
- ПКМ: если нет Спина — выдаёт `rotp_spin:spin` (урок 1); дальше показывает текущий урок
  и счётчики практики; диалог — сабы `entity.rotp_spin.gyro_teacher.*` en/ru.
- Модель: гуманоид игрока со скином выше (без geo-модели — скин правится тобой за минуты).
