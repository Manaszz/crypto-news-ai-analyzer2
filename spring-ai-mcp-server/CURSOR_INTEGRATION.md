# Интеграция Crypto MCP Server с Cursor

## 🚀 Быстрый старт

### 1. Запуск MCP сервера
```bash
# Убедитесь что сервер запущен
docker ps | grep crypto-mcp-server

# Если не запущен, запустите:
docker run -d --name crypto-mcp-server -p 8080:8080 -p 8089:8089 crypto-mcp-server
```

### 2. Проверка работоспособности
```bash
curl http://localhost:8080/actuator/health
```

### 3. Настройка в Cursor

#### Вариант A: SSE транспорт (рекомендуется для реального времени)

1. **Откройте настройки Cursor**: `Cmd/Ctrl + ,`
2. **Перейдите**: Features → Model Context Protocol
3. **Добавьте конфигурацию**:

```json
{
  "mcpServers": {
    "crypto-news-sse": {
      "name": "Crypto News MCP Server (SSE)",
      "transport": {
        "type": "sse",
        "url": "http://localhost:8089/mcp/sse",
        "timeout": 30000
      },
      "description": "Crypto News MCP Server с подключением через Server-Sent Events для анализа криптовалютных новостей в реальном времени"
    }
  }
}
```

#### Вариант B: HTTP транспорт

1. **Откройте настройки Cursor**: `Cmd/Ctrl + ,`
2. **Перейдите**: Features → Model Context Protocol
3. **Добавьте конфигурацию**:

```json
{
  "mcpServers": {
    "crypto-news-analyzer": {
      "url": "http://localhost:8080",
      "transport": "http",
      "description": "Crypto News Analysis with AI"
    }
  }
}
```

#### Вариант C: Через Docker exec (альтернативный)

```json
{
  "mcpServers": {
    "crypto-news-analyzer": {
      "command": "docker",
      "args": [
        "exec",
        "-i",
        "crypto-mcp-server",
        "java",
        "-Dspring.ai.mcp.server.transport.stdio.enabled=true",
        "-jar",
        "/app/crypto-mcp-server.jar"
      ],
      "description": "Crypto News Analysis with AI"
    }
  }
}
```

### 4. Перезапуск Cursor
После добавления конфигурации, перезапустите Cursor для применения настроек.

## 🛠️ Использование инструментов

После успешного подключения, вы можете использовать следующие команды в чате Cursor:

### Базовые команды
```
@crypto-news-analyzer Получи последние новости по Bitcoin

@crypto-news-analyzer Проанализируй Ethereum за последние 24 часа

@crypto-news-analyzer Сравни настроения для BTC, ETH, ADA
```

### Расширенные запросы
```
@crypto-news-analyzer Найди позитивные новости о Solana за последнюю неделю

@crypto-news-analyzer Проанализируй корреляцию между новостями и ценой Bitcoin

@crypto-news-analyzer Дай прогноз трендов для Cardano на основе новостей

@crypto-news-analyzer Найди события, которые могут повлиять на курс Dogecoin
```

## 🔧 Доступные инструменты

| Инструмент | Описание | Пример использования |
|------------|----------|----------------------|
| `getLatestCryptoNews` | Последние новости | `Покажи 10 последних новостей по BTC` |
| `analyzeCryptocurrency` | Комплексный анализ | `Проанализируй ETH за 7 дней` |
| `getMarketSentiment` | Анализ настроений | `Какие настроения по Bitcoin?` |
| `compareCryptocurrencies` | Сравнение | `Сравни BTC и ETH` |
| `getPositiveNews` | Позитивные новости | `Позитивные новости по ADA` |
| `getNegativeNews` | Негативные новости | `Негативные новости по DOGE` |
| `getTrendForecast` | Прогноз трендов | `Прогноз для SOL` |
| `searchCryptoNews` | Поиск новостей | `Найди новости про DeFi и ETH` |
| `getMarketMovingEvents` | Рыночные события | `События влияющие на BTC` |
| `analyzeSentimentPriceCorrelation` | Корреляция | `Корреляция новостей и цены BTC` |

## 🔍 Проверка подключения

### В Cursor
1. Откройте чат с AI
2. Введите: `@crypto-news-analyzer`
3. Если сервер подключен, вы увидите его в списке доступных инструментов

### Через терминал
```bash
# Проверка логов
docker logs crypto-mcp-server

# Проверка портов
netstat -an | findstr :8080
netstat -an | findstr :8089
```

## 🚨 Устранение проблем

### Сервер не подключается
1. **Проверьте статус контейнера**:
   ```bash
   docker ps | grep crypto-mcp-server
   ```

2. **Проверьте логи**:
   ```bash
   docker logs crypto-mcp-server
   ```

3. **Проверьте доступность портов**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Cursor не видит сервер
1. **Проверьте конфигурацию MCP** в настройках
2. **Перезапустите Cursor** полностью
3. **Убедитесь в правильности URL**: `http://localhost:8080`

### Ошибки в инструментах
- Используется demo API ключ
- Инструменты вернут заглушки вместо реальных данных
- Для полноценной работы нужен настоящий Perplexity API ключ

## 📝 Настройка с реальным API ключом

Для получения реальных данных:

### Метод 1: Использование .env файла (рекомендуется для локальной разработки)

1. **Получите API ключ Perplexity**: https://perplexity.ai/
2. **Создайте .env файл** в корне проекта:
   ```bash
   # .env
   PERPLEXITY_API_KEY=your_real_api_key_here
   PERPLEXITY_API_URL=https://api.perplexity.ai
   PERPLEXITY_MODEL=llama-3-sonar-large-32k-online
   ```
3. **Перезапустите через docker-compose**:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

### Метод 2: Переменные окружения в Docker

1. **Получите API ключ Perplexity**: https://perplexity.ai/
2. **Остановите контейнер**:
   ```bash
   docker stop crypto-mcp-server
   docker rm crypto-mcp-server
   ```
3. **Запустите с ключом**:
   ```bash
   docker run -d --name crypto-mcp-server \
     -p 8080:8080 -p 8089:8089 \
     -e PERPLEXITY_API_KEY=your_real_api_key \
     crypto-mcp-server
   ```

### ⚠️ Безопасность
- **.env файл добавлен в .gitignore** - не попадёт в Git
- **Никогда не коммитьте API ключи** в открытые репозитории
- **Используйте переменные окружения** для продакшена

## 💡 Полезные команды

### Мониторинг
```bash
# Просмотр логов в реальном времени
docker logs -f crypto-mcp-server

# Статистика использования
docker stats crypto-mcp-server

# Проверка health
curl http://localhost:8080/actuator/health
```

### Управление контейнером
```bash
# Остановка
docker stop crypto-mcp-server

# Запуск
docker start crypto-mcp-server

# Перезапуск
docker restart crypto-mcp-server

# Удаление
docker stop crypto-mcp-server && docker rm crypto-mcp-server
```

---

**🎯 После успешного подключения вы сможете анализировать криптовалютные новости прямо в Cursor!** 