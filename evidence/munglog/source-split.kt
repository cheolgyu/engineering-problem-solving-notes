// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: apps/collector-android/app/src/main/java/com/kullstudio/munglog/collector/PersonalizedAudioHead.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Split whole recording groups and validate augmentation source membership.

// Original lines 118-155
/** Explicit source selection may change WHICH groups are held, never support, holdout counts,
 * whole-group isolation or the existing training budget. The caller owns its provenance. */
private fun validPersonalizedAudioSplit(examples: List<PersonalizedAudioExample>,
    split: PersonalizedAudioDatasetSplit, fraction: Double): Boolean {
    if ((split.trainingIndices + split.holdoutIndices).sorted() != examples.indices.toList()) return false
    val training = split.trainingIndices.toSet()
    val groups = examples.indices.groupBy { examples[it].recordingGroup }
    if (groups.values.any { rows -> rows.map { it in training }.distinct().size != 1 }) return false
    return listOf(false, true).all { label ->
        val rows = groups.values.filter { examples[it.first()].isBark == label }
        val expected = (rows.size * fraction).roundToInt().coerceIn(1, rows.size - 2)
        rows.count { it.first() !in training } == expected && rows.count { it.first() in training } >= 2
    }
}

/**
 * Class-stratified split of whole recording groups. Ordering is stable and independent of input
 * row order. Support validation happens before this helper is called by the trainer.
 */
internal fun splitPersonalizedAudioDataset(
    examples: List<PersonalizedAudioExample>,
    holdoutFraction: Double,
): PersonalizedAudioDatasetSplit {
    val groups = examples.groupBy { it.recordingGroup }
    val heldOut = mutableSetOf<String>()
    listOf(false, true).forEach { label ->
        val keys = groups.filterValues { it.first().isBark == label }.keys.sorted()
        val count = (keys.size * holdoutFraction).roundToInt().coerceIn(1, keys.size - 2)
        // Uniform positions avoid always reserving only the first recording/group prefix.
        repeat(count) { position -> heldOut += keys[((position + 0.5) * keys.size / count).toInt()] }
    }
    return PersonalizedAudioDatasetSplit(
        trainingIndices = examples.indices.filter { examples[it].recordingGroup !in heldOut }
            .sortedWith(compareBy({ examples[it].recordingGroup }, { it })),
        holdoutIndices = examples.indices.filter { examples[it].recordingGroup in heldOut }
            .sortedWith(compareBy({ examples[it].recordingGroup }, { it })),
    )
}

// Original lines 231-245
        val split = sourceSplit?.let { PersonalizedAudioDatasetSplit(it.trainingIndices.toList(), it.holdoutIndices.toList()) }
            ?: splitPersonalizedAudioDataset(stableExamples, config.holdoutFraction)
        if (!validPersonalizedAudioSplit(stableExamples, split, config.holdoutFraction))
            return rejected(PersonalizedAudioRejectedReason.INVALID_SOURCE_SPLIT)
        val fitGroups = split.trainingIndices.map { stableExamples[it].recordingGroup }.toSet()
        val originalGroups = stableExamples.groupBy { it.recordingGroup }
        if (originalGroups.any { (_, rows) -> rows.none { it.condition == "original" && it.contributingGroups == setOf(it.recordingGroup) } } ||
            stableExamples.any { row ->
                !row.condition.matches(Regex("[a-z0-9-]{1,40}")) || row.recordingGroup !in row.contributingGroups ||
                    row.contributingGroups.size !in 1..2 || row.contributingGroups.any { source ->
                        source !in groupLabels || (source in fitGroups) != (row.recordingGroup in fitGroups)
                    } || (row.condition == "original" && row.contributingGroups != setOf(row.recordingGroup))
            } || originalGroups.values.any { rows -> rows.groupBy { it.condition }.values.any { condition ->
                condition.map { it.contributingGroups }.distinct().size != 1
            } }) return rejected(PersonalizedAudioRejectedReason.INVALID_AUGMENTATION_PROVENANCE)

