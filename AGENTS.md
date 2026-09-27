# AGENTS.md — RotP Spin Addon

Аддон на Спин (Spin) из JoJo's Bizarre Adventure Part 7/8 к моду **Ripples of the Past (RotP)**
(modid основного мода — `jojo`).
Minecraft **1.16.5**, Forge, **Java 8**. Всё происходит в этой папке — абсолютных путей нет.

## Правила ответа
- Отвечай пользователю по-русски. Код, имена классов, коммиты — по-английски.
- Перед реализацией любой механики Спина прочитай `docs/spin-lore.md` и пометь фичу
  ✅ КАНОН / ⚠️ ДОПУЩЕНИЕ / 🔴 OOC. 🔴 не реализуем — предлагай лорную альтернативу.
- Перед работой с кодом RotP (Ability, Capability, сущности, рендер, пакеты) прочитай
  `docs/rotp-playbook.md`.
- Перед любой работой с моделью или анимацией стенда прочитай `docs/model-guide.md`
  и строго соблюдай его этапы: геометрия «на глаз» из головы запрещена.
- Перед работой с чужими модами прочитай `docs/integrations.md`. Наш аддон работает
  самостоятельно; любая интеграция — опциональная и отключается сама, если мода нет.

## Обзор проекта
- `modid`: `rotp_spin`; исходники в `src/main/java`, ресурсы в `src/main/resources`,
  датаген пишет в `src/generated/resources`.
- Проект создаётся **из шаблона RotP-Addon-example**, а не из Forge MDK: в нём уже настроены
  RotP-maven, зависимость на основной мод и правильные маппинги.
- Лицензия GPL-3.0 (как у RotP и шаблона); копирайты не удалять.

## Bootstrap (если папка ещё пустая)
```bash
git clone --depth 1 https://github.com/StandoByte/RotP-Addon-example.git .tmp-template \
  && rsync -a --exclude .git .tmp-template/ . && rm -rf .tmp-template
```
Затем переименуй `modid`, package, `archivesBaseName`, `mods.toml`, `pack.mcmeta`.
`build.gradle` в остальном не трогай.

## Сборка и тесты
**JDK 8 лежит прямо в этой папке**, в каталоге `openjdk8`. Внешний Java не используй.
Перед каждой Gradle-командой активируй его:
```bash
export JAVA_HOME="$PWD/openjdk8/Contents/Home" && export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build          # сборка
./gradlew test           # JUnit
./gradlew runClient      # ручная проверка в игре
```
- Путь `Contents/Home` — раскладка macOS-бандла. Если внутри `openjdk8` сразу лежит `bin/java`,
  то `export JAVA_HOME="$PWD/openjdk8"`. Проверь раскладку один раз: `ls openjdk8`.
- JDK строго 8. После экспорта проверяй: `java -version` должно показать `1.8`.
- Каталог `openjdk8/` добавь в `.gitignore` — бинарники в репозиторий не коммитим.
- Версии зафиксированы шаблоном: Forge `1.16.5-36.2.34`, ForgeGradle `5.1.+`,
  mappings `channel: 'official', version: '1.16.5'`.
