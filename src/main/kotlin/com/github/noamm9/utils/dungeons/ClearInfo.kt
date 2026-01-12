package com.github.noamm9.utils.dungeons

class ClearInfo {
    val clearedRooms: Pair<MutableSet<String>, MutableSet<String>> = mutableSetOf<String>() to mutableSetOf()
    val deaths: MutableList<String> = mutableListOf()
    var secretsBeforeRun: Long = 0

    companion object {
        fun get(name: String) = (DungeonListener.dungeonTeammates.find { it.name == name } ?: DungeonListener.thePlayer)?.clearInfo
    }
}