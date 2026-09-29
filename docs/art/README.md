# Арт аддона: все текстуры и их ТЗ

Одна текстура — один файл ТЗ. Новая текстура появляется в коде, значит, здесь появляется строка
и отдельный файл `docs/art/<имя>.md`. PNG рисует художник; в `src` кладутся только готовые файлы.

| Текстура (`src/main/resources/assets/rotp_spin/…`) | Размер | ТЗ | Статус |
|---|---|---|---|
| `textures/item/steel_ball.png` | 16×16 | [steel_ball.md](steel_ball.md) | ✅ есть (по референсам: зелёный металл + шестиугольная пластина + прорези; генерируется `tools/gen_steel_ball.py`) |
| `textures/entity/steel_ball_wrapped.png` (3D-шар в полёте) | 256×128 | [steel_ball.md](steel_ball.md) | ✅ есть (тот же скрипт, проверить в игре) |
| `textures/item/gyros_holster.png` | 32×32 | [gyros_holster.md](gyros_holster.md) §1 | ✅ есть |
| `textures/entity/gyros_holster.png` (модель на игроке) | 64×64 | [gyros_holster.md](gyros_holster.md) §2, этап 3 | ✅ есть, ждёт проверки в игре |
| `textures/power/spin.png` | 32×32 | [spin_power_icon.md](spin_power_icon.md) | ✅ есть |
| `textures/action/spin_ball_throw.png` | 32×32 | [action_spin_ball_throw.md](action_spin_ball_throw.md) | ✅ есть |
| `textures/action/spin_muscle_hijack.png` | 32×32 | [action_spin_muscle_hijack.md](action_spin_muscle_hijack.md) | ✅ есть |
| `textures/action/spin_ball_steer.png` | 32×32 | [action_spin_ball_steer.md](action_spin_ball_steer.md) | ✅ есть |
| `textures/action/spin_healing.png` | 32×32 | [action_spin_healing.md](action_spin_healing.md) | ✅ есть |
| `textures/action/spin_item_throw.png` | 32×32 | [action_spin_item_throw.md](action_spin_item_throw.md) | ✅ есть |
| `textures/action/spin_block_throw.png` | 32×32 | [action_spin_block_throw.md](action_spin_block_throw.md) | ✅ есть |
| `textures/item/calibration_buckle.png` | 16×16 | [calibration_buckle.md](calibration_buckle.md) | ✅ есть |
| `textures/item/wrecking_ball.png` | 16×16 | [wrecking_ball.md](wrecking_ball.md) | ✅ есть (по референсам: медь + оранжевые борозды + золотые сателлиты; генерируется `tools/gen_wrecking_ball_icon.py`) |
| `textures/entity/ball_breaker.png` | 128×128 | [ball_breaker.md](ball_breaker.md) | 🟡 модель по референсам (52 куба) собрана, ждёт проверки в Blockbench; текстура — плейсхолдер `ball_breaker_placeholder.png`, финал рисует художник |
| `textures/entity/gyro_teacher.png` | 64×64 (скин) | [gyro_teacher.md](gyro_teacher.md) | ✅ есть (переделан по референсам: аниме-наряд SBR; генерируется `tools/gen_gyro_teacher_skin.py`, проверить в игре) |

Вращающийся предмет (`rotp_spin:spun_item`) рисуется иконкой самого брошенного предмета — своей текстуры
не нужно.

Исходники моделей (в jar не попадают): `gyros_holster.bbmodel`, `ball_breaker.bbmodel`
(генерируется скриптом `tools/gen_ball_breaker.py`: правки геометрии вносим в скрипт и перегенерируем). Геометрия в коде
(`client/render/GyrosHolsterModel`) снята с `.bbmodel`, а не с `GyrosHolsterModel.java.txt`:
в `.txt` ремень и кобуры смещены на 2 px вниз относительно `.bbmodel`.
