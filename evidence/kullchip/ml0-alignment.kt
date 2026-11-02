    fun normalize(
        window: SensorFeatureWindow,
        gravityReference: HarnessGravityReference,
        anchor: PetNavigationAnchor?,
    ): Result<Ml0BodyFrame> = runCatching {
        val samples = decodedSamples(window)
        val quaternions = samples.map { sample -> sample.normalizedQuaternionOrNull() }
        val quaternionCoverage = quaternions.count { it != null }.toFloat() / samples.size
        val heading = anchor?.globalHeadingDegrees?.takeIf(Float::isFinite)
        val useGnssOrientation = heading != null && quaternions.all { it != null }
        val mode = if (useGnssOrientation) {
            Ml0AlignmentMode.GNSS_ORIENTATION_BODY_FRAME
        } else {
            Ml0AlignmentMode.GRAVITY_MOUNT_FRAME
        }
        val values = if (useGnssOrientation) {
            worldAlignedValues(samples, quaternions.filterNotNull(), requireNotNull(heading))
        } else {
            gravityAlignedValues(samples, gravityReference)
        }
        val alignmentQuality = when (mode) {
            Ml0AlignmentMode.GNSS_ORIENTATION_BODY_FRAME -> headingQuality(anchor) * quaternionCoverage
            Ml0AlignmentMode.GRAVITY_MOUNT_FRAME -> GRAVITY_ONLY_ALIGNMENT_QUALITY
        }.coerceIn(0f, 1f)
        Ml0BodyFrame(
            windowId = window.windowId,
            algorithmVersion = Ml0BodyFrame.ALGORITHM_VERSION,
            featureSchemaVersion = Ml0BodyFrame.FEATURE_SCHEMA_VERSION,
            featureHash = featureHash(window, gravityReference, anchor, mode, values),
            rawContentHash = window.rawContentHash,
            alignmentMode = mode,
            quaternionCoverage = quaternionCoverage,
            alignmentQuality = alignmentQuality,
            values = values,
        )
    }
