# Приёмочная проверка REST API

Ручной прогон выполнен 26 ноября 2026 года на localhost:8080. Проверены все восемь операций из [контракта API](api-contract.md). В ответах с датами время указано в UTC.

## Сборка и запуск JAR

26 ноября 2026 года `./mvnw clean verify` завершилась с `BUILD SUCCESS` на Microsoft OpenJDK `21.0.13.0.2` и Maven `3.10.0`. Surefire зафиксировал 14 тестов, 0 падений, 0 ошибок и 0 пропусков. В итог входят четыре запуска параметризованного теста на форму JSON.

Исполняемый `meeting-room-booking-0.0.1-SNAPSHOT.jar` запущен от Anna на порту `8080`.

```bash
java -jar target/meeting-room-booking-0.0.1-SNAPSHOT.jar --app.demo-user=anna
```

| Проверка на запущенном JAR | Фактический результат |
|---|---|
| `GET /api/v1/rooms?minCapacity=8&size=2` | `200 OK`, `application/json`. Vega и Sirius в этом порядке, `page=0`, `size=2`, `totalElements=3`. |
| `/v3/api-docs` и `/swagger-ui/index.html` | Спецификация и интерфейс открылись. В Swagger UI отображались все восемь операций. |
| `POST /api/v1/bookings` через Swagger UI | `201 Created` для свободного слота Orion `00000000-0000-0000-0000-000000000002`. Статус `ACTIVE`, `Location: /api/v1/bookings/269d20e0-f8c0-49d9-9974-3179a4d165b7`. |
| GET по `Location` | `200 OK`. Все восемь полей совпали с ответом на POST, включая `id=269d20e0-f8c0-49d9-9974-3179a4d165b7` и `createdAt=2026-11-26T09:26:36.886802Z`. |

## Как повторить прогон

Запускай приложение по [README](../README.md), выбирая `--app.demo-user=anna`, `--app.demo-user=boris` или `--app.demo-user=vera`. При смене пользователя перезапусти приложение. Каждый запуск восстанавливает исходные данные, поэтому всю цепочку создания, отмены и повторной брони выполняй в одном процессе Anna. Для проверки прав используй исходную бронь Anna с UUID `00000000-0000-0000-0000-000000000001`.

Пути в таблицах приведены относительно `/api/v1`. Запросы сохранены в файлах [комнат](../requests/rooms.http), [слотов](../requests/slots.http), [бронирований](../requests/bookings.http) и [проверок ошибок](../requests/week-4-errors.http). У Orion UUID `00000000-0000-0000-0000-000000000001`, у Luna — `00000000-0000-0000-0000-000000000004`.

## Anna — списки и бронирование

| Запрос | HTTP | Фактический результат |
|---|---:|---|
| `GET /rooms?minCapacity=8&page=0&size=2` | 200 | Vega, затем Sirius. `page=0`, `size=2`, `totalElements=3`. |
| Тот же запрос с `page=2` | 200 | `items=[]`, `page=2`, `size=2`, `totalElements=3`. |
| `GET /rooms/{orionId}/slots` за 14 января 2030 года, период `[09:00Z, 11:00Z)` | 200 | Слот на `09:00Z` занят, слот на `10:00Z` свободен. Слот на `11:00Z` за пределами периода. `totalElements=2`. |
| Тот же запрос с `availableOnly=true` | 200 | Только свободный слот на `10:00Z`, `totalElements=1`. |

Для бронирования выбран свободный слот `00000000-0000-0000-0000-000000000002`.

| Запрос | HTTP | Фактический результат |
|---|---:|---|
| `POST /bookings` со свободным `slotId` | 201 | Создана бронь `ee8cce7c-5b0f-41f7-bd44-5a27ff7d30e4` со статусом `ACTIVE`. Заголовок `Location: /api/v1/bookings/ee8cce7c-5b0f-41f7-bd44-5a27ff7d30e4` совпал с `id`. |
| GET по `Location` | 200 | Все восемь полей совпали с ответом на POST. |
| Повторный POST на занятый слот, затем GET первой брони | 409 → 200 | `SLOT_ALREADY_BOOKED`, `fieldErrors=[]`. Первая бронь осталась `ACTIVE` с прежними полями. |
| Два DELETE первой брони, затем GET | 204 → 204 → 200 | Оба DELETE с пустым телом. При чтении изменился только `status` на `CANCELLED`. Сохранились `id` и `createdAt=2026-11-26T08:30:24.447836Z`. |
| POST на освободившийся слот, затем GET новой брони | 201 → 200 | Новая `ACTIVE`-бронь с UUID `b92e0251-58bc-4a61-b47b-8492635f1014` и `createdAt=2026-11-26T08:35:39.617845Z`. |
| `GET /bookings?status=ACTIVE&size=100` и тот же запрос со статусом `CANCELLED` | 200 → 200 | В присланной выборке по выбранному слоту новая бронь найдена в `ACTIVE`, первая — в `CANCELLED`. |