- Версия основного мода — только через `gradle.properties/main_mod_version`
  (доступные: https://github.com/StandoByte/RotP-maven/commits/main).
- Тесты: JUnit 5.8.2 + Mockito **4.11.0**. Mockito 5 требует Java 11 и на JDK 8 не соберётся.

## Референсы (скачивай сам по мере надобности)
Держи их в `.refs/` — она в `.gitignore` и в репозиторий не попадает.
```bash
mkdir -p .refs
git clone --depth 1 -b 1.16.5 https://github.com/StandoByte/Ripples-of-the-Past.git .refs/rotp
git clone --depth 1 https://github.com/StandoByte/RotP-Addon-example.git .refs/addon-example
```
- Основной мод: https://github.com/StandoByte/Ripples-of-the-Past (ветка `1.16.5`)
- Шаблон аддона: https://github.com/StandoByte/RotP-Addon-example
- Полезные ветки шаблона: `new-model-anim-import` (модели и анимации), `player-capability`
  (своя Capability игрока), `mixins`.
- Если сети нет или нужен один файл — смотри те же файлы онлайн на GitHub, не выдумывай API.
- Другие рабочие аддоны, вики мода и список практик, которые нельзя тащить в проект, —
  в `docs/resources.md`. Готового туториала по аддонам к RotP не существует: учимся по шаблону
  и по коду живых аддонов.
- `.refs/` — **только чтение**. Никогда не редактируй и не коммить её содержимое.

## Соглашения по коду
- Целевой API — Minecraft 1.16.5, official mappings. Не переноси код из 1.19+.
  Ориентиры: `World`, `CreatureEntity`, `Vector3d`, `MathHelper`, `EntityDataManager` + `DataParameter`,
  `NetworkHooks.getEntitySpawningPacket(this)`, `RenderingRegistry.registerEntityRenderingHandler(...)`
  в `FMLClientSetupEvent#enqueueWork`, `DeferredRegister.create(ForgeRegistries.X, MODID)`.
  Сомневаешься в сигнатуре — открой класс в `.refs/rotp` или в ванилле и скопируй точно.
- **GeckoLib как библиотека НЕ подключается**, но **формат файлов GeckoLib используется**:
  RotP сам читает `.geo.json` и `.animation.json` (Molang-выражения — через `mocha`).
  Модель в Blockbench делается плагином GeckoLib Animation Utils, файлы кладутся в
  `assets/rotp_spin/geo/` и `assets/rotp_spin/animations/`, Java-класс модели остаётся,
  но без хардкода анимаций. Подробности и ограничения — `docs/rotp-playbook.md`.
- Способности и стенды — через API RotP (его `Ability`, Resolve, стенд-капабилити), без дублирования.
  Энергия Спина — отдельная `SpinPowerCapability` со своим `ResourceLocation`.
- Никакого хардкода чисел (урон, дальность, кулдауны, радиусы) — всё в `ForgeConfigSpec`.
- `@OnlyIn(Dist.CLIENT)` — только на клиентском коде, никогда на тик-логике сущности или способности.
- Терминология: кинетика, вращение, микровибрации, золотое сечение. Запрещены «магия», «мана»,
  «заклинание».
- Каждый новый предмет и способность: локализации `en_us` и `ru_ru` + иконка.
- Опциональные моды (например, Tusk-аддон `rotp_t`) подключаются только по паттерну из
  `docs/integrations.md`: `mandatory=false`, `compileOnly`, изолированный пакет `compat.*`.
- PNG-текстуры ты не рисуешь: выдавай JSON-модель (`parent: item/generated`) и ТЗ художнику
  (размер, палитра в hex, силуэт, детали, 32-bit RGBA, без потерь).

## Рабочий цикл
1. Разведка: grep по проекту и по `.refs/`; найди готовый аналог, прежде чем писать своё.
2. Лор-чек по `docs/spin-lore.md`.
3. План изменений; если задача затрагивает больше 3 файлов — сначала покажи план и дождись «ок».
4. Минимальные точечные диффы.
5. `./gradlew build`, затем `./gradlew test`; ошибки компиляции чинишь сам.
6. Коммит в стиле Conventional Commits, одним осмысленным сообщением.

## Формат ответа на задачу
```
## Задача
### Анализ            — что найдено в коде, какие файлы-референсы прочитаны
### Лор                — ✅/⚠️/🔴 + пункт docs/spin-lore.md
### Совместимость      — какие API RotP использованы и по какому образцу
### Изменения          — путь к файлу: что и почему
### Конфиг             — новые ключи
### Проверка           — команда сборки, JUnit, сценарий в игре
### Риски и что осталось
```

## Анти-паттерны
- ❌ API 1.19+ или GeckoLib-синтаксис в этом проекте.
- ❌ Mockito 5 или запуск Gradle не на JDK 8.
- ❌ Правки в `.refs/` или коммит `.refs/`.
- ❌ Хардкод чисел, «магическая» терминология, PNG «из головы».
- ❌ Реализация фичи до лор-чека и до чтения аналога в RotP.
- ❌ Свой велосипед там, где в шаблоне есть готовая ветка.

## Шаблон задачи от пользователя
```
ЗАДАЧА:        <что сделать>
СИМПТОМ/ЦЕЛЬ:  <как проявляется баг или что должно получиться>
ЛОР-ОСНОВА:    <урок Джайро / способность / «на твоё усмотрение»>
ГРАНИЦЫ:       <какие файлы можно трогать>
ГОТОВО КОГДА:  <критерий приёмки>
```

## Headless-тестирование клиента

Используй `mcx` (см. /srv/minecraft-agent/MCX.md): `mcx start`, `mcx launch ./gradlew runClient --offline`, `mcx wait-window`, `mcx fit`, `mcx newworld`, `mcx key/hold/type/click/clickr`, `mcx shot`, `mcx rec`, `mcx stop`. Не запускай Xvfb вручную и не используй `pkill -f` (вместо него `mcx kill`).
