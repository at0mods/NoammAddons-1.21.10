package com.github.noamm9.utils.network

import com.github.noamm9.utils.network.cache.UuidCache
import com.github.noamm9.utils.network.data.MojangData

object ProfileUtils {
    private const val API = "https://api.noammaddons.workers.dev"

    suspend fun getUUIDbyName(name: String): Result<MojangData> {
        UuidCache.getFromCache(name)?.let { return Result.success(MojangData(name, it)) }
        return WebUtils.get<MojangData>("https://api.minecraftservices.com/minecraft/profile/lookup/name/$name")
            .onSuccess { UuidCache.addToCache(name, it.uuid) }
    }

    suspend fun getSecrets(playerName: String): Result<Long> {
        return getUUIDbyName(playerName).mapCatching { mojangData ->
            WebUtils.get<Long>("$API/secrets?uuid=${mojangData.uuid}").getOrThrow()
        }
    }
}