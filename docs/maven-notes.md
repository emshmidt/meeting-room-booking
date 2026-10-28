координаты проекта: groupId, artifactId, version;

com.learning;
finance-tracker;
1.0-SNAPSHOT;

выбранную версию Java: 21;
две прямые зависимости, их версии и scopes;

1. JUnit
   <dependency>
   <groupId>org.junit.jupiter</groupId>
   <artifactId>junit-jupiter</artifactId>
   <version>5.11.4</version>
   <scope>test</scope>
   </dependency>
2. Apache Commons CSV
   <dependency>
   <groupId>org.apache.commons</groupId>
   <artifactId>commons-csv</artifactId>
   <version>1.14.1</version>
   </dependency>


версии Surefire и Shade: 3.5.4, 3.6.2;

фазу и goal, указанные для Shade: package, shade

главный класс приложения: com.learning.Main;

версию Wrapper и версию Maven: 3.3.4, 3.9.16.

# Дерево зависимостей

```text
[INFO] com.learning:finance-tracker:jar:1.0-SNAPSHOT
[INFO] +- org.junit.jupiter:junit-jupiter:jar:5.11.4:test
[INFO] |  +- org.junit.jupiter:junit-jupiter-api:jar:5.11.4:test
[INFO] |  |  +- org.opentest4j:opentest4j:jar:1.3.0:test
[INFO] |  |  +- org.junit.platform:junit-platform-commons:jar:1.11.4:test
[INFO] |  |  \- org.apiguardian:apiguardian-api:jar:1.1.2:test
[INFO] |  +- org.junit.jupiter:junit-jupiter-params:jar:5.11.4:test
[INFO] |  \- org.junit.jupiter:junit-jupiter-engine:jar:5.11.4:test
[INFO] |     \- org.junit.platform:junit-platform-engine:jar:1.11.4:test
[INFO] \- org.apache.commons:commons-csv:jar:1.14.1:compile
[INFO]    +- commons-io:commons-io:jar:2.20.0:compile
[INFO]    \- commons-codec:commons-codec:jar:1.19.0:compile
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

Две прямые зависимости проекта: org.junit.jupiter:junit-jupiter, org.apache.commons:commons-csv.
Хотя бы одну транзитивную зависимость: org.opentest4j:opentest4j.
Через какую библиотеку она пришла: FinanceTracker → junit-jupiter → junit-jupiter-api → opentest4j..
Её выбранную версию и scope: 1.3.0:test.

# effective POM

**Свойства компиляции Java:** `maven.compiler.source=21` и `maven.compiler.target=21`.

**Maven Compiler Plugin:** `maven-compiler-plugin`, версия `3.15.0`.

- Фаза `compile` → goal `compile` — компиляция основного кода.
- Фаза `test-compile` → goal `testCompile` — компиляция тестового кода.

**Surefire:** `maven-surefire-plugin`, версия `3.5.4`; фаза `test` → goal `test` — выполнение модульных тестов.

**Shade:** `maven-shade-plugin`, версия `3.6.2`; фаза `package` → goal `shade` — создание JAR с зависимостями.

**Главный класс:** `com.learning.Main`, указанный в transformer манифеста Shade.

## Какие настройки объявлены непосредственно у нас?

В исходном pom.xml FinanceTracker явно заданы:

- Координаты проекта: `com.learning:finance-tracker:1.0-SNAPSHOT` и версия модели POM `4.0.0`.
- Свойства `maven.compiler.source=21`, `maven.compiler.target=21` и кодировка `project.build.sourceEncoding=UTF-8`.
- Прямая зависимость `org.junit.jupiter:junit-jupiter:5.11.4` со scope `test`.
- Прямая зависимость `org.apache.commons:commons-csv:1.14.1`. Её scope в исходном POM не указан.
- Плагин Surefire версии `3.5.4`. Его execution и фазу test мы вручную не описывали.
- Плагин Shade версии `3.6.2`: goal `shade` явно привязан к фазе `package`.
- Настройки Shade: `createDependencyReducedPom=false` и transformer манифеста с главным классом `com.learning.Main`.

Свойства `maven.compiler.source/target` — параметры компиляции, а не объявление Maven Compiler Plugin. Сам плагин и его execution видны в effective POM дополнительно.

## Какие элементы появились в итоговой модели дополнительно?

При сравнении с сохранённым target/day5-effective-pom.xml видны следующие дополнения:

- Для Commons CSV явно записан scope `compile`, который применяется по умолчанию. Это уточнение существующей зависимости, а не добавление новой.
- Добавлены репозитории библиотек и плагинов Maven Central с адресом `https://repo.maven.apache.org/maven2`; получение snapshot-артефактов из этих записей отключено.
- Указаны стандартные каталоги исходников, тестов и ресурсов: `src/main/java`, `src/test/java`, `src/main/resources`, `src/test/resources`. Пути раскрыты до абсолютных.
- Указаны каталоги результатов: `target`, `target/classes`, `target/test-classes`, а также итоговое имя артефакта `finance-tracker-1.0-SNAPSHOT`.
- Добавлен `maven-compiler-plugin:3.15.0` с execution для `compile` и `test-compile` и goals `compile`/`testCompile`.
- Для уже объявленного Surefire появился execution `default-test`: фаза `test`, goal `test`.
- Видны стандартные плагины и их привязки: Clean, Resources, JAR, Install, Deploy и Site. Например, `maven-jar-plugin:3.5.0` выполняет goal `jar` на фазе `package`.
- Появился раздел `pluginManagement` с управляемыми версиями Antrun, Assembly, Dependency и Release. Наличие плагина в этом разделе само по себе не означает, что его goal выполнится при каждой сборке.
- Добавлены настройки отчётности с каталогом `target/site`.

