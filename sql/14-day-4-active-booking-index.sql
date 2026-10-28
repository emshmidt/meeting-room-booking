--Правило в основную таблицу
--проверка ACTIVE
SELECT slot_id, COUNT(id)
FROM public.bookings
WHERE status = 'ACTIVE'
GROUP BY slot_id
HAVING COUNT(id) > 1;
--0 rows

--существующие индексы основной таблицы
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'bookings';
--bookings_pkey | CREATE UNIQUE INDEX bookings_pkey ON public.bookings USING btree (id)

--частичный уникальный индекс
CREATE UNIQUE INDEX bookings_one_active_per_slot_idx
    ON public.bookings (slot_id) WHERE status = 'ACTIVE';
--CREATE INDEX

--проверка
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename = 'bookings';
--bookings_pkey                    | CREATE UNIQUE INDEX bookings_pkey ON public.bookings USING btree (id)
--bookings_one_active_per_slot_idx | CREATE UNIQUE INDEX bookings_one_active_per_slot_idx ON public.bookings USING btree (slot_id) WHERE (status = 'ACTIVE'::text)

