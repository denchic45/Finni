package com.hackathon.finni.core.paginator

sealed interface FetchStrategy {
    /** 1. Ждём API. При ошибке activePages НЕ меняем (для догрузки) */
    object NetworkFirstGuarded : FetchStrategy

    /** 2. Ждём API. При ошибке ВСЁ равно обновляем activePages (покажет кэш + ошибку) */
    object NetworkFirstWithFallback : FetchStrategy

    /** 3. Сразу обновляем activePages (показываем кэш), API качается асинхронно */
    object StaleWhileRevalidate : FetchStrategy

    /** 4. Не вызываем API, только переключаем activePages */
    object CacheOnly : FetchStrategy
}