# Проверка исходного состояния
SELECT current_database();
booking_training_day5_check


После выполнения шагов результаты:
rooms	4
users	3
time_slots	8
bookings	6

Cвободные слоты —  номера 2, 5, 6, 7, 8;
SELECT s.id
FROM public.time_slots AS s
WHERE NOT EXISTS (
SELECT 1
FROM public.bookings AS b
WHERE b.slot_id = s.id AND b.status = 'ACTIVE'
);
--2, 5, 6, 7, 8

комнаты с количеством ACTIVE, включая нули — Orion 2, Vega 1, Sirius 0, Luna 0
SELECT r.id, r.name, COUNT(b.id) AS active_bookings_count
FROM public.rooms AS r
LEFT JOIN public.time_slots AS s ON s.room_id = r.id
LEFT JOIN public.bookings AS b ON b.slot_id = s.id AND b.status = 'ACTIVE'
GROUP BY r.id, r.name
ORDER BY r.id;
--Orion 2, Vega 1, Sirius 0, Luna 0

история Anna — три брони с устойчивой сортировкой
SELECT b.id, b.status, u.name, u.email, r.name, s.starts_at, s.ends_at
FROM public.bookings AS b
INNER JOIN public.users as u ON b.user_id = u.id
INNER JOIN public.time_slots as s ON b.slot_id = s.id
INNER JOIN public.rooms as r ON s.room_id = r.id
WHERE u.id = '00000000-0000-0000-0000-000000000001'
ORDER BY b.created_at DESC, b.id DESC;
--2 CANCELLED, 1 ACTIVE

отсутствие слотов с несколькими ACTIVE
SELECT slot_id, COUNT(id)
FROM public.bookings
WHERE status = 'ACTIVE'
GROUP BY slot_id
HAVING COUNT(id) > 1;
--0 rows
bookings_one_active_per_slot_idx | CREATE UNIQUE INDEX bookings_one_active_per_slot_idx 
ON public.bookings USING btree (slot_id) WHERE (status = 'ACTIVE'::text)

# Проверка сценариев

Сценарии R3/R4/R5 повторно выполнены в новой БД `booking_training_day5_check`
по [SQL-сценарию дня 4](../sql/13-day-4-booking-race.sql).
Таблица — `day4_lab.bookings_race`, индекс — `bookings_race_one_active_per_slot_idx`.
Слот — `00000000-0000-0000-0000-000000000008`.

| Запись | UUID | Итог |
|---|---|---|
| R3 | `40000000-0000-0000-0000-000000000003` | CANCELLED |
| R4 | `40000000-0000-0000-0000-000000000004` | Не сохранена; после ошибки выполнен ROLLBACK |
| R5 | `40000000-0000-0000-0000-000000000005` | ACTIVE |

Итоговая выборка слота 8: две записи, из них одна ACTIVE.

HTTP-ответы — ожидаемое поведение по контракту будущего API.

| Сценарий | Выполненное действие и результат | Ожидаемый HTTP-ответ |
|---|---|---|
| Успешная бронь | Создана R3 для слота 8; после фиксации сохранена одна ACTIVE | POST → 201, данные брони и Location |
| Вторая ACTIVE того же слота | Конкурентная вставка R4 ожидала завершения R3, после COMMIT получила ошибку частичного уникального индекса; R4 не сохранена | POST → 409 SLOT_ALREADY_BOOKED |
| Отмена | Статус R3 изменён на CANCELLED; запись сохранилась в истории | DELETE → 204 без тела; повторная отмена по контракту также возвращает 204 |
| Повторное бронирование | Создана новая R5 для слота 8; R3 осталась CANCELLED, R5 — ACTIVE, R4 отсутствует; активная бронь одна | POST → 201; GET истории сохраняет отменённую запись |
| Неверные данные | БД отклонила бронь с неизвестным пользователем по FK и комнату с capacity=0 по CHECK | Неверные поля → 400; неизвестный слот → 404 после проверки приложением. Ошибка FK сама по себе не определяет HTTP-статус |

Ожидаемые HTTP-ответы согласованы с [контрактом API](api-contract.md).
