# Подключение PostgreSQL

REST API запускается с профилем `memory` по умолчанию. Профиль `postgres` используется для проверки подключения и применения миграций через Spring Boot без веб-сервера. Перенос хранения REST API в PostgreSQL описан в [плане интеграции](postgresql-integration-backlog.md).

## Профили Spring

| Профиль | Конфигурация |
|---|---|
| `memory` | Регистрирует четыре репозитория в памяти и `DemoDataInitializer`. Flyway выключен, автоконфигурация `DataSource` исключена. `MeetingRoomBookingApplicationTests` явно запускается с этим профилем. |
| `postgres` | Получает параметры `DataSource` из окружения. Flyway ищет миграции в `classpath:db/migration`. Встроенная SQL-инициализация отключена. Для Hibernate заданы `ddl-auto=validate` и `open-in-view=false`. |
| `postgres,postgres-dev` | Загружает основную конфигурацию `postgres` и указывает Flyway обе папки: `classpath:db/migration` и `classpath:db/dev-migration`. |

Версии зависимостей управляются Spring Boot `4.1.2`. В дереве Maven разрешились Hibernate `7.4.13.Final`, Flyway Core и поддержка PostgreSQL `12.4.0`, JDBC-драйвер PostgreSQL `42.7.14`.

## Локальные базы

Сервер PostgreSQL `18.7` из Postgres.app доступен на `localhost:5432`. Для Spring Boot подготовлены две базы со схемой `public`.

| База | Назначение |
|---|---|
| `booking_app_schema_check` | Проверка применения схемы с нуля. |
| `booking_app` | Схема V1 и демонстрационные данные V2. |

## Проверка подключения

Из корня репозитория задай параметры в окружении процесса. В примере используется системное имя пользователя Postgres.app. Если сервер требует пароль, передай его через `SPRING_DATASOURCE_PASSWORD`.

```bash
export BOOKING_EXPECTED_DATABASE=booking_app_schema_check
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/$BOOKING_EXPECTED_DATABASE"
export SPRING_DATASOURCE_USERNAME="$(id -un)"
SPRING_FLYWAY_ENABLED=false ./mvnw -Dtest=PostgresMigrationSmoke -Dspring.profiles.active=postgres test
```

Для второй базы подставь `booking_app` и снова задай JDBC URL. `PostgresMigrationSmoke` открывает отдельный Spring-контекст без веб-сервера и сравнивает ответ `SELECT current_database()` со значением `BOOKING_EXPECTED_DATABASE`. С `SPRING_FLYWAY_ENABLED=false` проверяется только подключение JDBC. Обычная сборка через `./mvnw clean verify` запускает тесты без доступа к PostgreSQL.

## Применение схемы Flyway

Подключись к `booking_app_schema_check` и проверь целевую БД через `SELECT current_database()`. Для первой миграции используй пустую схему `public`. С параметрами подключения из предыдущего раздела выполни:

```bash
SPRING_FLYWAY_ENABLED=true ./mvnw -Dtest=PostgresMigrationSmoke -Dspring.profiles.active=postgres test
```

[Миграция V1](../src/main/resources/db/migration/V1__create_booking_schema.sql) создаёт таблицы `rooms`, `users`, `time_slots` и `bookings`. В схеме есть внешние ключи, ограничения часового слота и уникальная пара `(room_id, starts_at)`. Частичный уникальный индекс `bookings_one_active_per_slot_idx` гарантирует максимум одну `ACTIVE`-бронь на слот. Для новой брони PostgreSQL подставляет `ACTIVE` и `CURRENT_TIMESTAMP`, если соответствующие колонки пропущены при вставке.

После первого и повторного запуска проверь историю:

```sql
SELECT installed_rank, version, checksum, success
FROM public.flyway_schema_history
ORDER BY installed_rank;
```

При повторном запуске Flyway сверяет контрольную сумму и сообщает `Schema "public" is up to date. No migration necessary.`

## Подтверждённые проверки

- `./mvnw -o clean verify` без параметров подключения к БД — `BUILD SUCCESS`, 14 тестов, 0 падений, ошибок и пропусков.
- `PostgresMigrationSmoke` с отключённым Flyway прошёл отдельно для `booking_app_schema_check` и `booking_app`. PostgreSQL вернул ожидаемые имена обеих баз.
- V1 применена к пустой `booking_app_schema_check`. Проверены ограничения слотов и броней, внешние ключи, значения по умолчанию и частичный уникальный индекс.
- Повторный запуск прошёл с одним успешным smoke-тестом. В `flyway_schema_history` одна строка: `installed_rank=1`, `version=1`, `checksum=1327921915`, `success=true`. Во всех четырёх прикладных таблицах `0` строк.

## Демонстрационные данные Flyway

[Миграция V2](../src/main/resources/db/dev-migration/V2__insert_demo_data.sql) добавляет 4 комнаты, 3 пользователей, 8 слотов и 6 броней. UUID, связи, роли, статусы и даты соответствуют [демонстрационному набору в памяти](../src/main/java/com/learning/booking/config/DemoDataInitializer.java).

