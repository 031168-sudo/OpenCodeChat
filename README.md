# OpenCodeChat

Нативный Android-клиент для сервера **OpenCode** (Kotlin + Jetpack Compose).
Общается с API `opencode` v2 напрямую (`/api/...`), без WebView.

## Возможности

- проекты и сессии, история сообщений;
- потоковый ответ через SSE (`/api/event`), индикатор работы;
- выбор модели и агента, уровень размышлений;
- слэш-команды (`/new`, `/undo`, `/compact`, `/fork`, `/export`, `/share`);
- разрешения и формы (вопросы) opencode;
- вложения-файлы и изображения в сообщениях;
- файлы проекта: просмотр, поиск, загрузка, создание папок,
  переименование и удаление;
- `@` — выбор файла проекта в контекст запроса;
- использование по сессии и суммарно по проекту;
- оформление: авто/светлая/тёмная тема, шрифт (системный, Sans, Lora,
  Roboto Mono) и размер шрифта;
- кэш последней сессии, чтобы приложение быстро открывалось.

## Сборка

```bash
ANDROID_HOME=/home/gmpony/Android/Sdk \
/home/gmpony/opt/gradle/gradle-8.11.1/bin/gradle :app:assembleDebug --offline
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Настройка

При первом запуске укажи адрес сервера opencode, логин и пароль
(HTTP Basic). По умолчанию используется `https://gmpony.alfanomy.ru`.

## APK

https://diag.alfanomy.ru/apk/opencode-chat-debug.apk

## Документация

Подробности по API, архитектуре и изменениям — в [AGENTS.md](AGENTS.md).
