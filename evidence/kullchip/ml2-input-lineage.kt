        val allAxes = ml1.axes.associateBy(Ml1Inference::axis)
        val axes = allAxes.filterKeys(AXIS_LABELS::containsKey)
        val missingAxes = AXIS_LABELS.keys - axes.keys
        val unexpectedAxes = allAxes.keys - AXIS_LABELS.keys - AUXILIARY_AXES
        if (missingAxes.isNotEmpty() || unexpectedAxes.isNotEmpty()) {
            return@runSuspendCatching blocked(
                "missing=${missingAxes.sorted().joinToString(",")};" +
                    "unexpected=${unexpectedAxes.sorted().joinToString(",")}",
            )
        }
        AXIS_LABELS.forEach { (axis, labels) ->
            val actual = axes.getValue(axis).scores.keys
            if (actual != labels.toSet()) {
                return@runSuspendCatching blocked(
                    "axis=$axis;expected=${labels.joinToString(",")};actual=${actual.sorted().joinToString(",")}",
                )
            }
        }
        val upstreamArtifactSha256 = axes.values.map { inference -> inference.model.artifactSha256 }.distinct()
        if (upstreamArtifactSha256.size != 1) {
            return@runSuspendCatching blocked(
                "mixed_upstream_ml1_artifacts=${upstreamArtifactSha256.sorted().joinToString(",")}",
                reason = ModelBlockReason.DEPENDENCY_BLOCKED,
            )
        }
        val sharedMl1ArtifactSha256 = upstreamArtifactSha256.single()
        val values = FloatArray(Ml2PredictionFeatureContract.FEATURE_WIDTH)
        var offset = 0
        AXIS_LABELS.forEach { (axis, labels) ->
            labels.forEach { label -> values[offset++] = axes.getValue(axis).scores.getValue(label) }
        }
        values[offset] = axes.values.map { 1f - it.confidence }.average().toFloat().coerceIn(0f, 1f)
        check(offset + 1 == values.size)

        val featureHash = MessageDigest.getInstance("SHA-256").apply {
            putString(Ml2PredictionFeatureContract.SCHEMA_VERSION)
            putString(window.rawContentHash)
            putString(ml0.featureHash)
            putString(sharedMl1ArtifactSha256)
            AXIS_LABELS.keys.forEach { axis -> putString(axes.getValue(axis).featureHash) }
            values.forEach { value -> putFloat(value) }
        }.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
