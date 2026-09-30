# RotP playbook — техника 1.16.5

Практические чек-листы для работы с кодом аддона. Референсы читаются из `.refs/rotp`
и `.refs/addon-example` (см. AGENTS.md) либо онлайн на GitHub.

## Карта API 1.16.5 (official mappings)
| 1.19+ | 1.16.5 |
|---|---|
| `Level` | `World` |
| `PathfinderMob` | `CreatureEntity` |
| `Vec3` | `Vector3d` |
| `Mth` | `MathHelper` |
| `SynchedEntityData` | `EntityDataManager` + `DataParameter` |
| `EntityRenderers.register` | `RenderingRegistry.registerEntityRenderingHandler` прямо в `FMLClientSetupEvent`, **не** в `enqueueWork` |
| `level.getEntitiesOfClass` | `world.getEntitiesOfClass` (то же имя; `getEntitiesWithinAABB` — это MCP, не official) |
| `FriendlyByteBuf#writeUUID` | `PacketBuffer#writeUUID` / `readUUID` (`writeUniqueId` — MCP) |

Official mappings 1.16.5 — это имена Mojang, а не MCP. Названия из старых туториалов
(`getEntitiesWithinAABB`, `writeUniqueId`, `setMotion` и т.п.) не скомпилируются.

Рендерер сущности: в Forge 36 `RenderingRegistry.loadEntityRenderers` отрабатывает сразу после
`FMLClientSetupEvent` и **до** отложенной работы, поэтому регистрация внутри `enqueueWork` теряется
(NPE в `EntityRendererManager.render`). Так же делает сам RotP — см. `ClientSetup#onFMLClientSetup`.
В `enqueueWork` оставляй только то, что не потокобезопасно (`ItemModelsProperties.register` и т.п.).

Спавн-пакет кастомной сущности: `NetworkHooks.getEntitySpawningPacket(this)`.
Регистрация: `DeferredRegister.create(ForgeRegistries.X, MODID)`.
Сомнение в сигнатуре — открой класс в `.refs/rotp`, не угадывай.

## Модели и анимации (формат Gecko, но без библиотеки GeckoLib)
RotP **не зависит от мода GeckoLib**, но с версии `1.16.5-0.2.2-pre7-snapshot-250406-a` умеет
читать файлы **в формате GeckoLib** напрямую. Это официально поддерживаемый путь импорта моделей
и анимаций стендов — раньше каждый кадр приходилось вбивать в Java-код руками.

Процесс:
1. В Blockbench поставь плагин **GeckoLib Animation Utils** и делай модель как
   *GeckoLib Animated Model* (существующую конвертируй через `File → Convert Project`).
2. Экспортируй `.geo.json` (File → Export → GeckoLib Model), `.animation.json` (вкладка Animate →
   Export Animations) и текстуру `.png`.
3. Разложи файлы:
   - `src/main/resources/assets/rotp_spin/geo/<model_id>.geo.json`
   - `src/main/resources/assets/rotp_spin/animations/<model_id>.animation.json`
   - `src/main/resources/assets/rotp_spin/textures/entity/<model_id>.png`
4. Java-класс модели остаётся (см. `ExampleStandModel.java` в ветке), но хардкод анимаций
   из него убирается.

Важные ограничения, о которых пишет автор мода:
- Фича **в разработке**: структура модели и формат анимаций ещё могут измениться.
- Нужно реализовать **все** анимации, включая обычные удары, — раньше они добавлялись автоматически.
- Иерархия частей модели отличается от обычной: она подправлена, чтобы конечности корректно
  вращались вокруг оси X.
- Требуется достаточно свежий `main_mod_version` в `gradle.properties` и библиотека Molang-выражений
  (в основном моде она уже шейдится через Gradle).

Эталон — ветка шаблона `new-model-anim-import`, там же лежит `example_stand.geo.bbmodel`:
```bash
git -C .refs/addon-example fetch origin new-model-anim-import
git -C .refs/addon-example show origin/new-model-anim-import --stat
```

Старый Java-путь (Blockbench → Modded Entity → Export Java Entity, анимации покадрово в коде)
рабочий, но устаревший: используй его только для статичных моделей или по прямой просьбе пользователя.

