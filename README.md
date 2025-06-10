# Crypto News MCP Server

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)
![MCP](https://img.shields.io/badge/MCP-Compatible-purple)

Интеллектуальный MCP (Model Context Protocol) сервер для анализа криптовалютных новостей с использованием ИИ. Предоставляет 10 специализированных инструментов для анализа настроений, трендов и событий в мире криптовалют через Perplexity API.

## 🚀 Функциональность

### Основные возможности
- **Анализ настроений** криптовалютных новостей в реальном времени
- **Прогнозирование трендов** на основе новостного фона
- **Сравнительный анализ** различных криптовалют
- **Мониторинг событий**, влияющих на рынок
- **Корреляционный анализ** настроений и цен
- **Кэширование** результатов для оптимизации производительности
- **Real-time обновления** через Server-Sent Events (SSE)

### 10 MCP Инструментов

| Инструмент | Описание | Параметры |
|------------|----------|-----------|
| `getLatestCryptoNews` | Получение последних новостей | `cryptocurrency`, `maxArticles?` |
| `analyzeCryptocurrency` | Комплексный анализ криптовалюты | `cryptocurrency`, `timeRange?` |
| `getMarketSentiment` | Анализ настроений рынка | `cryptocurrency`, `timeRange?` |
| `compareCryptocurrencies` | Сравнение криптовалют | `cryptocurrencies` |
| `getPositiveNews` | Позитивные новости | `cryptocurrency`, `limit?` |
| `getNegativeNews` | Негативные новости | `cryptocurrency`, `limit?` |
| `getTrendForecast` | Прогноз трендов | `cryptocurrency` |
| `searchCryptoNews` | Поиск по ключевым словам | `cryptocurrency`, `keywords` |
| `getMarketMovingEvents` | События, влияющие на рынок | `cryptocurrency` |
| `analyzeSentimentPriceCorrelation` | Корреляция настроений и цен | `cryptocurrency` |

## 🏗️ Архитектура

### Схема архитектуры
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Cursor IDE    │    │   Cline VS Code  │    │  Other Clients  │
│                 │    │                  │    │                 │
└─────────┬───────┘    └─────────┬────────┘    └─────────┬───────┘
          │                      │                       │
          └──────────────────────┼───────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    MCP Protocol Layer   │
                    │  ┌─────┐ ┌─────┐ ┌─────┐│
                    │  │ SSE │ │HTTP │ │STDIO││
                    │  └─────┘ └─────┘ └─────┘│
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   CryptoNewsTools       │
                    │   (@Tool Annotations)   │
                    │   ┌─────────────────────┐│
                    │   │ 10 MCP Instruments  ││
                    │   └─────────────────────┘│
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  Application Services   │
                    │ ┌─────────────────────┐ │
                    │ │ NewsAnalyticsService│ │
                    │ │ SentimentAnalyzer   │ │
                    │ │ NewsUpdateService   │ │
                    │ └─────────────────────┘ │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   PerplexityNewsClient  │
                    │   (AI Integration)      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    Perplexity API       │
                    │   (External Service)    │
                    └─────────────────────────┘
```

### Технологический стек

- **Framework**: Spring Boot 3.3.0 + Spring AI 1.0.0-M6
- **Language**: Java 21 с Lombok для упрощения кода
- **Protocol**: MCP (Model Context Protocol)
- **AI Integration**: Perplexity API для анализа новостей
- **Database**: H2 (embedded) для кэширования
- **Cache**: Caffeine для высокопроизводительного кэширования
- **Logging**: SLF4J с Logback
- **Build**: Maven
- **Containerization**: Docker

### Ключевые компоненты

#### 1. CryptoNewsTools (@Tool)
Основной класс с 10 MCP инструментами, использующий Spring AI аннотации:
```java
@Component
@Slf4j
public class CryptoNewsTools {
    
    @Tool("Get latest cryptocurrency news")
    public String getLatestCryptoNews(String cryptocurrency, Integer maxArticles) {
        // Реализация получения новостей
    }
    
    @Tool("Analyze cryptocurrency sentiment and trends")
    public String analyzeCryptocurrency(String cryptocurrency, String timeRange) {
        // Комплексный анализ
    }
}
```

#### 2. NewsAnalyticsService
Основная бизнес-логика с кэшированием и транзакциями:
```java
@Service
@Transactional
@Slf4j
public class NewsAnalyticsService {
    
    @Cacheable(value = "news-cache")
    public List<NewsItem> getLatestCryptoNews(String cryptocurrency, Integer maxArticles) {
        // Логика получения и анализа новостей
    }
}
```

#### 3. Data Models (Lombok)
Современные модели данных с минимумом бойлерплейт кода:
```java
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsItem {
    private String title;
    private String description;
    private String cryptocurrency;
    private LocalDateTime publishedDate;
    private SentimentScore sentimentScore;
}
```

## 📁 Структура проекта

```
crypto-new-ai-analyzer/
├── spring-ai-mcp-server/
│   ├── src/main/java/com/cryptonews/mcpserver/
│   │   ├── SpringAiMcpApplication.java          # Главный класс приложения
│   │   ├── tools/
│   │   │   └── CryptoNewsTools.java             # 10 MCP инструментов (@Tool)
│   │   ├── service/
│   │   │   ├── NewsAnalyticsService.java        # Основная бизнес-логика
│   │   │   ├── SentimentAnalyzer.java           # Анализ настроений
│   │   │   ├── NewsUpdateService.java           # Обновления в реальном времени
│   │   │   └── SseService.java                  # Server-Sent Events
│   │   ├── client/
│   │   │   └── PerplexityNewsClient.java        # Интеграция с Perplexity API
│   │   ├── model/
│   │   │   ├── NewsItem.java                    # Модель новости (@Data)
│   │   │   ├── SentimentScore.java              # Модель оценки настроений
│   │   │   └── CryptoAnalytics.java             # Модель аналитики (@Value)
│   │   └── config/
│   │       ├── CachingConfig.java               # Конфигурация кэширования
│   │       └── PerplexityApiProperties.java     # Настройки API (@Data)
│   ├── src/main/resources/
│   │   └── application.yml                      # Конфигурация приложения
│   ├── Dockerfile                               # Docker образ
│   └── pom.xml                                  # Maven зависимости
├── memory-bank/                                 # Документация проекта
└── README.md                                    # Эта документация
```

## 🛠️ Примеры использования инструментов

### 1. Получение последних новостей
**Входные параметры:**
```json
{
  "tool": "getLatestCryptoNews",
  "parameters": {
    "cryptocurrency": "BTC",
    "maxArticles": 5
  }
}
```

**Пример ответа:**
```json
[
  {
    "id": 1,
    "title": "Bitcoin Reaches New All-Time High",
    "description": "Bitcoin surged to $75,000 following institutional adoption...",
    "cryptocurrency": "BTC",
    "publishedDate": "2024-06-10T10:30:00",
    "source": "CryptoNews",
    "url": "https://example.com/news/1",
    "sentimentScore": {
      "positiveScore": 0.8,
      "negativeScore": 0.1,
      "neutralScore": 0.1,
      "compoundScore": 0.7,
      "label": "POSITIVE"
    }
  }
]
```

### 2. Комплексный анализ криптовалюты
**Входные параметры:**
```json
{
  "tool": "analyzeCryptocurrency",
  "parameters": {
    "cryptocurrency": "ETH",
    "timeRange": "7 days"
  }
}
```

**Пример ответа:**
```json
{
  "cryptocurrency": "ETH",
  "timeRange": "7 days",
  "totalArticles": 45,
  "sentimentCounts": {
    "positive": 28,
    "negative": 12,
    "neutral": 5
  },
  "averageSentiment": 0.35,
  "sentimentTrend": 0.15,
  "keyTopics": ["DeFi", "Ethereum 2.0", "Smart Contracts"],
  "topPositiveNews": [...],
  "topNegativeNews": [...],
  "marketMovingEvents": "Ethereum London hard fork implementation...",
  "trendForecast": "Bullish outlook based on upcoming upgrades..."
}
```

### 3. Анализ настроений рынка
```json
{
  "tool": "getMarketSentiment",
  "parameters": {
    "cryptocurrency": "BTC",
    "timeRange": "24 hours"
  }
}
```

### 4. Сравнение криптовалют
```json
{
  "tool": "compareCryptocurrencies",
  "parameters": {
    "cryptocurrencies": "BTC,ETH,ADA"
  }
}
```

### 5. Поиск новостей по ключевым словам
```json
{
  "tool": "searchCryptoNews",
  "parameters": {
    "cryptocurrency": "BTC",
    "keywords": "institutional adoption"
  }
}
```

## 🐳 Развертывание с Docker

### Предварительные требования
- Docker и Docker Compose
- API ключ Perplexity (получить на [perplexity.ai](https://perplexity.ai))

### Шаг 1: Клонирование репозитория
```bash
git clone <repository-url>
cd crypto-new-ai-analyzer
```

### Шаг 2: Сборка Docker образа
```bash
cd spring-ai-mcp-server
docker build -t crypto-mcp-server .
```

### Шаг 3: Настройка API ключа

#### Метод 1: Использование .env файла (рекомендуется)
```bash
# Создайте .env файл в директории проекта
echo "PERPLEXITY_API_KEY=your_api_key_here" > .env
echo "PERPLEXITY_API_URL=https://api.perplexity.ai" >> .env
echo "PERPLEXITY_MODEL=llama-3-sonar-large-32k-online" >> .env
```

#### Метод 2: Переменные окружения
```bash
export PERPLEXITY_API_KEY=your_api_key_here
export PERPLEXITY_API_URL=https://api.perplexity.ai
export PERPLEXITY_MODEL=llama-3-sonar-large-32k-online
```

### Шаг 4: Запуск контейнера

#### С Docker Compose (рекомендуется)
```bash
docker-compose up -d
```

#### Напрямую с Docker
```bash
docker run -d \
  --name crypto-mcp-server \
  -p 8080:8080 \
  -p 8089:8089 \
  -e PERPLEXITY_API_KEY=your_api_key_here \
  -e PERPLEXITY_API_URL=https://api.perplexity.ai \
  -e PERPLEXITY_MODEL=llama-3-sonar-large-32k-online \
  crypto-mcp-server
```

### ⚠️ Безопасность API ключей
- **Используйте .env файл** для локальной разработки
- **.env файл добавлен в .gitignore** - не попадёт в Git
- **Никогда не коммитьте API ключи** в открытые репозитории
- **Используйте переменные окружения** в продакшене

## 🔗 Подключение к IDE

### Cursor IDE

1. **Откройте настройки Cursor** (`Cmd/Ctrl + ,`)
2. **Перейдите в раздел "Features" → "Model Context Protocol"**
3. **Выберите один из методов подключения:**

#### Метод 1: SSE транспорт (рекомендуется для реального времени)
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
      "description": "Crypto News MCP Server через Server-Sent Events"
    }
  }
}
```

#### Метод 2: HTTP транспорт
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

#### Метод 3: Docker exec (альтернативный)
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
      "description": "Crypto News Analysis via Docker exec"
    }
  }
}
```

### Cline (VS Code Extension)

1. **Установите расширение Cline в VS Code**
2. **Откройте настройки расширения**
3. **Добавьте MCP сервер в конфигурацию:**

```json
{
  "cline.mcpServers": [
    {
      "name": "crypto-news-analyzer",
      "url": "http://localhost:8080"
    }
  ]
}
```

## 🧪 Тестирование подключения

### Проверка работы сервера
```bash
# Проверка health endpoint
curl http://localhost:8080/actuator/health

