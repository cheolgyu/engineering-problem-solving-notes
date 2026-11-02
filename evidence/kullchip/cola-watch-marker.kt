    suspend fun dataEventToDbLapItem(dataMapItem: DataMapItem) {
        val stringData =
            dataMapItem.dataMap.getString(W_TO_M_LAP_PATH_KEY)
        val item: Lap =
            gson.fromJson(stringData, Lap::class.java)
        Timber.d(" item = $item")
        val walkId = if (item.walkId == 0) {
            val walk = runningWalkRepository.getRunningWalk()
            val walkId = walk?.id
            if (walkId == null) {
                return
            } else {
                walk.id
            }
        } else {
            item.walkId
        }
        val location = locationRepository.getLast(walkId)
        if (location == null) {
            Timber.d("walkId=$walkId , location is null")
            return
        }
        val lap = item.copy(
            walkId = walkId,
            locationId = location.id
        )
        chipLapRepository.insertLap(lap)
        Timber.d("==========================Lap Insert==========================lap=$lap")

    }
