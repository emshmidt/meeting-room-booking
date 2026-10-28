CREATE TABLE day4_lab.bookings_race (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES public.users(id) ON DELETE RESTRICT,
    slot_id uuid NOT NULL REFERENCES public.time_slots(id) ON DELETE RESTRICT,
    status text NOT NULL CHECK ( status IN ('ACTIVE', 'CANCELLED') ) DEFAULT 'ACTIVE',
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 1. Гонка без защиты
--A	Начни транзакцию Read Committed. Проверь наличие ACTIVE для слота 7 в учебной таблице
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT *
      FROM day4_lab.bookings_race
      WHERE slot_id = '00000000-0000-0000-0000-000000000007' AND status = 'ACTIVE';
-- 0 rows

--B	Начни такую же транзакцию. Выполни ту же проверку
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT *
      FROM day4_lab.bookings_race
      WHERE slot_id = '00000000-0000-0000-0000-000000000007' AND status = 'ACTIVE';
-- 0 rows

--A	Добавь R1: Anna, слот 7, ACTIVE. Пока не фиксируй
INSERT INTO day4_lab.bookings_race (id, user_id, slot_id)
    VALUES (
            '40000000-0000-0000-0000-000000000001',
            '00000000-0000-0000-0000-000000000001',
            '00000000-0000-0000-0000-000000000007'
           );
--B	Добавь R2: Boris, слот 7, ACTIVE
INSERT INTO day4_lab.bookings_race (id, user_id, slot_id)
VALUES (
           '40000000-0000-0000-0000-000000000002',
           '00000000-0000-0000-0000-000000000002',
           '00000000-0000-0000-0000-000000000007'
       );

--A	Зафиксируй
COMMIT;
--B	Зафиксируй
COMMIT;

--Любое	Выведи брони слота 7 и посчитай активные
SELECT slot_id, COUNT(id)
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000007' AND status = 'ACTIVE'
GROUP BY slot_id;
--2 активные брони
--BEGIN не предотвратил такой результат, тк не проверяет правило на 1 активную бронь на слот

--2. Защита
--Отдельной транзакцией измени только R2 на CANCELLED и зафиксируй
BEGIN;
UPDATE day4_lab.bookings_race
SET status = 'CANCELLED'
WHERE id = '40000000-0000-0000-0000-000000000002';
COMMIT;

--Проверь историю слота 7
SELECT id, slot_id, status
    FROM day4_lab.bookings_race
        WHERE slot_id = '00000000-0000-0000-0000-000000000007';
-- R1 активна, R2 отменена

--Найди слоты, у которых больше одной ACTIVE
--CTE
WITH counted AS (
    SELECT slot_id, COUNT(id) AS count_id
    FROM day4_lab.bookings_race
    WHERE status = 'ACTIVE'
    GROUP BY slot_id
)
SELECT slot_id
FROM counted
WHERE count_id > 1;
--0 rows

--HAVING
SELECT slot_id, COUNT(id)
FROM day4_lab.bookings_race
WHERE status = 'ACTIVE'
GROUP BY slot_id
HAVING COUNT(id) > 1;
--0 rows

--частичный уникальный индекс
CREATE UNIQUE INDEX bookings_race_one_active_per_slot_idx
    ON day4_lab.bookings_race (slot_id) WHERE (status = 'ACTIVE');
--CREATE INDEX
--проверка
SELECT id, slot_id, status
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000007';
-- R1 активна, R2 отменена

--3. Конкурентные вставки с защитой
--A	Начни Read Committed и проверь отсутствие ACTIVE для слота 8
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT *
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000008' AND status = 'ACTIVE';
--0 rows

--B	Начни Read Committed и выполни ту же проверку
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT *
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000008' AND status = 'ACTIVE';
--0 rows
--A	Добавь R3: Anna, слот 8, ACTIVE
INSERT INTO day4_lab.bookings_race(id, user_id, slot_id)
VALUES (
           '40000000-0000-0000-0000-000000000003',
           '00000000-0000-0000-0000-000000000001',
           '00000000-0000-0000-0000-000000000008'
       );
--INSERT 0 1

--B	Попробуй добавить R4: Boris, слот 8, ACTIVE
INSERT INTO day4_lab.bookings_race(id, user_id, slot_id)
VALUES (
           '40000000-0000-0000-0000-000000000004',
           '00000000-0000-0000-0000-000000000002',
           '00000000-0000-0000-0000-000000000008'
       );
--B ожидает

--A	Пока B ожидает, выполни COMMIT
COMMIT;
--COMMIT
--B	Получи результат вставки и заверши ошибочную транзакцию откатом
--ERROR:  duplicate key value violates unique constraint "bookings_race_one_active_per_slot_idx"
--DETAIL:  Key (slot_id)=(00000000-0000-0000-0000-000000000008) already exists.
ROLLBACK;
--ROLLBACK

--Любое	Проверь записи слота 8
SELECT *
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000008';
--1 row R3
--что произошло бы в B после ROLLBACK в A: вставка R4 в таблицу, после коммита в В сохранение изменений в таблице.

--4. Отмена и повторное бронирование
--Отмени R3 изменением статуса и зафиксируй
BEGIN;
UPDATE day4_lab.bookings_race
SET status = 'CANCELLED'
WHERE id = '40000000-0000-0000-0000-000000000003';
COMMIT;

--новая запись R5: Boris, слот 8, ACTIVE.
BEGIN;
INSERT INTO day4_lab.bookings_race(id, user_id, slot_id)
VALUES (
           '40000000-0000-0000-0000-000000000005',
           '00000000-0000-0000-0000-000000000002',
           '00000000-0000-0000-0000-000000000008'
       );
COMMIT;

--история слота 8 и отдельно посчитай ACTIVE
SELECT *
FROM day4_lab.bookings_race
WHERE slot_id = '00000000-0000-0000-0000-000000000008';
--R3 отменена, R5 активна, R4 отсутствует
SELECT slot_id, COUNT(id)
FROM day4_lab.bookings_race
WHERE status = 'ACTIVE' AND slot_id = '00000000-0000-0000-0000-000000000008'
GROUP BY slot_id;
-- 1
