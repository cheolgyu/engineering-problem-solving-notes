// Exact source excerpts from blink; original repository is private.
// Original commit: d2e90698ccf7f45154979f3cd4bc9ce5fb7ec0fa
// Original path: eye-unity-bridge/src/main/java/com/kullstudio/eye/unity/UnityEyeBridge.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Event ID deduplication, bounded pending queue, poll/confirm and explicit dropped IDs.

// Original lines 481-532
internal class UnityEyeEventBuffer(
    private val capacity: Int,
) {
    private val queuedEventIds = ArrayDeque<Long>()
    private val pendingPayloads = linkedMapOf<Long, String>()

    init {
        require(capacity > 0)
    }

    @Synchronized
    fun offer(eventId: Long, payload: String): List<Long> {
        require(eventId > 0L)
        if (eventId in pendingPayloads) return emptyList()
        val dropped = mutableListOf<Long>()
        while (pendingPayloads.size >= capacity) {
            val removedId = pendingPayloads.entries.first().key
            pendingPayloads.remove(removedId)
            queuedEventIds.remove(removedId)
            dropped += removedId
        }
        pendingPayloads[eventId] = payload
        queuedEventIds.addLast(eventId)
        return dropped
    }

    @Synchronized
    fun poll(): BufferedUnityEyeEvent? {
        while (queuedEventIds.isNotEmpty()) {
            val eventId = queuedEventIds.removeFirst()
            val payload = pendingPayloads[eventId] ?: continue
            return BufferedUnityEyeEvent(eventId, payload)
        }
        return null
    }

    @Synchronized
    fun confirm(eventId: Long): Boolean = pendingPayloads.remove(eventId) != null

    @Synchronized
    fun rejectAll(): List<Long> {
        val pending = pendingPayloads.keys.toList()
        clear()
        return pending
    }

    @Synchronized
    fun clear() {
        queuedEventIds.clear()
        pendingPayloads.clear()
    }
}

