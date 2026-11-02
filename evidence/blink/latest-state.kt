// Exact source excerpts from blink; original repository is private.
// Original commit: d2e90698ccf7f45154979f3cd4bc9ce5fb7ec0fa
// Original path: eye-android/src/main/java/com/kullstudio/eye/android/LatestValueDispatcher.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Pending state is replaced by the latest snapshot; commands have a separate queue.

// Original lines 7-44
internal class LatestValueDispatcher<T : Any>(
    private val post: (Runnable) -> Unit,
    private val deliver: (T) -> Unit,
) {
    private val lock = Any()
    private var pending: T? = null
    private var deliveryScheduled = false

    private val drain = Runnable {
        val value = synchronized(lock) {
            val latest = pending
            pending = null
            deliveryScheduled = false
            latest
        } ?: return@Runnable
        deliver(value)
    }

    /** Returns true when an older pending state was replaced. */
    fun submit(value: T): Boolean {
        val replaced: Boolean
        val shouldPost: Boolean
        synchronized(lock) {
            replaced = pending != null
            pending = value
            shouldPost = !deliveryScheduled
            if (shouldPost) deliveryScheduled = true
        }
        if (shouldPost) post(drain)
        return replaced
    }

    fun clear() {
        synchronized(lock) {
            pending = null
        }
    }
}

