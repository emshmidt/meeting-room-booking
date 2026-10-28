-- для каждой брони:
-- идентификатор и статус;
-- имя и email пользователя;
-- название комнаты;
-- начало и конец слота.
-- сортировка по началу слота, а при совпадении — по идентификатору брони.
-- 1 строка - 1 бронь с ее данными
-- 6 строк
SELECT b.id, b.status, u.name, u.email, r.name, s.starts_at, s.ends_at
FROM public.bookings AS b
INNER JOIN public.users as u ON b.user_id = u.id
INNER JOIN public.time_slots as s ON b.slot_id = s.id
INNER JOIN public.rooms as r ON s.room_id = r.id
ORDER BY s.starts_at, b.id;
-- 6 строк


-- на основе первого запроса получить только брони Anna, включая отменённые.
-- 1 строка - 1 бронь Анны с ее данными
-- 3 строки
SELECT b.id, b.status, u.name, u.email, r.name, s.starts_at, s.ends_at
FROM public.bookings AS b
         INNER JOIN public.users as u ON b.user_id = u.id
         INNER JOIN public.time_slots as s ON b.slot_id = s.id
         INNER JOIN public.rooms as r ON s.room_id = r.id
WHERE u.id = '00000000-0000-0000-0000-000000000001'
ORDER BY s.starts_at, b.id;
-- 3 строки


-- комнаты и слоты
-- получить идентификатор и название комнаты, идентификатор слота, его начало и конец.
-- упорядочить результат по идентификатору комнаты и началу слота.
-- 1 строка - 1 слот с его данными
-- 8 строк
SELECT r.id, r.name, s.id, s.starts_at, s.ends_at
FROM public.time_slots AS s
INNER JOIN public.rooms as r ON s.room_id = r.id
ORDER BY r.id, s.starts_at;
-- 8 строк

-- на основе предыдущего запроса комнат и слотов получи все комнаты, включая Luna
-- 1 строка - 1 слот с его комнатой либо комната без слота
-- 9 строк
SELECT r.id, r.name, s.id, s.starts_at, s.ends_at
FROM public.rooms as r
LEFT JOIN public.time_slots as s ON s.room_id = r.id
ORDER BY r.id, s.starts_at;
-- 9 строк

--пользователи, у которых вообще нет броней, включая отменённые
-- 1 строка - 1 пользователь
-- 1 строка
SELECT u.id, u.name, b.id
FROM public.users AS u
LEFT JOIN public.bookings AS b ON b.user_id = u.id
WHERE b.id IS NULL;
-- 1 строка

-- все пользователи и их активные брони
-- сортировка по идентификатору пользователя, затем по идентификатору брони
-- 1 строка - 1 бронь с его пользователем либо пользователь без активной брони

-- версия А: всех пользователей, присоединив только активные брони.
-- 4 строки
SELECT u.id, u.name, b.id, b.status
FROM public.users AS u
LEFT JOIN public.bookings AS b ON b.user_id = u.id AND b.status = 'ACTIVE'
ORDER BY u.id, b.id;
-- 4 строки

-- активные брони
-- 3 строки
SELECT u.id, u.name, b.id, b.status
FROM public.users AS u
LEFT JOIN public.bookings AS b ON b.user_id = u.id
WHERE b.status = 'ACTIVE'
ORDER BY u.id, b.id;
-- 3 строки

-- Vera сохраняется в первой версии, тк необходимо вывести все активные брони с сохранением всех пользователй
-- исчезает во второй, тк требуется вывести только активные брони
-- Boris занимает две строки и DISTINCT не требуется, тк у него 2 активные брони
