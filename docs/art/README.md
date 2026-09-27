# Арт аддона: все текстуры и их ТЗ

Одна текстура — один файл ТЗ. Новая текстура появляется в коде, значит, здесь появляется строка
и отдельный файл `docs/art/<имя>.md`. PNG рисует художник; в `src` кладутся только готовые файлы.

| Текстура (`src/main/resources/assets/rotp_spin/…`) | Размер | ТЗ | Статус |
|---|---|---|---|
| `textures/item/steel_ball.png` | 16×16 | — (сделана до ТЗ) | ✅ есть |
| `textures/item/gyros_holster.png` | 32×32 | [gyros_holster.md](gyros_holster.md) §1 | ✅ есть |
| `textures/entity/gyros_holster.png` (модель на игроке) | 64×64 | [gyros_holster.md](gyros_holster.md) §2, этап 3 | ✅ есть, ждёт проверки в игре |
| `textures/power/spin.png` | 32×32 | [spin_power_icon.md](spin_power_icon.md) | ✅ есть |
| `textures/action/spin_ball_throw.png` | 32×32 | [action_spin_ball_throw.md](action_spin_ball_throw.md) | ❌ нужна |
| `textures/action/spin_muscle_hijack.png` | 32×32 | [action_spin_muscle_hijack.md](action_spin_muscle_hijack.md) | ❌ нужна |
| `textures/action/spin_ball_steer.png` | 32×32 | [action_spin_ball_steer.md](action_spin_ball_steer.md) | ❌ нужна |
| `textures/action/spin_healing.png` | 32×32 | [action_spin_healing.md](action_spin_healing.md) | ❌ нужна |
| `textures/action/spin_item_throw.png` | 32×32 | [action_spin_item_throw.md](action_spin_item_throw.md) | ❌ нужна |

Вращающийся предмет (`rotp_spin:spun_item`) рисуется иконкой самого брошенного предмета — своей текстуры
не нужно.

Исходники моделей (в jar не попадают): `gyros_holster.bbmodel`. Геометрия в коде
(`client/render/GyrosHolsterModel`) снята с `.bbmodel`, а не с `GyrosHolsterModel.java.txt`:
в `.txt` ремень и кобуры смещены на 2 px вниз относительно `.bbmodel`.
