CREATE TEMP TABLE day3_slots_lab (
    id	uuid,
    room_id	uuid NOT NULL,
    starts_at	timestamptz NOT NULL,
    ends_at	timestamptz NOT NULL
);

WITH slot_numbers AS (
    SELECT r.room_no, s.slot_no, (r.room_no - 1) * 1000 + s.slot_no AS global_no
    FROM generate_series(1, 100) AS r(room_no)
    CROSS JOIN generate_series(1, 1000) AS s(slot_no)
       ),
    prepared_slots AS (
    SELECT global_no,
       room_no,
       TIMESTAMPTZ '2030-01-01 00:00:00+00' + (slot_no - 1) * INTERVAL '1 hour' AS starts_at
    FROM slot_numbers
    )
INSERT INTO day3_slots_lab (
    id,
    room_id,
    starts_at,
    ends_at
)
SELECT
    lpad(global_no::text, 32, '0')::uuid,
    lpad(room_no::text, 32, '0')::uuid,
    starts_at,
    starts_at + INTERVAL '1 hour'
FROM prepared_slots;

ANALYZE day3_slots_lab;
--CREATE TABLE
--INSERT 0 100000
--ANALYZE

--Составь запрос для одной условной комнаты за период [2030-01-14 09:00 UTC, 2030-01-14 11:00 UTC).
--Выведи поля слота, отсортируй по началу.
--До индекса
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, room_id, starts_at, ends_at
FROM day3_slots_lab
WHERE room_id = '00000000-0000-0000-0000-000000000001'
        AND starts_at >= '2030-01-14 09:00:00+00'
        AND starts_at < '2030-01-14 11:00:00+00'
ORDER BY starts_at;
--Результат
-- Sort  (cost=2685.01..2685.02 rows=2 width=48) (actual time=14.849..14.851 rows=2.00 loops=1)
-- Sort Key: starts_at
--    Sort Method: quicksort  Memory: 25kB
--   Buffers: local hit=935
--    ->  Seq Scan on day3_slots_lab  (cost=0.00..2685.00 rows=2 width=48) (actual time=0.088..14.838 rows=2.00 loops=1)
--          Filter: ((starts_at >= '2030-01-14 12:00:00+03'::timestamp with time zone) AND (starts_at < '2030-01-14 14:00:00+03'::timestamp with time zone) AND (room_id = '00000000-0000-0000-0000-000000000001'::uuid))
--          Rows Removed by Filter: 99998
--          Buffers: local hit=935
--  Planning:
--    Buffers: shared hit=14 dirtied=1
--  Planning Time: 0.701 ms
--  Execution Time: 14.890 ms
--(12 rows)


--добавление индекса
CREATE INDEX day3_slots_lab_room_start_idx
    ON day3_slots_lab (room_id, starts_at);
--CREATE INDEX

--обновление статистики
ANALYZE day3_slots_lab;
-- ANALYZE

-- После индекса
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, room_id, starts_at, ends_at
FROM day3_slots_lab
WHERE room_id = '00000000-0000-0000-0000-000000000001'
AND starts_at >= '2030-01-14 09:00:00+00'
AND starts_at < '2030-01-14 11:00:00+00'
ORDER BY starts_at;
--Результат
--Index Scan using day3_slots_lab_room_start_idx on day3_slots_lab  (cost=0.42..10.21 rows=2 width=48) (actual time=0.061..0.062 rows=2.00 loops=1)
--Index Cond: ((room_id = '00000000-0000-0000-0000-000000000001'::uuid) AND (starts_at >= '2030-01-14 12:00:00+03'::timestamp with time zone) AND (starts_at < '2030-01-14 14:00:00+03'::timestamp with time zone))
--   Index Searches: 1
--   Buffers: local hit=1 read=3
-- Planning:
--   Buffers: shared hit=18, local read=1
-- Planning Time: 0.475 ms
-- Execution Time: 0.087 ms
--(8 rows)

--Сравнение
-- способ чтения: последовательно Seq Scan и по индексу Index Scan
-- наличие отдельной сортировки: есть, нет
-- оценки и фактическое количество строк: 2 до и после, 2 до и после, при loops=1
-- показатели буферов: local hit=935 и local hit=1 read=3
-- время выполнения: 14.890 ms и 0.087 ms







