--Бронь с неизвестным пользователем или слотом. Ошибка: внешний ключ
INSERT INTO public.bookings (
                             id,
                             user_id,
                             slot_id)
VALUES (
           '00000000-0000-0000-0000-000000000007',
           '00000000-0000-0000-0000-000000000100',
           '00000000-0000-0000-0000-000000000001'
       );
--ERROR:  insert or update on table "bookings" violates foreign key constraint "bookings_user_id_fkey"
--DETAIL:  Key (user_id)=(00000000-0000-0000-0000-000000000100) is not present in table "users".


--Новый пользователь с уже существующим email. Ошибка: уникальность
INSERT INTO public.users (
                          id,
                          name,
                          email,
                          role
)
VALUES
    (
     '00000000-0000-0000-0000-000000000004',
     'new Anna',
     'anna@example.com',
     'USER'
    );
--ERROR:  duplicate key value violates unique constraint "users_email_key"
--DETAIL:  Key (email)=(anna@example.com) already exists.

--Комната с вместимостью 0. Ошибка: проверка положительности
INSERT INTO public.rooms (
                          id,
                          name,
                          capacity,
                          has_projector
)
VALUES (
        '00000000-0000-0000-0000-000000000010',
        'new room',
        0,
        false
       );
--ERROR:  new row for relation "rooms" violates check constraint "rooms_capacity_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, new room, 0, f, null).

--Явный NULL в обязательном поле. Ошибка: NOT NULL
INSERT INTO public.users (
                          id,
                          name,
                          email,
                          role
)
VALUES
    (
     '00000000-0000-0000-0000-000000000010',
     'new user',
     NULL,
     'USER'
    );
--ERROR:  null value in column "email" of relation "users" violates not-null constraint
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, new user, null, USER).

--Бронь со статусом PENDING. Ошибка: проверка статуса

INSERT INTO public.bookings (
                             id,
                             user_id,
                             slot_id,
                             status
)
VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'PENDING'
    );
--ERROR:  new row for relation "bookings" violates check constraint "bookings_status_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, 00000000-0000-0000-0000-000000000001, 00000000-0000-0000-0000-000000000001, PENDING, 2026-10-22 16:09:52.169943+03).

--Слот длительностью 90 минут. Ошибка: проверка длительности
INSERT INTO public.time_slots (
                               id,
                               room_id,
                               starts_at,
                               ends_at
)
VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000001',
        '2030-01-14 13:00:00+00:00',
        '2030-01-14 14:30:00+00:00'
    );
--ERROR:  new row for relation "time_slots" violates check constraint "time_slots_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, 00000000-0000-0000-0000-000000000001, 2030-01-14 16:00:00+03, 2030-01-14 17:30:00+03).

--Слот 09:30–10:30 UTC. Проверка выравнивания
INSERT INTO public.time_slots (
                               id,
                               room_id,
                               starts_at,
                               ends_at
)
VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000004',
        '2030-01-14 9:30:00+00:00',
        '2030-01-14 10:30:00+00:00'
    );
--ERROR:  new row for relation "time_slots" violates check constraint "time_slots_starts_at_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, 00000000-0000-0000-0000-000000000004, 2030-01-14 12:30:00+03, 2030-01-14 13:30:00+03).

--Слот 09:00:00.5–10:00:00.5 UTC. Ошибка: проверка долей секунды
INSERT INTO public.time_slots (
    id,
    room_id,
    starts_at,
    ends_at
)
VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000004',
        '2030-01-14 9:00:00.5+00:00',
        '2030-01-14 10:00:00.5+00:00'
    );
--ERROR:  new row for relation "time_slots" violates check constraint "time_slots_starts_at_check"
--DETAIL:  Failing row contains (00000000-0000-0000-0000-000000000010, 00000000-0000-0000-0000-000000000004, 2030-01-14 12:00:00.5+03, 2030-01-14 13:00:00.5+03).

--Новый слот той же комнаты с уже существующим началом. Ошибка: составная уникальность
INSERT INTO public.time_slots (
    id,
    room_id,
    starts_at,
    ends_at
)
VALUES
    (
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000002',
        '2030-01-14 10:00:00+00:00',
        '2030-01-14 11:00:00+00:00'
    );

--ERROR:  duplicate key value violates unique constraint "time_slots_room_id_starts_at_key"
--DETAIL:  Key (room_id, starts_at)=(00000000-0000-0000-0000-000000000002, 2030-01-14 13:00:00+03) already exists.

--Удаление Orion при существующих слотах. Ошибка: запрет удаления родителя
DELETE
FROM public.rooms
WHERE name = 'Orion';
--ERROR:  update or delete on table "rooms" violates RESTRICT setting of foreign key constraint "time_slots_room_id_fkey" on table "time_slots"
--DETAIL:  Key (id)=(00000000-0000-0000-0000-000000000001) is referenced from table "time_slots".

--Проверка DEFAULT
INSERT INTO public.bookings (
                             id,
                             user_id,
                             slot_id
)
VALUES
    (
        '00000000-0000-0000-0000-000000000007',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000006'
    );
--проверка полей
SELECT id, status, created_at
FROM public.bookings
WHERE id = '00000000-0000-0000-0000-000000000007';
--удаление поля
DELETE FROM public.bookings
WHERE id = '00000000-0000-0000-0000-000000000007';
--проверка удаления
SELECT id, status, created_at
FROM public.bookings
WHERE id = '00000000-0000-0000-0000-000000000007';
--проверка оставшихся броней
SELECT id, status
FROM public.bookings;