## A. Физика сущности (падает / телепортируется / застревает)
- `setNoGravity(true)` выставляется каждый тик в полёте; найди, где вызывается `false`.
- Движение только через `setDeltaMovement` + `move(MoverType.SELF, ...)`. Не смешивай
  с `setPos` / `teleportTo` — это и даёт «телепорт».
- Возврат к владельцу: коллизии с блоками отключены, с сущностями сохранены,
  `canBeCollidedWith() == false`.
- Рассинхрон: проверь спавн-пакет, `DataParameter`-флаги и что клиент не пересчитывает физику сам.
- Сравнивай с метательными сущностями RotP (`entity.itemprojectile.ItemNbtProjectileEntity` —
  база нашего `SteelBallEntity`, как у `BladeHatEntity`; `entity.damaging.projectile.ModdedProjectileEntity`)
  и с ванильными `AbstractArrowEntity`, `TridentEntity` (возврат по Loyalty), `SnowballEntity`.

## B. NBT и синхронизация
- ItemStack: `stack.getOrCreateTag().putInt(...)`; отображение — `Item#appendHoverText`.
- Entity: `addAdditionalSaveData` / `readAdditionalSaveData`; частое состояние —
  `EntityDataManager.defineId` + `DataParameter`.
- Каждый тик через NBT синхронизировать нельзя: либо `DataParameter`, либо свой пакет
  в собственном `SimpleChannel` (не вмешивайся в канал RotP).
- UUID — `PacketBuffer#writeUUID` / `readUUID`, не строкой.
- Свой канал уже есть: `network.AddonPackets` (s2c-пакет `SpinEnergySyncPacket`). Новые пакеты
  регистрируй там же с `NetworkDirection`; клиентское состояние держи в классе без клиентских
  импортов (как `ClientSpinState`), чтобы хендлер пакета не ронял сервер.

## C. Новая способность
- Наследник `Ability` из RotP; переопредели старт, активный тик, конец, условие активности
  и требование по Resolve — точные сигнатуры смотри в RotP.
- AOE — `world.getEntitiesOfClass(Class, AxisAlignedBB, Predicate)`, радиус из конфига.

## C2. Клавиши
- Образец — `com.github.standobyte.jojo.client.InputHandler` в RotP: `new KeyBinding(MOD_ID + ".key.x",
  GLFW_KEY_..., category)` + `ClientRegistry.registerKeyBinding(...)` в `FMLClientSetupEvent`.
- Обработка — в `TickEvent.ClientTickEvent` через `consumeClick()`; на сервер уходит c2s-пакет,
  вся логика и проверки (энергия, кулдаун, предмет в руке) — только на сервере.
