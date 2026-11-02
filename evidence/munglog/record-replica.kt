// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: apps/collector-android/core-viewer/src/main/kotlin/com/kullstudio/munglog/viewer/ViewerRecordReplica.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Revision-aware merging preserves successful writes and the first expiry bound.

// Original lines 11-56
internal object ViewerRecordReplica {
    const val LIMIT = 5_000
    private val order = compareByDescending<ViewerRecord> { it.occurredAtSeconds }.thenByDescending { it.key }

    fun validate(home: String, page: ViewerRecordsPage, after: Long? = null) {
        require(page.records.size <= 100 && page.nextPage.length <= 1024 && page.snapshotRevision >= 0)
        require(page.records.all { it.key.isNotBlank() && it.householdKey == home && it.occurredAtSeconds > 0 &&
            it.syncRevision in 1..page.snapshotRevision && it.expiresAtElapsedMillis != null })
        require(page.records.map { it.key }.distinct().size == page.records.size)
        if (after == null) {
            require(page.nextRevision == 0L && !page.hasMoreChanges)
            require(page.nextPage.isEmpty() || page.records.isNotEmpty())
        } else {
            require(after >= 0 && page.nextPage.isEmpty() && page.nextRevision in after..page.snapshotRevision)
            var previous: Long = after
            for (record in page.records) { require(record.syncRevision > previous); previous = record.syncRevision }
            if (page.hasMoreChanges) require(page.records.isNotEmpty() && page.nextRevision == previous)
            else require(page.nextRevision == page.snapshotRevision)
        }
    }

    data class Merge(val records: List<ViewerRecord>, val trimmed: Boolean)
    fun merge(old: List<ViewerRecord>, incoming: List<ViewerRecord>, nowMillis: Long): Merge {
        val byKey = old.associateBy { it.key }.toMutableMap()
        for (record in incoming) {
            val previous = byKey[record.key]
            if (previous != null) {
                require(previous.householdKey == record.householdKey && previous.occurredAtSeconds == record.occurredAtSeconds)
                if (record.syncRevision < previous.syncRevision) continue
                if (record.syncRevision == previous.syncRevision) {
                    // Pagination overlap cannot undo a successful local mutation
                    // or extend the deadline of a cached representation.
                    byKey[record.key] = previous.copy(expiresAtElapsedMillis = minOf(
                        requireNotNull(previous.expiresAtElapsedMillis), requireNotNull(record.expiresAtElapsedMillis)))
                    continue
                }
            }
            // A read/feedback revision changes state, not the immutable event's
            // lifetime. A faster later round trip cannot extend its first bound.
            byKey[record.key] = if (previous == null) record else record.copy(expiresAtElapsedMillis = minOf(
                requireNotNull(previous.expiresAtElapsedMillis), requireNotNull(record.expiresAtElapsedMillis)))
        }
        val alive = byKey.values.filter { (it.expiresAtElapsedMillis ?: Long.MIN_VALUE) > nowMillis }.sortedWith(order)
        return Merge(alive.take(LIMIT), alive.size > LIMIT)
    }
}