# Проверка информации о приложении
curl http://localhost:8080/actuator/info

# Тест SSE подключения
curl -N -H "Accept: text/event-stream" http://localhost:8089/mcp/sse

# Проверка логов контейнера
docker logs crypto-mcp-server
```

### Пример вызова через Cursor

После подключения MCP сервера в Cursor, вы можете использовать инструменты в чате:

**Анализ последних новостей:**
```
@crypto-news-analyzer Проанализируй последние новости по Bitcoin за неделю и дай прогноз тренда
```

**Сравнение криптовалют:**
```
@crypto-news-analyzer Сравни настроения рынка для BTC, ETH и ADA за последние 24 часа
```

**Поиск событий:**
```
@crypto-news-analyzer Найди все события, которые могут повлиять на курс Ethereum в ближайшее время
```

**Анализ корреляции:**
```
@crypto-news-analyzer Проанализируй корреляцию между настроениями в новостях и движением цены Bitcoin
```

## ⚙️ Конфигурация

### application.yml
```yaml
server:
  port: 8080

spring:
  ai:
    mcp:
      server:
        transport:
          stdio:
            enabled: true
          http:
            enabled: true
          sse:
            enabled: true
  
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h

perplexity:
  api:
    url: ${PERPLEXITY_API_URL:https://api.perplexity.ai}
    key: ${PERPLEXITY_API_KEY}
    model: sonar-pro
    backup-model: sonar

news:
  analytics:
    max-articles-per-request: 20
    default-time-range-hours: 24

logging:
  level:
    com.cryptonews: DEBUG
    org.springframework.ai: DEBUG
    root: INFO
```

### Переменные окружения
```bash
# Обязательные
PERPLEXITY_API_KEY=your_perplexity_api_key

# Опциональные
PERPLEXITY_API_URL=https://api.perplexity.ai
SERVER_PORT=8080
LOGGING_LEVEL_ROOT=INFO
```

## 🔧 Разработка

### Локальный запуск
```bash
cd spring-ai-mcp-server

# Установка зависимостей и сборка
mvn clean package

# Запуск в режиме разработки
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Запуск тестов
```bash
# Все тесты
mvn test

# Только unit тесты
mvn test -Dtest=*Test

# Интеграционные тесты
mvn test -Dtest=*IntegrationTest
```

### Hot reload для разработки
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

## 📊 Мониторинг и отладка

### Actuator endpoints
- **Health**: `GET /actuator/health` - Состояние приложения
- **Metrics**: `GET /actuator/metrics` - Метрики производительности
- **Info**: `GET /actuator/info` - Информация о приложении
- **Loggers**: `GET /actuator/loggers` - Управление уровнями логирования

### Полезные команды для отладки
```bash
# Просмотр логов в реальном времени
docker logs -f crypto-mcp-server

# Проверка использования ресурсов
docker stats crypto-mcp-server

# Подключение к контейнеру
docker exec -it crypto-mcp-server /bin/bash

# Проверка конфигурации Spring
docker exec crypto-mcp-server curl http://localhost:8080/actuator/configprops
```

## 🤝 Примеры интеграции

### Интеграция с Trading Bot
```python
import requests

# Получение анализа настроений для принятия торговых решений
def get_trading_signal(crypto):
    response = requests.post('http://localhost:8080/tools/analyzeCryptocurrency', 
                           json={
                               'cryptocurrency': crypto,
                               'timeRange': '24 hours'
                           })
    
    analysis = response.json()
    if analysis['averageSentiment'] > 0.5:
        return 'BUY'
    elif analysis['averageSentiment'] < -0.3:
        return 'SELL'
    else:
        return 'HOLD'
```

### Telegram Bot Integration
```javascript
const TelegramBot = require('node-telegram-bot-api');
const axios = require('axios');

const bot = new TelegramBot(token, {polling: true});

bot.onText(/\/analyze (.+)/, async (msg, match) => {
    const crypto = match[1];
    
    try {
        const response = await axios.post('http://localhost:8080/tools/getLatestCryptoNews', {
            cryptocurrency: crypto,
            maxArticles: 5
        });
        
        bot.sendMessage(msg.chat.id, `Анализ новостей для ${crypto}:\n${JSON.stringify(response.data, null, 2)}`);
    } catch (error) {
        bot.sendMessage(msg.chat.id, 'Ошибка получения данных');
    }
});
```

## 🚨 Устранение проблем

### Частые проблемы и решения

1. **Сервер не запускается**
   ```bash
   # Проверьте логи
   docker logs crypto-mcp-server
   
   # Убедитесь что порты свободны
   netstat -tulpn | grep :8080
   ```

2. **Ошибки API ключа Perplexity**
   ```bash
   # Проверьте переменные окружения
   docker exec crypto-mcp-server env | grep PERPLEXITY
   ```

3. **MCP сервер не подключается к Cursor**
   - Убедитесь что сервер запущен: `curl http://localhost:8080/actuator/health`
   - Проверьте конфигурацию MCP в настройках Cursor
   - Перезапустите Cursor после изменения конфигурации

4. **Медленные ответы от инструментов**
   - Проверьте кэш: `curl http://localhost:8080/actuator/metrics/cache.gets`
   - Увеличьте размер кэша в `application.yml`

## 📈 Метрики производительности

Типичные показатели производительности:
- **Время ответа API**: 200-500ms для кэшированных результатов
- **Время анализа новостей**: 1-3 секунды для новых запросов
- **Использование памяти**: 512MB-1GB в зависимости от загрузки
- **Concurrent users**: До 100 одновременных пользователей

## 📝 Changelog

### v1.0.0 (Current)
- ✅ Реализованы все 10 MCP инструментов
- ✅ Интеграция с Perplexity API
- ✅ Spring AI 1.0.0-M6 поддержка
- ✅ Docker containerization
- ✅ Lombok и SLF4J рефакторинг
- ✅ Комплексное тестирование

## 🆘 Поддержка

**При возникновении проблем:**

1. **Проверьте документацию** - большинство вопросов освещены здесь
2. **Изучите логи** - `docker logs crypto-mcp-server`
3. **Проверьте health endpoints** - `/actuator/health`
4. **Создайте issue** в репозитории с подробным описанием проблемы

**Полезные ссылки:**
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Perplexity API Documentation](https://docs.perplexity.ai/)
- [Docker Documentation](https://docs.docker.com/)

---

**🎯 Разработано с использованием Spring AI и MCP Protocol для интеграции с современными IDE и AI-ассистентами.**

**⭐ Поставьте звезду репозиторию, если проект оказался полезным!** 