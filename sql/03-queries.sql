--все столбцы и все строки таблицы комнат
SELECT *
FROM public.rooms;
--название комнаты и вместимость всех комнат
SELECT name, capacity
FROM public.rooms;
--вместимость и название комнаты всех комнат
SELECT capacity, name
FROM public.rooms;
--название и вместимость комнат, рассчитанных минимум на восемь человек
SELECT name, capacity
FROM public.rooms
WHERE capacity >= 8;
--название комнат, рассчитанных минимум на восемь человек.
SELECT name
FROM public.rooms
WHERE capacity >= 8;
--название и вместимость комнат, рассчитанных минимум на сто человек
SELECT name, capacity
FROM public.rooms
WHERE capacity >= 100;

--комнаты с названиями Orion или Luna
SELECT name, capacity
FROM public.rooms
WHERE name IN ('Orion', 'Luna');
--комнаты вместимостью от 4 до 8 включительно
SELECT name, capacity
FROM public.rooms
WHERE  capacity BETWEEN 4 AND 8;
--комнаты, название которых начинается с заглавной O
SELECT name
FROM public.rooms
WHERE name LIKE 'O%';
--комната, которая вмещает минимум восемь человек и имеет проектор. Также подходит Orion
SELECT name, capacity , has_projector
FROM public.rooms
WHERE (capacity>= 8 AND has_projector) OR name = 'Orion';
--все комнаты без проектора
SELECT name, capacity , has_projector
FROM public.rooms
WHERE NOT has_projector;
--комнаты с отсутствующим описанием
SELECT name, description
FROM public.rooms
WHERE description IS NULL;
--комнаты с описанием, равным пустой строке
SELECT name, description
FROM public.rooms
WHERE description = '';
--неправильное сравнение с null
SELECT name, description
FROM public.rooms
WHERE description = NULL;
--основной порядок — вместимость по возрастанию;
--при одинаковой вместимости — идентификатор по возрастанию.
SELECT id, name, capacity
FROM public.rooms
ORDER BY capacity, id;
--первая страница размером две строки;
--вторая страница того же размера;
--третья страница
SELECT id, name, capacity
FROM public.rooms
ORDER BY capacity, id LIMIT 2 OFFSET 0;
SELECT id, name, capacity
FROM public.rooms
ORDER BY capacity, id LIMIT 2 OFFSET 2;
SELECT id, name, capacity
FROM public.rooms
ORDER BY capacity, id LIMIT 2 OFFSET 4;


--предварительная выборка тестовой комнаты
SELECT id, name, capacity
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
--изменение данных
--изменение вместимости тестовой комнаты
UPDATE public.rooms
SET capacity = 6
WHERE  id = '00000000-0000-0000-0000-000000000005';
--проверка после изменения вместимости тестовой комнаты
SELECT id, name, capacity
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
--проверка перед удалением тестовой комнаты
SELECT id, name, capacity
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
--удаление тестовой комнаты
DELETE
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
--проверка результата
SELECT id, name, capacity
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
SELECT *
FROM public.rooms;
--повторное удаление
DELETE
FROM public.rooms
WHERE id = '00000000-0000-0000-0000-000000000005';
