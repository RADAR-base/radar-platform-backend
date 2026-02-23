package org.radarbase.delegate.service

import jakarta.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class Cache<T>
    @Inject
    constructor(
        private val CACHE_EXPIRY_SECONDS: Long = 10L,
    ) {
        private val logger = LoggerFactory.getLogger(Cache::class.java)
        private val mutex = Mutex()
        private val cache = ConcurrentHashMap<String, CacheEntry<T>>()

        private data class CacheEntry<T>(
            val data: T,
            val timestamp: Instant = Instant.now(),
        ) {
            fun isExpired(expirySeconds: Long): Boolean = Instant.now().isAfter(timestamp.plusSeconds(expirySeconds))
        }

        suspend fun withCache(
            cacheKey: String,
            fetchData: suspend () -> T,
            logMessage: String? = null,
        ): T {
            return mutex.withLock {
                @Suppress("UNCHECKED_CAST")
                val cached = cache[cacheKey] as? CacheEntry<T>

                if (cached != null && !cached.isExpired(CACHE_EXPIRY_SECONDS)) {
                    logMessage?.let { logger.debug(it) }
                    return@withLock cached.data
                }
                val data = fetchData()
                cache[cacheKey] = CacheEntry(data)
                data
            }
        }

        fun clearCache(cacheKey: String) {
            cache.remove(cacheKey)
        }

        fun clearAllCache() {
            cache.clear()
        }
    }
