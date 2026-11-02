
    fun calculate(
        command: WearControlCommand,
        phoneSentAtEpochMillis: Long,
        sourcedStatus: SourcedWearStatus,
    ): WearClockAlignmentSample {
        val status = sourcedStatus.status
        require(status.operationId == command.operationId && status.walkId == command.walkId) {
            "wear_control_identity_mismatch"
        }
        require(phoneSentAtEpochMillis == command.requestedAtEpochMillis) {
            "wear_control_send_time_mismatch:$phoneSentAtEpochMillis:${command.requestedAtEpochMillis}"
        }
        val echoedRequest = requireNotNull(status.controlRequestedAtEpochMillis) {
            "wear_control_time_echo_missing"
        }
        require(echoedRequest == command.requestedAtEpochMillis) {
            "wear_control_time_echo_mismatch:$echoedRequest:${command.requestedAtEpochMillis}"
        }
        require(phoneSentAtEpochMillis >= 0L) { "phone send time must be non-negative" }
        val phoneReceivedAt = sourcedStatus.receivedAtEpochMillis
        val roundTrip = phoneReceivedAt - phoneSentAtEpochMillis
        require(roundTrip in 0L..MAX_ROUND_TRIP_MILLIS) {
            "wear_control_round_trip_invalid:$roundTrip"
        }
        val phoneMidpoint = Math.addExact(phoneSentAtEpochMillis, roundTrip / 2L)
        val offset = Math.subtractExact(phoneMidpoint, status.occurredAtEpochMillis)
        return WearClockAlignmentSample(
            sourceNodeId = sourcedStatus.sourceNodeId,
            phoneMinusWearMillis = offset,
            roundTripMillis = roundTrip,
            phoneSentAtEpochMillis = phoneSentAtEpochMillis,
            phoneReceivedAtEpochMillis = phoneReceivedAt,
            wearOccurredAtEpochMillis = status.occurredAtEpochMillis,
            controlRequestedAtEpochMillis = echoedRequest,
        )
    }

    fun align(sourceEpochMillis: Long, phoneMinusWearMillis: Long): Long {
        val aligned = Math.addExact(sourceEpochMillis, phoneMinusWearMillis)
        require(aligned >= 0L) { "wear_timeline_negative:$aligned" }
        return aligned
    }
}
