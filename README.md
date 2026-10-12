# FoodCards System


## Описание проекта
Многомодульная распределенная система для генерации, публикации и обработки карточек товаров. Система построена на микросервисной архитектуре, использует Apache Kafka для асинхронного обмена сообщениями и хранит данные в PostgreSQL и Oracle.

## Модули системы
| Модуль | Описание | Ссылка на документацию | 
|-----------|------------|--------|
| FoodCards-Publisher-System | Ядро системы. Приём файлов (REST/gRPC/FS), валидация, запись в 2 БД. | [Документация Publisher](./FoodCards-Publisher-System/README.md) |
| Mealcard-File-Generator | Генерация тестовых карточек и отправка их в Publisher. | [Документация Generator](./Mealcard-File-Generator/README.md) |
| App-adapter | Интеграционный шлюз. Обработка событий из Kafka и работа с Oracle. | [Документация Adapter](./App-adapter/README.md) |

## Структура проекта

<details>
  <summary>Нажмите, чтобы посмотреть структуру папок</summary>
  
FoodCards/
── App-adapter/              # Модуль адаптера
│   ├── build.gradle
│   ├── src/
│   ── Dockerfile
├── FoodCards-Publisher-System/  # Модуль Publisher
│   ├── build.gradle
│   ├── src/
│   └── Dockerfile
├── Mealcard-File-Generator/  # Модуль Generator
│   ├── build.gradle
│   ├── src/
│   └── Dockerfile
├── init-postgres/            # Скрипты инициализации PostgreSQL
├── init-oracle/              # Скрипты инициализации Oracle
├── build.gradle              # Корневой build.gradle
├── settings.gradle           # Настройки Gradle
├── docker-compose.yml        # Docker Compose конфигурация
└── gradlew                   # Gradle wrapper

</details>

## Ключевые возможности

- 3 независимых микросервиса: Publisher (Spring Boot), Generator (Java HTTP), App-Adapter (Spring Boot + Kafka)
- 4 канала поступления файлов в Publisher: файловая система (сканер), REST multipart, REST chunked, gRPC streaming
- Двойная база данных: PostgreSQL (POM) + Oracle (GRU) с изолированными транзакциями и независимыми EntityManagerFactory
- Автоматическое управление файлами: сканер директорий по расписанию, переименование, сортировка по папкам YYYYMMDD
- Асинхронная коммуникация: Apache Kafka для обмена событиями между App-Adapter и внешними системами
- Внешняя конфигурация: application.yml и logback.xml монтируются через volume без пересборки образа

## Технологический стек

- Язык: Java 21
- Фреймворк: Spring Boot 3.2.5 (для Publisher и App-Adapter)
- Сборка: Gradle 8.14 (с использованием Shadow Plugin)
- Инфраструктура: Docker, Docker Compose
- Базы данных: PostgreSQL 15, Oracle XE 21
- Коммуникация: gRPC, REST, Apache Kafka 3.7


## Переменные окружения
### Publisher 
| Переменная | Пример значения | Описание |
|-----------|------------|--------|
| POSTGRES_PASSWORD | my_secure_pg_pass | Пароль для пользователя pom в PostgreSQL
| GRU_APP_PASSWORD | my_gru_app_pass | Пароль для схемы GRU в Oracle |
| RECEIVER_DATASOURCE_POM_URL | jdbc:postgresql://pom_db:5432/pom_db | JDBC URL для PostgreSQL |
| RECEIVER_DATASOURCE_GRU_URL | jdbc:oracle:thin:@gru_db:1521/XEPDB1 | JDBC URL для Oracle |
| APP_FILESPATHS_PROCESS_DIR | /app/incoming | Директория для входящих файлов |
| APP_FILESPATHS_TARGET_DIR | /app/target | Директория для обработанных файлов |

### Generator
| Переменная | Пример значения | Описание |
|-----------|------------|--------|
| SEND_URL | http://publisher:666/files | URL Publisher для отправки файлов |
| GRPC_HOST | publisher | Хост gRPC сервера Publisher |
| GRPC_PORT | 6666 | Порт gRPC сервера Publisher |

### App-Adapter
| Переменная | Пример значения | Описание |
|-----------|------------|--------|
| SPRING_KAFKA_BOOTSTRAP_SERVERS | kafka:9092 | Адрес Kafka broker |
| GRU_APP_PASSWORD | my_gru_app_pass | Пароль для схемы GRU в Oracle |
	
## Запуск системы
```bash
# Собрать образы и запустить все сервисы в фоновом режиме
docker-compose up -d --build

# Посмотреть логи всех сервисов
docker-compose logs -f

# Остановить систему (сохраняя данные в Docker volumes)
docker-compose down
```
