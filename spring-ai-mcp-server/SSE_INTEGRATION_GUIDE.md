# SSE Integration Guide & API Key Security

## 🔐 Безопасное хранение API ключей

### Проблема
Раннее API ключ Perplexity хранился прямо в `application.yml`, что небезопасно для публичных репозиториев.

### Решение
Теперь используются переменные окружения и .env файл:

1. **API ключ удален из application.yml**
2. **Создан .env файл** с реальным ключом
3. **.env добавлен в .gitignore** 
4. **application.yml использует переменные окружения** с fallback значениями

### Структура файлов
```
spring-ai-mcp-server/
├── .env                 # Локальный API ключ (НЕ в Git)
├── .gitignore          # Игнорирует .env
├── application.yml     # Использует ${PERPLEXITY_API_KEY:demo-key}
└── docker-compose.yml  # Читает .env автоматически
```

## 🚀 Server-Sent Events (SSE) Integration

### Что такое SSE?
Server-Sent Events обеспечивают реальное время подключение между клиентом и сервером через HTTP/2. Идеально подходит для MCP протокола.

### Преимущества SSE
- ✅ **Реальное время**: Мгновенные уведомления о новых данных
- ✅ **Автопереподключение**: Встроенная обработка разрывов соединения  
- ✅ **HTTP/2 совместимость**: Работает через стандартные порты
- ✅ **Низкие накладные расходы**: Меньше ресурсов чем WebSocket

### Настройка в Spring Boot
Наш сервер автоматически поддерживает SSE на порту **8089**:
```yaml
spring:
  ai:
    mcp:
      sse:
        enabled: true
```

### Endpoints
- **HTTP API**: `http://localhost:8080` 
- **SSE Stream**: `http://localhost:8089/mcp/sse`

## 📝 Конфигурация Cursor

### Файл: cursor-mcp-sse-config.json
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
      "description": "Crypto News MCP Server с подключением через Server-Sent Events"
    }
  }
}
```

### Настройка в Cursor IDE
1. **Откройте настройки**: `Cmd/Ctrl + ,`
2. **Перейдите**: Features → Model Context Protocol  
3. **Скопируйте конфигурацию выше**
4. **Перезапустите Cursor**

## 🔧 Локальная разработка

### 1. Настройка .env файла
```bash
# Создайте .env в корне spring-ai-mcp-server/
cd spring-ai-mcp-server
cat > .env << EOF
PERPLEXITY_API_KEY=your_real_api_key_here
PERPLEXITY_API_URL=https://api.perplexity.ai  
PERPLEXITY_MODEL=llama-3-sonar-large-32k-online
EOF
```

### 2. Запуск через Docker Compose
```bash
# .env автоматически загружается docker-compose
docker-compose up -d

# Проверка логов
docker-compose logs -f crypto-mcp-server
```

### 3. Проверка подключений
```bash
# HTTP API
curl http://localhost:8080/actuator/health

# SSE Stream  
curl -N -H "Accept: text/event-stream" http://localhost:8089/mcp/sse

# Проверка портов
netstat -an | findstr :8080
netstat -an | findstr :8089
```

## 🧪 Тестирование SSE

### Через curl
```bash
# Подключение к SSE stream
curl -N \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  http://localhost:8089/mcp/sse
```

### Через JavaScript (браузер)
```javascript
const eventSource = new EventSource('http://localhost:8089/mcp/sse');

eventSource.onmessage = function(event) {
  console.log('MCP Event:', event.data);
};

eventSource.onerror = function(event) {
  console.error('SSE Error:', event);
};
```

## 🔍 Troubleshooting

### Проблема: "Connection refused на 8089"
```bash
# Проверьте что контейнер запущен с правильными портами
docker ps | grep crypto-mcp-server
# Должно показать: 0.0.0.0:8089->8089/tcp

# Перезапустите если нужно
docker-compose down
docker-compose up -d
```

### Проблема: "401 Unauthorized от Perplexity"
```bash
# Проверьте что .env файл загружен
docker exec crypto-mcp-server env | grep PERPLEXITY
# Должно показать ваш реальный API ключ

# Если показывает demo-key, проверьте .env
cat .env
```

### Проблема: "SSE не подключается в Cursor"
1. **Проверьте URL**: должен быть `http://localhost:8089/mcp/sse`
2. **Проверьте timeout**: установите 30000ms или больше
3. **Перезапустите Cursor** полностью
4. **Проверьте логи** контейнера: `docker logs crypto-mcp-server`

## 📊 Мониторинг

### Логи в реальном времени
```bash
# Все логи
docker-compose logs -f

# Только ошибки  
docker-compose logs -f | grep ERROR

# Логи SSE подключений
docker-compose logs -f | grep SSE
```

### Метрики
```bash
# Health check
curl http://localhost:8080/actuator/health

# Статистика JVM
curl http://localhost:8080/actuator/metrics

# Информация о приложении  
curl http://localhost:8080/actuator/info
```

## 🚢 Production Deployment

### Переменные окружения в продакшене
```bash
# Устанавливайте через системные переменные, не .env
export PERPLEXITY_API_KEY=prod_api_key
export PERPLEXITY_API_URL=https://api.perplexity.ai
export PERPLEXITY_MODEL=llama-3-sonar-large-32k-online

# Затем запуск
docker-compose up -d
```

### Docker Swarm / Kubernetes
```yaml
# В secrets вместо environment
apiVersion: v1
kind: Secret
metadata:
  name: perplexity-secret
type: Opaque
stringData:
  api-key: "your_production_api_key"
```

---

**🎯 После настройки вы получаете полнофункциональный MCP сервер с безопасным хранением ключей и поддержкой SSE для реального времени!** 