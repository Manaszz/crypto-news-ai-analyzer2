# Active Context: Crypto News MCP Server

## 1. Current Focus

The critical build and dependency issues have been resolved, and the project now has a stable foundation. The `pom.xml` has been rebuilt with correct dependencies, and the `CryptoNewsTools` class has been refactored to remove the problematic `FunctionCallbackWrapper`.

The immediate focus is to complete the implementation of the core features and then move on to a comprehensive testing phase.

## 2. Next Steps

1.  **Finalize Core Features**:
    - **Task 3 (HTTP Transport)**: Implement a security filter for API key validation on HTTP requests to secure the endpoints.
    - **Task 4 (SSE Transport)**: Enhance the `SseSessionManager` to track subscriptions and deliver targeted updates.
2.  **Comprehensive Testing**:
    - **Task 8 (Testing)**:
        - Add integration tests for the full request-response lifecycle.
        - Implement tests for the SSE transport to verify real-time updates.
        - Write unit tests for the `GlobalExceptionHandler` to ensure correct error responses.
3.  **Documentation and Deployment**:
    - **Task 9 (Docs & Deployment)**:
        - Review and update the `README.md` to reflect the final project structure and setup.
        - Ensure the `Dockerfile` and `docker-compose.yml` are working correctly for easy deployment.

## 3. Key Decisions & Patterns

- **Dependency Management**: After significant issues, the `pom.xml` has been stabilized by using the `spring-ai-bom` in the `<dependencyManagement>` section and including the `spring-milestones` repository.
- **Tool Definition**: The `CryptoNewsTools` have been refactored to use simple `Function<String, String>` beans instead of `FunctionCallbackWrapper`. This simplifies the code and removes a problematic dependency.
- **Test Configuration**: A separate `src/test/resources/application.yml` has been created to provide dummy API keys and other configuration for the test environment. This allows the application context to be loaded during tests without requiring real credentials.

## Current Task
✅ **ЗАВЕРШЕНО**: Рефакторинг MCP tools для использования аннотации `@Tool` из Spring AI

## Recent Changes (Current Session)

1. **✅ Полный рефакторинг CryptoNewsTools**:
   - Переписан с использованием Spring AI `@Tool` аннотации
   - Все 10 инструментов теперь используют декларативный подход
   - Улучшена документация и обработка параметров

2. **✅ Расширение PerplexityNewsClient**:
   - Добавлены методы для всех криптовалютных операций
   - Улучшена обработка ошибок с retry механизмом
   - Безопасная обработка null-ответов

3. **✅ Исправление NewsAnalyticsService**:
   - Адаптирован для работы с реальными моделями данных
   - Исправлены методы работы с NewsItem (description вместо content)
   - Интегрирован с расширенным PerplexityNewsClient

4. **✅ Доработка репозитория**:
   - Добавлен метод `findByCryptocurrencyIgnoreCase`
   - Исправлены проблемы с поиском по ключевым словам

5. **✅ Исправление тестов**:
   - Переписан NewsAnalyticsServiceTest для новой архитектуры
   - Все 7 тестов теперь проходят успешно

6. **✅ Масштабный рефакторинг с Lombok и SLF4J**:
   - **SLF4J**: Заменены все ручные логгеры на `@Slf4j` аннотации
   - **Lombok модели**: `@Data`, `@Value`, `@Builder` для упрощения POJO
   - **Результат**: Устранено 380+ строк бойлерплейт кода
   - Сохранена вся функциональность и совместимость

## Architectural Decisions

1. **Spring AI @Tool Integration**: Использование аннотации `@Tool` обеспечивает:
   - Автоматическое обнаружение инструментов Spring AI
   - Совместимость с MCP Protocol
   - Простое добавление новых инструментов
   - Лучшую типизацию параметров

2. **Error Handling Pattern**: Все tools возвращают читаемые сообщения об ошибках в JSON формате для лучшего UX

3. **Caching Strategy**: Используется Spring Cache с Caffeine для оптимизации производительности

## Implementation Notes

- Проект использует Spring AI 1.0.0-M6 с MCP Server starter
- Все инструменты поддерживают опциональные параметры
- Perplexity API интегрирован с правильным retry механизмом
- База данных H2 используется для кэширования новостей 