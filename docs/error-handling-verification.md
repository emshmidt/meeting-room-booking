# Проверка общего обработчика ошибок

Неделя 4, день 4. Ручные запросы модуля 1 выполнены 19 ноября 2026 года. Для воспроизведения используются [запросы модуля](../requests/week-4-errors.http) и [контракт ошибок](api-contract.md#единый-формат-ошибки).

## Ручной прогон на localhost:8080

Первые два POST выполнились в одном процессе от Anna на слоте Orion `00000000-0000-0000-0000-000000000002` с началом в `2030-01-14T10:00:00Z`. Время приведено из заголовка `Date` и пересчитано на Москву (UTC+3).

| Время, Москва | Запрос | HTTP | Фактический результат |
|---|---|---:|---|
| 16:03:58 | POST брони на свободный слот | 201 | Тело содержит `ACTIVE` и `id=671f9ac5-f2a8-4486-b885-a8fdebf04fa1`. Заголовок `Location: /api/v1/bookings/671f9ac5-f2a8-4486-b885-a8fdebf04fa1` совпадает с `id`. `createdAt=2026-11-19T13:03:58.255752Z`. |
| 16:04:21 | Повторный POST с тем же `slotId` | 409 | `application/json`, `SLOT_ALREADY_BOOKED`, сообщение «Слот уже забронирован», `fieldErrors=[]`. |
| 16:39:06 | PUT `/api/v1/bookings` | 405 | `Allow: GET, POST`, тип тела `application/problem+json`. Копия тела в исходном выводе обрывается после `title`. |
| 16:39:21 | POST `/api/v1/bookings` с `Content-Type: text/plain` | 415 | `Accept: application/json, application/*+json`, тип тела `application/problem+json`, `status=415`, `title="Unsupported Media Type"`. |
| 16:43:47 | POST брони с `slotId=ffffffff-ffff-ffff-ffff-ffffffffffff` | 404 | `application/json`, `SLOT_NOT_FOUND`, сообщение «Слот не найден», `fieldErrors=[]`. |

## Повторная проверка модуля 1

`./mvnw -o clean verify` в копии текущих исходников завершился `BUILD SUCCESS`. Surefire: 7 тестов, 0 падений, 0 ошибок, 0 пропущено. В журнале тестового запуска есть исходный `IllegalStateException` с тестовым сообщением и стеком.

Для HTTP-проверки использованы свежие отдельные процессы Anna и Vera и свободные локальные порты.

| Сценарий | HTTP | Фактический результат |
|---|---:|---|
| Anna, POST свободного слота | 201 | `ACTIVE`, `Location` совпадает с `id` из тела. |
| Anna, повторный POST | 409 | `SLOT_ALREADY_BOOKED`, `fieldErrors=[]`. |
| Anna, GET по `Location` после конфликта | 200 | Тело совпадает с успешным POST. |
| Anna, POST с неизвестным `slotId` | 404 | `SLOT_NOT_FOUND`, `fieldErrors=[]`. |
| Anna, GET неизвестной брони | 404 | `BOOKING_NOT_FOUND`, `fieldErrors=[]`. |
| Anna, GET слотов неизвестной комнаты | 404 | `ROOM_NOT_FOUND`, `fieldErrors=[]`. |
| Anna, POST комнаты от USER | 403 | `FORBIDDEN`, `fieldErrors=[]`. |
| PUT коллекции бронирований | 405 | `Allow: GET, POST`, корректный JSON `application/problem+json`, `status=405`. |
| POST брони с `text/plain` | 415 | `Accept: application/json, application/*+json`, `application/problem+json`, `status=415`. |
| Anna, DELETE созданной брони | 204 | Тело пустое. |
| Anna, GET истории с `size=` | 400 | Ответ MVC сохраняет статус ошибки входного параметра. |
| Vera, повтор слота с эквивалентным смещением | 409 | `SLOT_ALREADY_EXISTS`, `fieldErrors=[]`. |
| Vera, POST слота с началом в прошлом | 201 | `available=false`, получен UUID для следующего запроса. |
| Vera, POST брони уже начавшегося слота | 409 | `SLOT_ALREADY_STARTED`, `fieldErrors=[]`. |

## Что подтверждает проверка кода

- В [ApiExceptionHandler](../src/main/java/com/learning/booking/controller/ApiExceptionHandler.java) `403`, три варианта `404` и четыре варианта `409` сопоставлены с кодами контракта. Обработчик `500` принимает неожиданные исключения, включая `BookingSlotMissingException`. Журнал получает исходное исключение, публичное тело содержит нейтральное сообщение.
- [ApiExceptionHandlerTest](../src/test/java/com/learning/booking/controller/ApiExceptionHandlerTest.java) напрямую вызывает обработчик с `IllegalStateException` и проверяет статус `500`, `INTERNAL_ERROR`, пустой список и отсутствие тестовой внутренней строки в сообщении. Это проверка возвращаемого DTO. Полный HTTP-путь для `500` не входит в этот прогон. По времени отмены `BookingServiceTest` проверяет `CancellationClosedException` на границе начала слота, а соответствие `409 CANCELLATION_CLOSED` сверено в advice.
- Конфликты и отсутствие ресурсов возвращают DTO проекта. `405` и `415` обрабатывает Spring со своими заголовками. Согласование ошибок JSON, DTO, path/query и проверок времени со схемой `VALIDATION_ERROR` относится к модулю 2.

## Модуль 2. Повторная проверка 24 ноября 2026 года

На копии текущих исходников выполнен `./mvnw -o clean verify`. `BUILD SUCCESS`, тестов 7, падений 0, ошибок 0, пропусков 0. Свежий JAR запускался в двух отдельных процессах от Anna и Vera на свободных локальных портах.

Выполнен 21 HTTP-запрос. Для `400` сверены `Content-Type: application/json`, код, сообщение и точные имена полей внутри `fieldErrors`. Запросы повторяются из [week-4-errors.http](../requests/week-4-errors.http).

| Пользователь | Запрос | HTTP | Фактический результат |
|---|---|---:|---|
| Anna | История без параметра size | 200 | `size=20`, `totalElements=3`. |
| Anna | Оборванный JSON брони | 400 | `MALFORMED_JSON`, `fieldErrors=[]`. |
| Anna | Неверный UUID в slotId | 400 | `VALIDATION_ERROR`, поля `slotId`. |
| Anna | Неизвестное поле userId | 400 | `VALIDATION_ERROR`, поля `userId`. |
| Anna | Пустой объект брони | 400 | `VALIDATION_ERROR`, поля `slotId`. |
| Anna | Явный null в slotId | 400 | `VALIDATION_ERROR`, поля `slotId`. |
| Anna | Пустое обязательное тело | 400 | `VALIDATION_ERROR`, `fieldErrors=[]`, «Тело запроса обязательно». |
| Anna | JSON null вместо объекта брони | 400 | `VALIDATION_ERROR`, `fieldErrors=[]`, «Тело запроса обязательно». |
| Anna | Массив вместо объекта брони | 400 | `VALIDATION_ERROR`, `fieldErrors=[]`. |
| Anna | Неверный UUID в пути брони | 400 | `VALIDATION_ERROR`, поля `id`. |
| Anna | Пустой параметр size | 400 | `VALIDATION_ERROR`, поля `size`. |
| Anna | Список слотов без from | 400 | `VALIDATION_ERROR`, поля `from`. |
| Anna | Параметр to без смещения | 400 | `VALIDATION_ERROR`, поля `to`. |
| Anna | Обратный порядок границ периода | 400 | `VALIDATION_ERROR`, поля `to`. |
| Anna | PUT коллекции бронирований | 405 | `Allow: POST, GET`, `application/problem+json`. |
| Anna | POST брони с text/plain | 415 | `Accept: application/json, application/*+json`, `application/problem+json`. |
| Anna | История после отклонённых запросов | 200 | Ответ совпадает с начальным, на странице сохранились три исходные брони. |
| Vera | Слот без endsAt | 400 | `VALIDATION_ERROR`, поля `endsAt`. |
| Vera | Слот длительностью 59 минут | 400 | `VALIDATION_ERROR`, поля `endsAt`. |
| Vera | Строка вместо числа в capacity | 400 | `VALIDATION_ERROR`, поля `capacity`. |
| Vera | Создание комнаты с hasProjector: false | 201 | Создана Atlas, `capacity=4`, `hasProjector=false`. |

У слота без `endsAt` отказ срабатывает на Bean Validation. Слот длительностью 59 минут отклоняет `SlotService`. У обоих одинаковый код и поле. Проверка пустого тела и JSON null подтверждает [формулировку в контракте](api-contract.md#единый-формат-ошибки).

### Примеры фактических ответов 400

**Оборванный JSON**

```json
{
  "code": "MALFORMED_JSON",
  "message": "Не удалось разобрать тело запроса",
  "fieldErrors": []
}
```

**Пустое обязательное тело**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Тело запроса обязательно",
  "fieldErrors": []
}
```

**Пустой параметр size**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Некорректные данные запроса",
  "fieldErrors": [
    {
      "field": "size",
      "message": "Ожидается целое число"
    }
  ]
}
```

## Модуль 3. Unit-тесты бронирования

25 ноября 2026 года выполнен `./mvnw test`. Итог сверён с четырьмя отчётами Surefire.

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

Из них 7 тестов в [BookingServiceTest](../src/test/java/com/learning/booking/service/BookingServiceTest.java), по одному в `RoomServiceTest`, `ApiExceptionHandlerTest` и `MeetingRoomBookingApplicationTests`.

- `shouldAllowRebookingAfterCancellation` проверяет сохранение `createdAt` при отмене и создание новой `ACTIVE`-брони с другим UUID. Первая бронь остаётся в истории со статусом `CANCELLED`.
- `shouldRejectBookingWhenSlotStartsNow` проверяет `SlotAlreadyStartedException` на точной временной границе и пустое хранилище после отказа.
- `shouldPaginateBookingHistoryAfterFilteringByUserAndStatus` проверяет порядок двух отменённых броней Anna, пустую третью страницу и `totalElements=2` на всех трёх страницах.

## Модуль 4. Параллельная бронь и сквозная проверка

Ручной прогон 25 ноября 2026 года. Для параллельного POST и проверок состояния использовался один процесс с `--app.demo-user=anna`. GET для проверки прав выполнялись после отдельного запуска с `--app.demo-user=boris`. Время в таблице пересчитано из HTTP-заголовка `Date` на Москву (UTC+3). Для запросов с выводом через `jq` сохранён результат фильтрации.

### Параллельный POST от Anna

В `12:38:45` GET слотов Orion вернул `200`. Слот `00000000-0000-0000-0000-000000000002` на `2030-01-14T10:00:00Z–11:00:00Z` был свободен (`available=true`). В ответе `page=0`, `size=20` и `totalElements=1`.

На тот же `slotId` отправлены два фоновых `curl`. Статус, заголовки и тело каждого запроса сохранены раздельно. Оба ответа датированы `12:39:10` и имеют `Content-Type: application/json`.

**Запрос A — `409 Conflict`**

```json
{"code":"SLOT_ALREADY_BOOKED","message":"Слот уже забронирован","fieldErrors":[]}
```

**Запрос B — `201 Created`**

`Location: /api/v1/bookings/17b12549-9c35-4030-a060-e25149d9055f`

```json
{
  "id": "17b12549-9c35-4030-a060-e25149d9055f",
  "userId": "00000000-0000-0000-0000-000000000001",
  "slotId": "00000000-0000-0000-0000-000000000002",
  "status": "ACTIVE",
  "createdAt": "2026-11-25T09:39:10.278051Z",
  "roomId": "00000000-0000-0000-0000-000000000001",
  "startsAt": "2030-01-14T10:00:00Z",
  "endsAt": "2030-01-14T11:00:00Z"
}
```

После обеих попыток `GET /api/v1/bookings?status=ACTIVE&size=100` с фильтрацией по `slotId` показал одну `ACTIVE`-бронь. Её UUID совпал с `Location` и `id` из ответа B.

```json
{"count":1,"ids":["17b12549-9c35-4030-a060-e25149d9055f"]}
```

### Отмена, повторная бронь и отказ при неверном вводе

Все запросы выполнены в том же процессе Anna.

| Время, Москва | Запрос | HTTP | Фактический результат |
|---|---|---:|---|
| 12:42:07 | GET первой брони по `Location` | 200 | `ACTIVE`. Поля в присланном выводе совпадают с ответом B. |
| 12:42:21 | DELETE первой брони | 204 | Пустое тело. |
| 12:42:39 | Повтор DELETE первой брони | 204 | Пустое тело. |
| 12:42:57 | GET первой брони после отмены | 200 | `CANCELLED`, тот же UUID и `createdAt=2026-11-25T09:39:10.278051Z`. |
| 12:43:14 | POST брони на освободившийся слот | 201 | `ACTIVE`, новый UUID `d8d511b6-a538-4106-b2b8-89cae346b086` в теле и `Location`, `createdAt=2026-11-25T09:43:14.529132Z`. |
| 12:44:09 | POST с `{"slotId":"abc"}` | 400 | `VALIDATION_ERROR`, ошибка поля `slotId` с сообщением «Значение имеет неверный тип или формат». |

До запроса с `slotId="abc"` в истории Anna было `totalElements=5`. Для выбранного слота в ней видны новая `ACTIVE` с UUID `d8d511b6-a538-4106-b2b8-89cae346b086`, отменённая `17b12549-9c35-4030-a060-e25149d9055f` и исходная `CANCELLED` с UUID `00000000-0000-0000-0000-000000000002`. После `400` контрольный GET снова показал `totalElements=5`.

### Проверка от Boris

| Время, Москва | Запрос | HTTP | Фактический результат |
|---|---|---:|---|
| 12:46:17 | GET `/api/v1/bookings/00000000-0000-0000-0000-000000000001` | 403 | `FORBIDDEN`, `fieldErrors=[]`. |
| 12:46:32 | GET `/api/v1/bookings/ffffffff-ffff-ffff-ffff-ffffffffffff` | 404 | `BOOKING_NOT_FOUND`, `fieldErrors=[]`. |

Оба ответа имели `Content-Type: application/json`.

```json
{"code":"FORBIDDEN","message":"Недостаточно прав для выполнения операции","fieldErrors":[]}
```

```json
{"code":"BOOKING_NOT_FOUND","message":"Бронирование не найдено","fieldErrors":[]}
```

### Воспроизведение

На свежем запуске от Anna убедиться, что слот свободен, и отправить два POST в фоне. Каждый запрос пишет статус, заголовки и тело в отдельные файлы.

```sh
booking_api=http://localhost:8080/api/v1
slot_id=00000000-0000-0000-0000-000000000002
race_dir=$(mktemp -d)

post_booking() {
  curl -sS --max-time 10 \
    -D "$race_dir/$1.headers" -o "$race_dir/$1.json" \
    -w '%{http_code}\n' \
    -H 'Content-Type: application/json' -H 'Accept: application/json' \
    -d "{\"slotId\":\"$slot_id\"}" "$booking_api/bookings" > "$race_dir/$1.status"
}

post_booking A &
request_a=$!
post_booking B &
request_b=$!
wait "$request_a"
wait "$request_b"

for request in A B; do
  echo "Запрос $request"
  cat "$race_dir/$request.status" "$race_dir/$request.headers" "$race_dir/$request.json"
  echo
done

curl -fsS "$booking_api/bookings?status=ACTIVE&size=100" -H 'Accept: application/json' |
  jq --arg slotId "$slot_id" '.items | map(select(.slotId == $slotId)) | {count: length, ids: map(.id)}'
```

В следующих запросах подставить UUID из фактического `Location`. Запросы и порядок смены пользователя указаны в [week-4-errors.http](../requests/week-4-errors.http).
