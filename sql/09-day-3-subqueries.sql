-- комнаты вместимостью выше средней
-- UUID, название и вместимость комнат
-- 1 строка - 1 комната
-- 1 строка
SELECT r.id, r.name, r.capacity
FROM public.rooms as r
WHERE capacity > (
    SELECT AVG(capacity)
    FROM public.rooms
    );
-- 1 строка Luna: 12

-- свободные слоты
-- UUID слота; UUID комнаты; начало и конец слота
-- 1 строка - 1 свободный слот
-- 5 строк
SELECT s.id, s.room_id, s.starts_at, s.ends_at
FROM public.time_slots AS s
WHERE NOT EXISTS(
    SELECT 1
    FROM public.bookings AS b
    WHERE s.id = b.slot_id AND b.status = 'ACTIVE'
)
ORDER BY s.starts_at, s.id;
-- 5 строк
-- сейчас все слоты на 14.01.2030, свободные UTC+3:
-- для 3 комнаты в 12, для 1 комнаты в 13, для 2 комнаты в 13, для 3 комнаты в 13, для 2 в 14

-- Слот 3 исключён: у него есть и отменённая, и активная бронь.
-- Слот 7 включён: его единственная бронь отменена.
-- Luna отсутствует: у комнаты нет слотов, которые можно было бы вывести.

-- отчёт пользователей через CTE
-- пользователи, у которых активных броней больше одной
-- 1 строка - 1 пользователь
-- 1 строка
WITH user_counts AS (
    SELECT u.id AS id, u.name AS name, COUNT(b.id) AS bookings_count
    FROM public.users AS u
    LEFT JOIN public.bookings AS b ON b.user_id = u.id AND b.status = 'ACTIVE'
    GROUP BY u.id
    )
SELECT u.id, u.name, u.bookings_count
FROM user_counts AS u
WHERE bookings_count > 1;
-- 1 строка Boris: 2

-- получить последнюю созданную бронь каждого пользователя, у которого есть история бронирования
-- учитываем все статусы, включая отменённые

-- шаг 1. Проверить время создания
-- 1 строка - 1 бронь
-- 6 строк
SELECT id, user_id, created_at
FROM public.bookings;
-- 6 строк
-- все шесть броней создавались одним INSERT, поэтому одинаковое время создания

-- шаг 2. Пронумеровать брони
-- составь запрос, который выводит: UUID брони; UUID пользователя; статус; время создания; порядковый номер внутри истории пользователя.
-- 1 строка - 1 бронь
-- 6 строк
SELECT id, user_id, status, created_at,
       ROW_NUMBER() OVER (
PARTITION BY user_id
ORDER BY created_at DESC, id DESC
) AS row_num
FROM public.bookings;
-- 6 строк
-- Для Anna номера идут от 1 до 3, для Boris — тоже от 1 до 3.

-- шаг 3. Оставить последнюю бронь
-- 1 строка - 1 бронь
-- 2 строки
WITH numbered_bookings AS (
    SELECT id, user_id, status, created_at,
    ROW_NUMBER() OVER (
    PARTITION BY user_id
    ORDER BY created_at DESC, id DESC
    ) AS row_num
    FROM public.bookings
)
SELECT id, user_id, status, created_at
FROM numbered_bookings
WHERE row_num = 1
ORDER BY user_id;
-- 2 строки - 2 отмененные брони для Anna, Boris

-- до отбора было 6 строк, тк нумерация не меняет кол-во строк,
-- а после — 2 строки, тк выбирается последнее бронирование для каждого пользователя с историей бронирований;
-- отменённые брони подходят под задание, тк рассматриваем последние брони, а не последние активные брони
-- Vera отсутствует, тк у неет нет истории бронирований;
-- можно сохранить Vera в отчёте обо всех пользователях начать с всех пользователей и через LEFT JOIN присоединить уже выбранные последние брони.
-- Тогда Vera сохранится с NULL в полях брони..
