-- Проверка соединений
-- А
SELECT current_database();
--booking_training
SELECT current_user;
--emiliashmidt
SELECT pg_backend_pid();
--95152
SHOW transaction_isolation;
--read committed
SHOW server_version;
--18.6 (Postgres.app)

--B
SELECT current_database();
--booking_training
SELECT current_user;
--emiliashmidt
SELECT pg_backend_pid();
--95222
SHOW transaction_isolation;
--read committed
SHOW server_version;
--18.6 (Postgres.app)



CREATE SCHEMA IF NOT EXISTS day4_lab;

CREATE TABLE day4_lab.room_state (
    id uuid PRIMARY KEY,
    capacity integer NOT NULL CHECK ( capacity > 0 )
);

INSERT INTO day4_lab.room_state ( id, capacity)
VALUES ('00000000-0000-0000-0000-000000000001', 6);

-- 1. Проверка отката
--A: начни явную транзакцию.
--A: измени вместимость строки с идентификатором 1 на 8.
--A: прочитай эту строку. Запиши результат.
BEGIN;
UPDATE day4_lab.room_state
    SET capacity = 8
        WHERE id = '00000000-0000-0000-0000-000000000001';
SELECT * FROM day4_lab.room_state;
-- вместимость 8

--B: прочитай ту же строку, пока транзакция A ещё открыта.
--не увидит изменение, вместимость 6
SELECT * FROM day4_lab.room_state;
-- вместимость 6

--A: отмени транзакцию.
ROLLBACK;

--A и B: снова прочитай строку.
SELECT * FROM day4_lab.room_state;
-- вместимость 6 и в А, и в В

-- 2. Проверка фиксации
--A: начни новую транзакцию.
--A: снова измени вместимость на 8.
--A: зафиксируй транзакцию.
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 8
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;

--B: выполни новую выборку этой строки.
SELECT * FROM day4_lab.room_state;
-- вместимость 8

--A: также проверь результат.
SELECT * FROM day4_lab.room_state;
-- вместимость 8

-- возвращение вместимости 6
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 6
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;
--B: выполни новую выборку этой строки.
SELECT * FROM day4_lab.room_state;
-- вместимость 6

--A: также проверь результат.
SELECT * FROM day4_lab.room_state;
-- вместимость 6

-- 3. Воспроизведение ошибки и восстановление работы
--A: начни транзакцию.
--A: измени вместимость с 6 на 7.
--A: прочитай строку и убедись, что собственное изменение видно.
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 7
WHERE id = '00000000-0000-0000-0000-000000000001';
SELECT * FROM day4_lab.room_state;
-- вместимость 7

--A: попробуй изменить вместимость на 0.
UPDATE day4_lab.room_state
SET capacity = 0
WHERE id = '00000000-0000-0000-0000-000000000001';
--ERROR:  new row for relation "room_state" violates check constraint "room_state_capacity_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000001, 0).

--A: попробуй выполнить обычную выборку в той же транзакции.
SELECT * FROM day4_lab.room_state;
--ERROR:  current transaction is aborted, commands ignored until end of transaction block

--B: прочитай строку.
SELECT * FROM day4_lab.room_state;
--вместимость 6

--A: выполни откат.
ROLLBACK;

--A и B: снова прочитай строку.
SELECT * FROM day4_lab.room_state;
--вместимость 6 и в А, и в В


-- 4. Read Committed
--A: начни транзакцию с явно указанным уровнем Read Committed.
--A: прочитай вместимость нужной строки. Запиши
BEGIN ISOLATION LEVEL READ COMMITTED;
SELECT * FROM day4_lab.room_state;
--вместимость 6

--B: начни свою транзакцию, измени вместимость на 7 и зафиксируй.
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 7
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;

--A: повтори тот же SELECT, не завершая свою транзакцию.
SELECT * FROM day4_lab.room_state;
--вместимость 7, так как заново делается снимок уже измененной записи

--A: заверши читающую транзакцию откатом.
ROLLBACK;

--Проверь текущее значение.
SELECT * FROM day4_lab.room_state;
----вместимость 7 в А, и в В

-- возвращение вместимости 6
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 6
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;
--Проверь текущее значение.
SELECT * FROM day4_lab.room_state;
----вместимость 6 в А, и в В

--5. Repeatable Read
--A: начни транзакцию с уровнем Repeatable Read.
--A: прочитай вместимость — это чтение закрепит снимок.
BEGIN ISOLATION LEVEL REPEATABLE READ;
SELECT * FROM day4_lab.room_state;
--вместимость 6

--B: в отдельной транзакции измени вместимость на 7 и сделай COMMIT.
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 7
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;

--A: повтори прежний SELECT внутри открытой транзакции.
SELECT * FROM day4_lab.room_state;
--вместимость 6, так как используется снимок первого SELECT

--A: заверши транзакцию.
COMMIT;

--A: выполни новое чтение после завершения предыдущей транзакции.
SELECT * FROM day4_lab.room_state;
--вместимость 7
-- в отличие от 4 пункта внутри А транзакции значение не меняется, так как снимок делается только при 1 SELECT
-- после выхода из транзакции читается измененное значение, так как делается новый снимок

-- возвращение вместимости 6
BEGIN;
UPDATE day4_lab.room_state
SET capacity = 6
WHERE id = '00000000-0000-0000-0000-000000000001';
COMMIT;
--Проверь текущее значение.
SELECT * FROM day4_lab.room_state;
--вместимость 6 в А, и в В

--6. Ожидание блокировки

--A: начни транзакцию.
--A: выбери нужную строку с блокировкой для изменения.
BEGIN;
SELECT * FROM day4_lab.room_state
WHERE id = '00000000-0000-0000-0000-000000000001'
FOR UPDATE;

--B: начни транзакцию и сначала выполни обычный SELECT этой строки. Запиши, удалось ли чтение.
BEGIN;
SELECT * FROM day4_lab.room_state
WHERE id = '00000000-0000-0000-0000-000000000001';
-- чтение удалось, вместимость 6

--B: попробуй изменить вместимость на 8.
UPDATE day4_lab.room_state
SET capacity = 8
WHERE id = '00000000-0000-0000-0000-000000000001';
-- команда ожидает завершения A.

--A: выполни откат, освобождая блокировку.
ROLLBACK;

--B: дождись завершения UPDATE.
--UPDATE 1

--B: прочитай строку внутри своей транзакции.
SELECT * FROM day4_lab.room_state;
-- вместимость 8

--B: откати изменение.
ROLLBACK;
--Проверь строку в обоих соединениях.
SELECT * FROM day4_lab.room_state;
--вместимость 6 в А, и в В
-- ожидание в 6 опыте не является deadlock, так как нет цикла зависимости ожидания А от В, и В от А




