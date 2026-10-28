-- количество записей в четырёх таблицах
-- 1 строка - количество записей в 1 таблице
-- 1 строка
SELECT COUNT(r.id) AS rooms_count
FROM public.rooms as r;
-- 1 строка 4 комнаты

SELECT COUNT(u.id) AS users_count
FROM public.users as u;
-- 1 строка 3 пользователя

SELECT COUNT(s.id) AS slots_count
FROM public.time_slots as s;
-- 1 строка 8 слотов

SELECT COUNT(b.id) AS bookings_count
FROM public.bookings as b;
-- 1 строка 6 броней

--по всем комнатам четыре показателя: сумму вместимости, среднюю вместимость, минимум и максимум
-- 1 строка содержит все показатели по всем комнатам
-- 1 строка
SELECT SUM(r.capacity),
       AVG(r.capacity),
       MIN(r.capacity),
       MAX(r.capacity)
FROM public.rooms AS r;
-- 1 строка
-- 32, 8, 4, 12

-- условие вместимости, которому не соответствует ни одна комната, например больше 100.
-- Для этого пустого отбора сравни количество комнат, сумму вместимости и сумму с подстановкой нуля через COALESCE.
-- 1 строка все показатели
-- 1 строка
SELECT COUNT(r.id),
       SUM(r.capacity),
       COALESCE(SUM(r.capacity), 0)
FROM public.rooms AS r
WHERE r.capacity > 100;
-- 1 строка
-- 0, NULL, 0

-- активные брони всех комнат
-- для каждой комнаты: UUID; название; количество активных броней
-- cортировка — по UUID комнаты
-- 1 строка - 1 комната
-- 4 строки
SELECT r.id, r.name, COUNT(b.id) AS active_bookings_count
FROM public.rooms AS r
LEFT JOIN public.time_slots AS s ON s.room_id = r.id
LEFT JOIN public.bookings AS b ON b.slot_id = s.id AND b.status = 'ACTIVE'
GROUP BY r.id, r.name
ORDER BY r.id;
-- 4 строки
-- Orion: 2, Vega: 1, Sirius: 0, Luna: 0

--вариант с подсчетом строк, а не b.id
SELECT r.id, r.name, COUNT(*) AS active_bookings_count
FROM public.rooms AS r
         LEFT JOIN public.time_slots AS s ON s.room_id = r.id
         LEFT JOIN public.bookings AS b ON b.slot_id = s.id AND b.status = 'ACTIVE'
GROUP BY r.id, r.name
ORDER BY r.id;
-- 4 строки
-- Orion: 3, Vega: 3, Sirius: 2, Luna: 1
-- считаются не активные брони, а считаются строки результата соединений
-- для Luna сохраняется одна строка с NULL в полях слота и брони

-- активные брони всех пользователей
-- UUID, имя и количество активных броней каждого пользователя
-- сортировка — по UUID пользователя
-- 1 строка - 1 пользователь
-- 3 строки
SELECT u.id, u.name, COUNT(b.id)
FROM public.users AS u
LEFT JOIN public.bookings AS b ON b.user_id = u.id AND b.status = 'ACTIVE'
GROUP BY u.id
ORDER BY u.id;
-- 3 строки
-- Anna: 1, Boris: 2, Vera: 0

--На основе правильного отчёта из шага 2
-- сделай новый запрос, который оставляет только комнаты с количеством активных броней строго больше одной.
-- 1 строка - 1 комната
-- 1 строка
SELECT r.id, r.name, COUNT(b.id) AS active_bookings_count
FROM public.rooms AS r
LEFT JOIN public.time_slots AS s ON s.room_id = r.id
LEFT JOIN public.bookings AS b ON b.slot_id = s.id AND b.status = 'ACTIVE'
GROUP BY r.id, r.name
HAVING COUNT(b.id) > 1
ORDER BY r.id;
-- 1 строка
-- Orion: 2
-- условие относится к HAVING, тк выполняется уже после группировки

-- активные брони по дням
-- дата начала слота и количество активных броней на эту дату
-- 1 строка - 1 день
-- 1 строка
SELECT (s.starts_at AT TIME ZONE 'UTC')::date, COUNT(b.id) AS active_bookings_count
FROM public.time_slots AS s
LEFT JOIN public.bookings AS b ON b.slot_id = s.id AND b.status = 'ACTIVE'
GROUP BY (s.starts_at AT TIME ZONE 'UTC')::date
ORDER BY (s.starts_at AT TIME ZONE 'UTC')::date;
-- 1 строка
--  2030-01-14: 3
-- «назначено на день» — дата начала слота;
-- «создано за день» — дата создания брони.