- Дефолтная клавиша не должна пересекаться с RotP (`M`, `O`, `H`, `J`, `K`, `V`, `B`, `\`, `Left Alt`).
- Локализация: `key.rotp_spin.*` и категория `key.categories.rotp_spin` в `en_us`/`ru_ru`.
- Обязательны: лорная слабость или цена, локализации `en_us`/`ru_ru`, иконка, регистрация
  в `DeferredRegister`.
- Capability энергии Спина — по образцу ветки `player-capability`.
- **Спин — не-стендовая сила RotP** `rotp_spin:spin` (`power.SpinPowerType`, образец — `ZombiePowerType`),
  регистрируется в `InitPowers.NON_STAND_POWERS`; выдаётся `/jojopower give <игрок> rotp_spin:spin`.
  Совместима с любым стендом, несовместима с Хамоном/Вампиризмом (у игрока одна не-стендовая сила).
  Проверка владения — `SpinPowerType.hasSpin(entity)`.
- Энергия: источник истины — `SpinPowerCapability`; RotP-энергия лишь зеркалит её
  (`tickEnergy`: сервер — капабилити, клиент — `ClientSpinState`; `consumeEnergy` списывает с капабилити).
  Поэтому `/jojoenergy` и `setEnergy` RotP на Спин не действуют — меняй энергию через капабилити
  или командой `/spinenergy set <игроки> <число> [points|ratio]` / `/spinenergy get <игрок>`
  (`command.SpinEnergyCommand`). Шкалу энергии рисует сам RotP — своего HUD у аддона нет.
- Способности Спина — наследники `NonStandAction` в пакете `action`, регистрируются в общем
  `InitStands.ACTIONS`, в хотбар — через массивы `attacks`/`abilities` конструктора `SpinPowerType`
  (раскладка в `InitPowers`, образец — `ModZombieActions`). Урок 1 — встроенный прыжок RotP `isLeapUnlocked`.
  - ЛКМ: `spin_ball_throw` (урок 1; также быстрый доступ, средняя кнопка), `spin_muscle_hijack` (урок 2),
    `spin_item_throw` (урок 3, сущность `entity.SpunItemEntity`), `spin_block_throw` (урок 3:
    вырывает целевой блок и бросает как грубый шар, сущность `entity.SpunBlockEntity`, правил
    выбора — `entity.SpinBlock`, JUnit; возврат и Golden-бонус только у идеальной сферы).
  - ПКМ (удержание): `spin_ball_steer` (урок 3), `spin_healing` (урок 2; «рентген» — вода в радиусе
    `zeppeli_healing.xrayWaterRadius` от пациента даёт диагноз и множитель лечения), `spin_golden_frame`
    (урок 4, пустые руки: сложенный руками золотой прямоугольник калибрует Golden Spin на
    `golden_spin.hand_frame.durationTicks`, срок — `SpinData` NBT `HandFrameUntil`; удар срывает складывание).
    Снегопад над бросающим тоже калибрует (`golden_spin.snowfallCalibrates`).
    `spin_body_brace` (урок 1, Спин на своём теле): после `body_brace.windupTicks` тело жёсткое —
    `power.SpinBraceHandler` (`LivingHurtEvent`) снимает долю кинетического удара (снаряд, взрыв, ближний
    бой; не огонь/магия/bypassArmor) за энергию и отбрасывает атакующего в упор; математика —
    `power.SpinBrace` (JUnit, потолок 0.95: не неуязвимость). Цена — энергия за тик, за поглощённый
    урон и замедление; удар стойку не срывает.
- Уроки: `power.SpinData` хранит урок и счётчики практики (NBT `Lesson`, `BallHits`, `Hijacks`, `GoldenHits`),
  `SpinData#isActionUnlocked` (хук RotP `TypeSpecificData`) закрывает действия старших уроков;
  соответствие действие → урок — `SpinData#requiredLesson` (действия только до урока 3; уроки 4–5 —
  пассивный бонус Golden/Super Spin к урону шара, `power.SpinGolden`, JUnit), логика перехода —
  `power.SpinLessons` (JUnit).
  Урок 2: попадания вращающимся шаром по существам (`SteelBallEntity#hurtTarget`), урок 3: успешные
  перехваты мышц, уроки 4–5: попадания вращающимся шаром на уроке 3+ (`SpinData#onGoldenHit`).
  Бонус применяется в конструкторе `SteelBallEntity`: урок 4 — при калибровке (живой биом не из
  `golden_spin.deadBiomeCategories` или `calibration_buckle` в инвентаре), урок 5 — везде;
  сколотый шар сохраняет лишь долю бонуса (`golden_spin.chippedRetention` — в манге Ball Breaker
  с повреждённым шаром был неполноценным).
  Урок 2: попадания вращающимся шаром по существам (`SteelBallEntity#hurtTarget`), урок 3: успешные
  перехваты мышц. Клиенту уходит «действующий» урок (`SpinLessonSyncPacket` → `ClientSpinState`),
  т.к. COMMON-конфиг (`lessons.enabled`) на клиент не синхронизируется. Для тестов — `/spinlesson set|get`.
- Рикошет шара: `SteelBallEntity#onHitBlock` отражает скорость от грани (`entity.SpinRicochet`, JUnit),
  лимит отскоков и потеря скорости — `steel_ball.ricochet*`; после лимита шар ложится и возвращается как раньше.
- Ремонт повреждённого шара — наковальня (`item.SteelBallRepair`, `AnvilUpdateEvent`), материал из конфига.
- Числа действий не передаются в `Builder` (конфиг ещё не загружен при регистрации): переопределяй
  `getEnergyCost`, `getHeldTickEnergyCost`, `getCooldownAdditional`, `getMaxRangeSqEntityTarget`
  и читай `SpinConfig` в момент вызова.
- Ключи: название — `action.rotp_spin.<id>`; сообщения условий — `jojo.message.action_condition.<postfix>`
  (`conditionMessage("rotp_spin.xxx")`), нехватка энергии — `jojo.message.action_condition.no_energy_spin`.
  Иконка — `textures/action/<id>.png` 32×32 (ТЗ: `docs/art/action_<id>.md`, индекс всех текстур — `docs/art/README.md`).
- Удерживаемое действие: логику пиши в `holdTick` только на сервере; `NonStandAction#onHoldTick` сам
  списывает `getHeldTickEnergyCost` каждый тик, пока условия выполнены. Движение своей сущности от
  удерживаемого действия синхронизируй через `hurtMarked = true` (иначе пакет скорости уйдёт раз в `updateInterval`).
- Прыжок RotP (`InputHandler#onInputUpdate`) срабатывает только у силы, чей режим HUD открыт
  (`ActionsOverlayGui#getCurrentPower`). Чтобы прыжок Спина работал при закрытом HUD и в режиме стенда,
  `client.SpinLeapInput` (приоритет LOWEST, после RotP) повторяет тот же путь: `MCUtil.leap` +
  `ClOnLeapPacket(NON_STAND)`. Серверную проверку, списание энергии и кулдаун делает RotP.
- Падение: RotP (`GameplayEventHandler#onLivingFall`, LOW) прощает любой силе с прыжком
  `(leapStrength + 5) * 3` блоков (у Спина ≈ 19). `power.SpinFallHandler` (LOWEST, строго после RotP)
  убирает этот бонус
  для Спина и ставит `spin_leap.fallDistanceReduction` (по умолчанию 0); бонус прыгающего стенда
  и изменения других модов сохраняются. Исходная высота — `entity.fallDistance` (обнуляется после события).

## D. Предметы аддона
- **`steel_ball`** — бросок с возвратом в руку; NBT-флаг «повреждён», влияющий на Golden Spin
  и Ball Breaker.
- **`gyros_holster`** (реализован) — тяжёлый кожаный ремень с двумя открытыми боковыми кобурами.
  NBT: список `Balls` из сохранённых `ItemStack` (флаг «повреждён» у каждого шара сохраняется),
  вместимость — `holster.capacity`. ПКМ заряжает шары из инвентаря, Shift+ПКМ достаёт; клавиша
  `key.rotp_spin.holster_throw` (по умолчанию `R`) бросает шар из кобуры через c2s `HolsterThrowPacket`.
  Без Curios кобура работает из **любого слота инвентаря** (`InventoryHolsterAccess`). С Curios
  (реализовано, см. `docs/integrations.md`) сначала ищется в слоте `belt` (`compat.curios.CuriosHolsterAccess`),
  затем в инвентаре. Реализация выбирается в `AddonMain#commonSetup` через `HolsterAccess.set(...)`.
  Отрисовка на игроке — только в слоте Curios `belt`: `compat.curios.CuriosCompat#init` (конструктор мода)
  вешает на стак кобуры `ICurio` (`HolsterCurio`), его `render` зовёт клиентский
  `client.render.GyrosHolsterRender` → `GyrosHolsterModel` (геометрия из `docs/art/gyros_holster.bbmodel`,
  текстура `textures/entity/gyros_holster.png` 64×64). Кость `body` копирует позу торса игрока,
  шары видны по числу шаров в кобуре. ТЗ — `docs/art/gyros_holster.md`.
- **`calibration_buckle`** — латунная пряжка 1:1.618 с гравировкой Золотой спирали; включает
  Golden Spin в биомах без природных маркеров. Текстура 16×16, золотой прямоугольник 10×16 px.

## E. Тесты
- Чистая логика (математика полёта, счётчики, NBT-хелперы) выносится в классы без `World`
  и покрывается JUnit.
- `test { useJUnitPlatform() }`, JUnit 5.8.2, Mockito 4.11.0.
- Для всего, что требует мира, пиши текстовый сценарий ручной проверки в `runClient`.
