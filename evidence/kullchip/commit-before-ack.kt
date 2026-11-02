            WearDataPaths.SENSOR_BATCH -> {
                val batch = WearSensorWireCodec.decode(message.data).getOrThrow()
                val alignedPackets = graph.wearTimelineAuthority
                    .alignBatch(batch, message.sourceNodeId)
                    .getOrThrow()
                val committed = graph.ingress.submitDurableBatch(
                    batchId = wearRoomBatchId(batch.wireBatchId),
                    packets = alignedPackets,
                ).getOrThrow()
                val ack = WearSensorAck(
                    wireBatchId = batch.wireBatchId,
                    walkId = batch.walkId,
                    roomBatchId = committed.batchId.value,
                    committedAtEpochMillis = graph.clock.now(),
                )
                val payload = WearControlWireCodec.encodeAck(ack).getOrThrow()
                Wearable.getMessageClient(this@MobileWearListenerService)
                    .sendMessage(message.sourceNodeId, WearDataPaths.SENSOR_ACK, payload)
                    .addOnFailureListener { error -> Log.w(TAG, "sensor ack failed", error) }
            }