Перед первым запуском проверь через `SELECT current_database()`, что подключение открыто к пустой `booking_app`. Из корня репозитория выполни:

```bash
SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/booking_app' \
SPRING_DATASOURCE_USERNAME="$(id -un)" \
BOOKING_EXPECTED_DATABASE='booking_app' \
SPRING_FLYWAY_ENABLED=true \
./mvnw -Dtest=PostgresMigrationSmoke -Dspring.profiles.active=postgres,postgres-dev test
```

На пустой `booking_app` Flyway применил V1 и V2. Повторный запуск с той же командой подтвердил обе миграции и сообщил `No migration necessary`. Smoke-тест прошёл. Запрос к `flyway_schema_history` из раздела выше вернул две успешные строки.

| `installed_rank` | `version` | `checksum` | `success` |
|---:|---:|---:|---|
| 1 | 1 | 1327921915 | `true` |
| 2 | 2 | 397731764 | `true` |

### Проверка демонстрационных данных

Выполняй запросы в `booking_app` после применения миграций. Количества записей — `4 / 3 / 8 / 6`. История Anna содержит брони `…0003 CANCELLED`, `…0002 CANCELLED`, `…0001 ACTIVE`, все с `created_at = 2026-10-01T09:01:00Z`. Слотов с несколькими `ACTIVE` нет. Для Orion в периоде `[09:00Z, 11:00Z)` 14 января 2030 года слот на `09:00Z` имеет `ACTIVE`, а слот на `10:00Z` свободен.

```sql
SELECT
    (SELECT count(*) FROM public.rooms) AS rooms,
    (SELECT count(*) FROM public.users) AS users,
    (SELECT count(*) FROM public.time_slots) AS time_slots,
    (SELECT count(*) FROM public.bookings) AS bookings;

SELECT b.id, b.status, b.created_at AT TIME ZONE 'UTC' AS created_at_utc
FROM public.bookings b
JOIN public.users u ON u.id = b.user_id
WHERE u.email = 'anna@example.com'
ORDER BY b.created_at DESC, b.id DESC;

SELECT slot_id, count(*) AS active_count
FROM public.bookings
WHERE status = 'ACTIVE'
GROUP BY slot_id
HAVING count(*) > 1;

SELECT s.starts_at AT TIME ZONE 'UTC' AS starts_at_utc,
       EXISTS (
           SELECT 1
           FROM public.bookings b
           WHERE b.slot_id = s.id AND b.status = 'ACTIVE'
       ) AS has_active_booking
FROM public.time_slots s
JOIN public.rooms r ON r.id = s.room_id
WHERE r.name = 'Orion'
  AND s.starts_at >= TIMESTAMPTZ '2030-01-14 09:00:00+00'
  AND s.starts_at <  TIMESTAMPTZ '2030-01-14 11:00:00+00'
ORDER BY s.starts_at, s.id;
```

### Проверка ограничений

Каждый блок выполнен отдельно в `booking_app`. При повторении доводи каждый блок до `ROLLBACK` после ошибки или успешной вставки. В `psql` для просмотра подробностей ошибки перед откатом можно выполнить `\errverbose`.

| Пробная вставка | Результат PostgreSQL |
|---|---|
| Бронь с отсутствующим пользователем | Отклонена по `bookings_user_fk`. |
| Комната с `capacity = 0` | Отклонена по `rooms_capacity_positive`. |
| Второй слот Orion с началом `09:00Z` | Отклонена по `time_slots_room_start_key`. |
| Вторая `ACTIVE` для слота Orion на `09:00Z` | Отклонена по `bookings_one_active_per_slot_idx`. |
| `CANCELLED` для того же слота | Вставка прошла. `RETURNING` вернул статус `CANCELLED`, затем выполнен откат. |

```sql
BEGIN;
INSERT INTO public.bookings (id, user_id, slot_id, status)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    '90000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002',
    'ACTIVE'
);
ROLLBACK;

BEGIN;
INSERT INTO public.rooms (id, name, capacity, has_projector)
VALUES ('10000000-0000-0000-0000-000000000002', 'Capacity check', 0, false);
ROLLBACK;

BEGIN;
INSERT INTO public.time_slots (id, room_id, starts_at, ends_at)
VALUES (
    '10000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    TIMESTAMPTZ '2030-01-14 09:00:00+00',
    TIMESTAMPTZ '2030-01-14 10:00:00+00'
);
ROLLBACK;

BEGIN;
INSERT INTO public.bookings (id, user_id, slot_id, status)
VALUES (
    '10000000-0000-0000-0000-000000000004',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'ACTIVE'
);
ROLLBACK;

BEGIN;
INSERT INTO public.bookings (id, user_id, slot_id, status)
VALUES (
    '10000000-0000-0000-0000-000000000005',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'CANCELLED'
)
RETURNING id, status;
ROLLBACK;
```

После откатов повторный подсчёт подтвердил исходные `4 / 3 / 8 / 6`.
