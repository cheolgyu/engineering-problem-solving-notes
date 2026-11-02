// Exact source excerpts from blink; original repository is private.
// Original commit: d2e90698ccf7f45154979f3cd4bc9ce5fb7ec0fa
// Original path: app/src/main/java/com/blinkroll/app/InferenceWorkBudget.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Two outstanding inference slots with synchronized accounting and idempotent release.

// Original lines 13-59
internal class InferenceWorkBudget(private val capacity: Int = 2) {
    private var outstanding = 0
    private var peakOutstanding = 0
    private var deferredIdle = 0L
    private var deferredTemporal = 0L

    init {
        require(capacity > 0)
    }

    @Synchronized
    fun tryAcquire(temporalPriority: Boolean): InferenceWorkPermit? {
        if (outstanding >= capacity) {
            if (temporalPriority) deferredTemporal++ else deferredIdle++
            return null
        }
        outstanding++
        peakOutstanding = maxOf(peakOutstanding, outstanding)
        return InferenceWorkPermit {
            synchronized(this) {
                check(outstanding > 0)
                outstanding--
            }
        }
    }

    @Synchronized
    fun snapshot() = InferenceWorkSnapshot(
        outstanding, peakOutstanding, deferredIdle, deferredTemporal,
    )

    @Synchronized
    fun resetMetrics() {
        // A previous session may still own the serial model. Never manufacture free slots.
        peakOutstanding = outstanding
        deferredIdle = 0L
        deferredTemporal = 0L
    }
}

internal class InferenceWorkPermit(private val release: () -> Unit) : AutoCloseable {
    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true)) release()
    }
}