## Anna и Boris — отказы и права доступа

| Пользователь | Запрос | HTTP | Фактический результат |
|---|---|---:|---|
| Anna | `POST /rooms` и `POST /rooms/{orionId}/slots` с корректными телами | 403, 403 | `FORBIDDEN`, `fieldErrors=[]` в обоих ответах. |
| Anna | `POST /bookings` с `slotId="abc"` | 400 | `VALIDATION_ERROR`, ошибка поля `slotId`. |
| Anna | `POST /bookings` с `slotId="ffffffff-ffff-ffff-ffff-ffffffffffff"` | 404 | `SLOT_NOT_FOUND`, `fieldErrors=[]`. |
| Boris | GET и DELETE исходной брони Anna | 403, 403 | `FORBIDDEN`, `fieldErrors=[]` в обоих ответах. |
| Boris | GET слотов Orion после отказа в отмене | 200 | Слот на `09:00Z` остался занят (`available=false`). |

## Vera — доступ администратора

| Запрос | HTTP | Фактический результат |
|---|---:|---|
| GET исходной брони Anna, DELETE и повторный GET | 200 → 204 → 200 | До отмены `ACTIVE`, у DELETE пустое тело. После отмены `CANCELLED`, исходные `id` и `createdAt=2026-10-01T09:01:00Z` сохранились. |
| `GET /bookings` после отмены чужой брони | 200 | Собственная история Vera пуста, `totalElements=0`. |
| GET слотов Luna | 200 | `items=[]`, `totalElements=0`. |
| GET слотов комнаты `ffffffff-ffff-ffff-ffff-ffffffffffff` | 404 | `ROOM_NOT_FOUND`, `fieldErrors=[]`. |
| `POST /rooms` | 201 | Создана комната `fb035ed1-b4e2-4153-a90f-fc595abcb351`. |
| `POST /rooms/{newRoomId}/slots` с интервалом `2026-11-26T09:00:00Z` – `2026-11-26T10:00:00Z` | 201 | Создан слот `18f04ffa-7ccd-47b3-8cac-562ba09f68cd`. `roomId` совпал с UUID новой комнаты, `available=true` на момент ответа. |
| Повтор POST слота с тем же началом в той же комнате | 409 | `SLOT_ALREADY_EXISTS`, `fieldErrors=[]`. |
| POST слота длительностью 59 минут | 400 | `VALIDATION_ERROR`, поле `endsAt`, сообщение «Слот должен длиться 60 минут». |

Для повторения проверки создания с доступным слотом выбери часовой интервал, который ещё впереди.

## Валидация и разбор JSON

Все семь запросов вернули `400 Bad Request` с `Content-Type: application/json`.

| Запрос | `code` | `fieldErrors` |
|---|---|---|
| `GET /bookings?size=101` | `VALIDATION_ERROR` | `size` — размер страницы должен быть от 1 до 100. |
| `GET /bookings?status=PENDING` | `VALIDATION_ERROR` | `status` — допустимы `ACTIVE` и `CANCELLED`. |
| GET слотов Orion с `to=2030-01-14T11:00:00` без смещения | `VALIDATION_ERROR` | `to` — неверный формат параметра. |
| POST брони с корректным `slotId` и дополнительным полем `userId` | `VALIDATION_ERROR` | `userId` — неизвестное поле. |
| POST брони с телом `{"slotId":123}` | `VALIDATION_ERROR` | `slotId` — неверный тип или формат. |
| POST брони с оборванным телом `{"slotId":` | `MALFORMED_JSON` | `[]` |
| POST брони с телом `null {}` | `MALFORMED_JSON` | `[]` |

Для последней проверки отправь `null {}` как исходное тело POST в `/api/v1/bookings` с `Content-Type: application/json`. Ответ подтверждает обработку двух корневых JSON-значений. Этот случай также проверяет [параметризованный тест MockMvc](../src/test/java/com/learning/booking/MeetingRoomBookingApplicationTests.java).

## Параллельные запросы и граничные случаи

- [Параллельный прогон от 25 ноября 2026 года](error-handling-verification.md#параллельный-post-от-anna) зафиксировал `201` и `409 SLOT_ALREADY_BOOKED`, после которых для слота осталась одна `ACTIVE`-бронь с UUID из `Location`.
- [Тесты BookingService](../src/test/java/com/learning/booking/service/BookingServiceTest.java) с фиксированным `Clock` покрывают отказ создания и отмены активной брони при `startsAt == now`. Ошибкам соответствуют `409 SLOT_ALREADY_STARTED` и `409 CANCELLATION_CLOSED`.
- [Тест ApiExceptionHandler](../src/test/java/com/learning/booking/controller/ApiExceptionHandlerTest.java) проверяет публичный ответ `500 INTERNAL_ERROR` при контролируемом исключении. Результат `500` взят из проверки обработчика.
