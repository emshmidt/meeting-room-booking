# Запрос

```bash
curl -i --max-time 15 https://example.com/
```

# Ответ

```http
HTTP/2 200

date: Mon, 26 Oct 2026 14:43:02 GMT

content-type: text/html; charset=utf-8

server: cloudflare

last-modified: Sat, 24 Oct 2026 20:05:47 GMT

allow: GET, HEAD

accept-ranges: bytes

age: 385

cf-cache-status: HIT

cf-ray: a50a3b263a90e4db-RIX

alt-svc: h3=":443"; ma=86400


<!doctype html><html lang=en><head><meta charset=utf-8><link rel=icon href=data:,><meta name=viewport content="width=device-width,initial-scale=1"><title>Example Domain</title><style>html{color-scheme:light dark;background:light-dark(#eee,#222)}body{font:16px/1.6 system-ui,sans-serif;max-width:26em;margin:auto;padding:25vh 2em 2em;text-align:center}</style></head><body><p>This domain is for use in documentation examples without needing permission. This is not a service; avoid relying on it for testing and monitoring purposes.</p><script src=/s.js></script></body></html>

```

1. Версия HTTP: HTTP/2, код статуса: 200. 
Полученный код означает: Успешная обработка.
2. Заголовки ответа:
Дата date: Mon, 26 Oct 2026 14:43:02 GMT

Тип ответа content-type: text/html; charset=utf-8

Сервер server: cloudflare

Последние изменения last-modified: Sat, 24 Oct 2026 20:05:47 GMT

3. Content-Type соответствует html формату.
4. Тело. Оно начинается с `<!doctype html>` и содержит код страницы с
надписью: 

This domain is for use in documentation examples without needing permission. This is not a service; avoid relying on it for testing and monitoring purposes..

# Подробный режим
```bash
curl -v --max-time 15 https://example.com/
```

# Ответ
Тело:

```html
<!doctype html> ... </html>
```

```text
> GET / HTTP/2
```

отправленный метод: GET 

путь ресурса: / 

сведения о сервере назначения: 
```text
> Host: example.com
```

Имя хоста: example.com.
Порт: для HTTPS по умолчанию 443.

Короткое сравнение -i и -v: при запуске подробного запроса появились 
дополнительные данные `>` обычно относятся к отправляемым данным, с `<` — к полученным заголовкам. 
Там также появились служебные сообщения о соединении.

В будущем сервисе бронирования сформирует HTTP 409 при конфликте активной брони
приложение при распознании конкретного конфликт занятости слота.

# День 5. Практика HTTP и JSON

Сценарий: пользователь выбирает свободный будущий слот и хочет его забронировать.

Метод HTTP POST

Путь /api/v1/bookings.

Заголовок, описывающий формат отправляемого тела.
Content-Type: application/json

Заголовок, обозначающий ожидаемый формат ответа.
Accept: application/json

JSON-тело с единственным полем slotId. 
{"slotId": "00000000-0000-0000-0000-000000000007"}

POST /api/v1/bookings HTTP/2
Content-Type: application/json
Accept: application/json

```json
{"slotId": "00000000-0000-0000-0000-000000000007"}
```

Для одного запроса рассмотрим разные условия:

Слот существует, свободен и ещё не начался	Статус успеха, адрес созданной брони, основные поля ответа

201 Created 
Location: /api/v1/bookings/40000000-0000-0000-0000-000000000015

```json
 {"id": "40000000-0000-0000-0000-000000000015", "userId": "00000000-0000-0000-0000-000000000002",
"slotId":  "00000000-0000-0000-0000-000000000007", "status": "ACTIVE"}
```
В JSON пропущена закрывающая фигурная скобка	Статус и причина отказа

400 Bad Request
```json
{"code": "MALFORMED_JSON", "message": "Bad json"}
```

UUID записан правильно, но такого слота нет	Статус и причина отказа

404 Not Found
```json
{"code": "NOT_FOUND_SLOT", "message": "Slot with uuid not found"}
```

Слот существует, но уже имеет ACTIVE-бронь	Статус и причина отказа

409 Conflict
```json
{"code": "SLOT_TAKEN", "message": "Slot already taken"}
```

Случай со сломанным JSON отличается от корректного JSON, содержащего slotId со значением "abc" тем,
что пропущенная фигурная скобка — ошибка синтаксиса JSON, тело не удаётся разобрать;
slotId со значением "abc" — JSON разобран успешно, но значение не соответствует формату UUID. 

Опиши состояние данных:
1. После первого успешного POST. 1 запись, код 201
2. После повторного POST того же слота, отклонённого ограничением уникальности. 1 запись, код 409
3. После первой отмены брони. 1 запись со статусом отменена, код 204
4. После повторной отмены. 1 запись со статусом отменена, код 204


Для обязательной положительной целочисленной capacity рассмотри:
- передано число 4; json корректен, правилам соответствует
- передана строка "4"; json корректен, правилам не соответствует
- передан null; json корректен, правилам не соответствует
- поле отсутствует. json корректен, правилам не соответствует

