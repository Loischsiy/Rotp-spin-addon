# Интеграции с другими модами

Общее правило: наш аддон **самодостаточен**. Любой другой мод — опциональная интеграция,
которая включается сама, если мод установлен, и молча отключается, если его нет.

## Идентификаторы
| Мод | modid |
|---|---|
| Ripples of the Past (обязательный) | `jojo` |
| RotP Tusk Stand Addon (опциональный) | `rotp_t` |
| Curios API (опциональный, реализован) | `curios` |
| Наш аддон | `rotp_spin` |

Tusk-аддон: https://github.com/Yarost228/RotpTuskAddon, пакет `com.doggys_tilt.rotp_t`,
зависит от `jojo` версии `[0.2.2,0.3)`, лицензия GPL-3.0.

## Что интегрируем
**Golden Spin для Tusk** (урок 4 Джайро: «Вращай пули по золотому сечению» → Tusk ACT2 и далее).
Если установлен `rotp_t`, освоенный игроком Золотой Спин должен усиливать выстрелы ногтями Tusk
и открывать переход к следующему акту по лору. Без `rotp_t` эта способность просто не существует:
ни в списке способностей, ни в конфиге, ни в логах ошибок.

## Как реализовать (обязательный паттерн)
1. **`mods.toml`** — зависимость с `mandatory=false` и порядком загрузки:
```toml
[[dependencies.rotp_spin]]
    modId="rotp_t"
    mandatory=false
    versionRange="[0,)"
    ordering="AFTER"
    side="BOTH"
```
2. **`build.gradle`** — Tusk-аддон подключается как `compileOnly fg.deobf(...)` (или через
   локальный jar в `libs/`), но **никогда** как `implementation`: иначе он попадёт в рантайм-зависимости.
3. **Изоляция классов.** Весь код, который упоминает классы `rotp_t`, живёт в отдельном пакете
   `com.<you>.rotpspin.compat.tusk` и **нигде больше не импортируется напрямую**.
   Точка входа — интерфейс в основном коде:
```java
public interface ITuskCompat {
    boolean isActive();
    void applyGoldenSpin(LivingEntity user, float power);
    ITuskCompat NOOP = /* пустая реализация */;
}
```
4. **Ленивая загрузка.** В `FMLCommonSetupEvent`:
```java
tuskCompat = ModList.get().isLoaded("rotp_t")
        ? new TuskCompatImpl()   // класс грузится только здесь
        : ITuskCompat.NOOP;
```
   `TuskCompatImpl` нельзя упоминать в статических полях классов, которые грузятся всегда, иначе
   получишь `NoClassDefFoundError` без Tusk-аддона.
5. **Где можно — обходись без классов Tusk вообще.** Обращайся к сущностям и способностям через
   реестры и `ResourceLocation("rotp_t", "...")`: такой код безопасен даже без изоляции.
6. **Регистрация способности.** `tusk_golden_spin` регистрируется в `DeferredRegister` условно —
   только если `rotp_t` загружен. Плюс ключ в конфиге `compat.tusk.enabled` (по умолчанию `true`),
   чтобы игрок мог выключить интеграцию вручную.
7. **Локализация и подсказки.** Строки `rotp_spin.ability.tusk_golden_spin.*` в `en_us` и `ru_ru`.
   Если `rotp_t` нет — способность не показывается, а не показывается «серой».

## Тестирование интеграции
- [ ] Игра запускается **без** `rotp_t`: нет краша, нет `NoClassDefFoundError`, способности нет в списке.
- [ ] Игра запускается **с** `rotp_t`: способность есть и работает.
- [ ] Выключение через конфиг отключает способность без перезапуска мира.
- [ ] Сохранение мира, сделанное с `rotp_t`, открывается после его удаления без потери прогресса Спина.
- [ ] Порядок загрузки: наш мод грузится после `jojo` и после `rotp_t`.

## Curios: кобура в слоте `belt` (реализовано)
- `build.gradle`: API — `compileOnly fg.deobf(".../curios-forge:${curios_version}:api")`, полный мод —
  `runtimeOnly` (только для `runClient`); в jar не попадает. Версия — `gradle.properties/curios_version`.
- `mods.toml`: `curios`, `mandatory=false`, `ordering="AFTER"`.
- Слот: IMC `SlotTypePreset.BELT` в `InterModEnqueueEvent`; предмет — тег `data/curios/tags/items/belt.json`
  (без Curios тег просто не используется).
- Код: только `compat.curios.*`; ядро знает лишь `IHolsterAccess` / `HolsterAccess`. `AddonMain` проверяет
  `ModList.get().isLoaded("curios")` строковым литералом, не трогая классы compat.
- Конфиг `compat.curios.enabled` проверяется при каждом поиске кобуры — выключается без перезапуска.

Ручная проверка:
- [ ] Без Curios: игра запускается, нет `NoClassDefFoundError`, кобура работает из инвентаря.
- [ ] С Curios: кобура надевается в слот «Пояс», `R` бросает из неё, вернувшийся шар ложится туда же,
      полоска энергии видна.
- [ ] `compat.curios.enabled=false`: кобура в поясе игнорируется, из инвентаря работает.

## Добавление новой интеграции
Тот же паттерн: интерфейс в ядре + `NOOP` + реализация в `compat.<modid>` + `mandatory=false`
в `mods.toml` + `compileOnly` в Gradle + ключ конфига + тест «без мода / с модом».