Версии выше взяты из конкретного сохранённого effective POM. Присутствие Install/Deploy в итоговой модели не означает их выполнение командой `clean verify`: до этих фаз она не доходит.

## Почему effective POM подробнее исходного файла?

Исходный pom.xml содержит настройки, которые мы задали для проекта. Maven дополняет их унаследованными настройками Super POM, стандартными привязками lifecycle для типа упаковки проекта и значениями по умолчанию; при наличии активных профилей учитывается и их вклад. Если у проекта есть явно указанный родительский POM, его настройки также участвуют в итоговой модели; у FinanceTracker элемента parent нет.

Effective POM показывает объединённую модель для конкретной сборки: поэтому в нём видны дополнительные плагины, executions, репозитории, каталоги и развёрнутые значения. Например, мы указали только версию Surefire, а стандартная привязка добавила его запуск на фазе test.

Это помогает понять, какие настройки фактически использует Maven. Effective POM не является журналом выполненной сборки и не заменяет dependency:tree для просмотра транзитивных зависимостей.

# Сборка

## Результаты проверки

Команда задания: `./mvnw clean verify` из каталога FinanceTracker.

Результаты проверены 28 октября 2026 года по сохранённым отчётам Surefire, содержимому JAR и текущему pom.xml. При заполнении заметок команда повторно не запускалась. Полный журнал терминала недоступен, поэтому точная завершающая строка BUILD SUCCESS и код завершения команды отдельно не подтверждены.

**Подтверждённый результат:** тесты завершились успешно; JAR с зависимостями сформирован.

| Показатель | Фактическое значение |
|---|---:|
| Тестов по отчётам Surefire | 196 |
| Падений — failures | 0 |
| Ошибок — errors | 0 |
| Пропусков — skipped | 0 |
| Отчётов тестовых классов | 9 |

Числа получены суммированием атрибутов tests/failures/errors/skipped в файлах `target/surefire-reports/TEST-*.xml`, а не перенесены из результатов прежних занятий.

## Surefire и Shade

- **Surefire 3.5.4:** сохранены XML- и текстовые отчёты девяти тестовых классов в `target/surefire-reports`. Они подтверждают выполнение 196 тестов с нулевыми failures/errors/skipped. Плагин выполняет goal `test` на фазе `test`.
- **Shade 3.6.2:** в pom.xml его goal `shade` привязан к `package`. Результат упаковки подтверждён содержимым итогового JAR: присутствуют классы приложения, Commons CSV, Commons IO и Commons Codec. Классов пакета org.junit внутри нет. В манифесте записан `Main-Class: com.learning.Main`.
- Дословные сообщения запуска плагинов из консоли не сохранены в доступных материалах. Выводы выше основаны на отчётах, артефактах и настройках; это не цитата журнала сборки.

## Наличие артефакта

Файл `target/finance-tracker-1.0-SNAPSHOT.jar` существует; размер — 1 045 727 байт. Время изменения — 28 октября 2026 года, 12:38:37 по Москве.

Также существует `target/original-finance-tracker-1.0-SNAPSHOT.jar` размером 60 792 байта — исходный JAR до замены результатом Shade.

## Почему тесты выполнялись до упаковки?

В default lifecycle фаза test предшествует package. При выполнении verify Maven сначала проходит предшествующие фазы: компилирует основной код и тесты, запускает Surefire, затем упаковывает приложение. При обычной конфигурации падение тестов останавливает сборку до упаковки. Отчёты этой сборки показывают отсутствие падений и ошибок.

## Почему запустился Shade?

В исходном pom.xml есть явный execution: фаза package, goal shade. Поэтому при достижении package Maven формирует обычный JAR и выполняет настроенную обработку Shade для создания JAR с зависимостями и главным классом. Указывать Shade отдельным аргументом команды не требуется.

## Дошла ли команда до install?

Команда clean verify заканчивает default lifecycle на verify. Фазы install и deploy идут дальше, поэтому эта команда их не выполняет и не устанавливает полученный артефакт в локальный Maven-репозиторий как результат install. Наличие ранее установленной версии в репозитории не доказывает выполнение install в этом запуске.

## Какие дополнительные проверки выполнялись на verify?

В текущем pom.xml нет дополнительных goals, привязанных к verify: интеграционные тесты, Checkstyle, анализ покрытия и другие проверки на этой фазе не настроены. Само достижение verify не добавляет их автоматически.

Фактически подтверждены модульные тесты Surefire на фазе test и наличие результата упаковки package. Shade выполняет упаковку, а не дополнительную проверку качества. Без полного журнала нельзя дословно восстановить последовательность всех сообщений конкретного запуска.

# Выводы

Зачем Commons CSV нужен при обычном запуске? Для работы с CSV файлами.
Почему JUnit не требуется основному приложению? Он нужен только для тестов в процессе сборки.
Что добавляет Shade к упаковке? Добавляет зависимости к JAR.
Почему scope зависимости сам по себе не встраивает её в JAR? Scope определяет, на каких этапах зависимость доступна в classpath: при компиляции, тестировании и запуске. Включение библиотек внутрь JAR настраивается отдельно — у нас через Shade.