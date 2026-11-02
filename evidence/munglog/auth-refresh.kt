// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: apps/collector-android/data-viewer/src/main/kotlin/com/kullstudio/munglog/viewer/data/ViewerProtobufApi.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: One token refresh reuses encoded request bytes; stale session and total deadline are rechecked.

// Original lines 452-497
    private suspend fun post(path: String, request: MessageLite, home: String = "", cacheMillis: Long = 0,
        timeoutMillis: Long? = null): ByteArray {
        checkCurrent()
        require(timeoutMillis == null || timeoutMillis in 1..10_000)
        val started = nowMillis()
        fun remainingBudget(): Int? = timeoutMillis?.let { budget ->
            val elapsed = nowMillis() - started
            if (elapsed < 0L || elapsed >= budget) throw SocketTimeoutException("Collector report read timed out")
            (budget - elapsed).toInt()
        }
        val body = request.toByteArray()
        val lease = cache.lease()
        var refreshed = false
        while (true) {
            remainingBudget()
            val token = auth.idToken(account, refreshed)
            checkCurrent()
            remainingBudget()
            try {
                return withContext(Dispatchers.IO) {
                    val call = {
                        checkCurrent()
                        val remaining = remainingBudget()
                        val response = wire?.invoke(path, body, token) ?: if (remaining == null) transport.post(path, body, token)
                            else transport.post(path, body, token, remaining)
                        checkCurrent()
                        remainingBudget()
                        response
                    }
                    val response = if (path in READS) cache.getOrLoad(ReadCacheKey.create(environment, account, home, path, body), lease, cacheMillis, call) else call()
                    checkCurrent()
                    response
                }
            } catch (error: ApiHttpException) {
                if (error.status != 401 || refreshed) throw error
                checkCurrent()
                // Exactly one Firebase token refresh. Mutations keep the identical request UUID.
                refreshed = true
            } catch (failure: IOException) {
                // A deadline disconnect may surface as a closed socket rather than a
                // SocketTimeoutException. Normalize only this explicitly budgeted read.
                remainingBudget()
                throw failure
            }
        }
    }

