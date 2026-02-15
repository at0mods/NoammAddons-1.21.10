package com.github.noamm9.commands.impl

import com.github.noamm9.commands.BaseCommand
import com.github.noamm9.commands.CommandNodeBuilder
import com.github.noamm9.utils.ChatUtils
import com.github.noamm9.utils.PlayerUtils
import com.github.noamm9.utils.dungeons.map.utils.ScanUtils

object TestCommand: BaseCommand("test") {
    override fun CommandNodeBuilder.build() {
        runs {
            val room = ScanUtils.currentRoom ?: return@runs
            ChatUtils.chat(ScanUtils.getRelativeCoord(PlayerUtils.getSelectionBlock() !!, room.corner ?: return@runs, room.rotation ?: return@runs))
        }
    }
}