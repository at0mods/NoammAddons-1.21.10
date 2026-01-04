package com.github.noamm9.utils

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.event.EventBus.register
import com.github.noamm9.event.impl.PacketEvent
import com.github.noamm9.event.impl.WorldChangeEvent
import net.minecraft.Util
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket
import kotlin.math.min


object ServerUtils {
    var averageTps = 20f
        private set

    var currentPing: Int = 0
        private set

    var averagePing: Int = 0
        private set

    private var lastTimePacket = 0L

    fun init() {
        register<WorldChangeEvent> {
            averageTps = 20f
            currentPing = 0
            averagePing = 0
            lastTimePacket = 0L
        }

        register<PacketEvent.Received> {
            if (event.packet is ClientboundSetTimePacket) {
                if (lastTimePacket != 0L) averageTps = (20000f / (System.currentTimeMillis() - lastTimePacket + 1)).coerceIn(0f, 20f)
                lastTimePacket = System.currentTimeMillis()
            }
            else if (event.packet is ClientboundPongResponsePacket) {
                currentPing = (Util.getMillis() - event.packet.time).toInt().coerceAtLeast(0)
                val pingLog = mc.debugOverlay.pingLogger
                val sampleSize = min(pingLog.size(), 10)

                if (sampleSize == 0) {
                    averagePing = currentPing
                    return@register
                }

                var total = 0L
                for (i in 0 until sampleSize) {
                    total += pingLog.get(i)
                }

                averagePing = (total / sampleSize).toInt()
            }
        }
    }
}