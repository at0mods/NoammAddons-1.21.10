package com.github.noamm9.features.impl.dungeon.solvers.terminals

import com.github.noamm9.NoammAddons
import com.github.noamm9.event.impl.TickEvent
import com.github.noamm9.features.Feature
import com.github.noamm9.features.impl.dungeon.solvers.terminals.TerminalListener.FIRST_CLICK_DELAY
import com.github.noamm9.ui.clickgui.componnents.*
import com.github.noamm9.ui.clickgui.componnents.impl.DropdownSetting
import com.github.noamm9.ui.clickgui.componnents.impl.SliderSetting
import com.github.noamm9.ui.clickgui.componnents.impl.ToggleSetting
import com.github.noamm9.utils.ChatUtils
import net.minecraft.world.inventory.ClickType
import kotlin.random.Random

object AutoTerminal: Feature("Automatically clicks terminals for you.") {
    private val randomDelay by ToggleSetting("Random Delay", true).section("Settings")

    private val autoDelay by SliderSetting("Click Delay", 150.0, 0.0, 500.0, 10.0)
        .withDescription("Fixed delay between clicks in milliseconds.")
        .showIf { ! randomDelay.value }

    private val minRandomDelay by SliderSetting("Min Random Delay", 50.0, 0.0, 500.0, 10.0)
        .withDescription("The minimum possible delay")
        .showIf { randomDelay.value }

    private val maxRandomDelay by SliderSetting("Max Random Delay", 150.0, 0.0, 500.0, 10.0)
        .withDescription("The maximum possible delay")
        .showIf { randomDelay.value }

    private val clickOrder by DropdownSetting("Click Order", 0, listOf("None", "Random", "Human", "Skizo"))
        .withDescription("Human: Logic pathing. Skizo: Chaotic/Furthest.")

    private val autoNumbers by ToggleSetting("Numbers", true).section("Terminals")
    private val autoColors by ToggleSetting("Colors", true)
    private val autoMelody by ToggleSetting("Melody", true)
    private val autoRubix by ToggleSetting("Rubix", true)
    private val autoRedGreen by ToggleSetting("Red-Green", true)
    private val autoStartWith by ToggleSetting("Start-With", true)

    private var lastClickTime = 0L
    private var lastClickedSlot: Int? = null


    override fun onEnable() {
        super.onEnable()
        register()
    }

    override fun onDisable() {
        super.onDisable()
        if (! TerminalSolver.enabled) {
            unregister()
        }
    }

    override fun init() {
        register<TickEvent.Server> {
            if (! autoMelody.value) return@register
            if (! TerminalListener.inTerm) return@register
            if (TerminalListener.checkFcDelay()) return@register
            if (TerminalListener.currentType != TerminalType.MELODY) return@register
            if (System.currentTimeMillis() - lastClickTime < 250) return@register

            val current = TerminalType.melodyCurrent ?: return@register
            val correct = TerminalType.melodyCorrect ?: return@register
            val buttonRow = TerminalType.melodyButton ?: return@register
            if (current != correct) return@register

            val actualSlot = buttonRow * 9 + 16
            if (lastClickedSlot == actualSlot) return@register

            sendClickPacket(actualSlot)
            lastClickTime = System.currentTimeMillis()
            lastClickedSlot = actualSlot
        }
    }

    fun onItemsUpdated() {
        if (! enabled) return

        if (TerminalSolver.solution.isEmpty()) {
            TerminalSolver.solve()
        }

        val solution = TerminalSolver.solution
        if (solution.isEmpty()) return

        autoClick(solution)
    }

    private fun autoClick(solution: List<TerminalClick>) {
        val type = TerminalListener.currentType ?: return
        if (! shouldAutoSolve(type)) return

        val rawClick = if (type == TerminalType.NUMBERS) solution.first()
        else when (clickOrder.value) {
            1 -> solution.random()
            2 -> HumanClickOrder.getBestClick(solution, type)
            3 -> HumanClickOrder.getWorstClick(solution, type)
            else -> solution.first()
        }

        if (lastClickedSlot == rawClick.slotId && type != TerminalType.RUBIX) return

        val finalClick = if (type == TerminalType.RUBIX)
            TerminalClick(rawClick.slotId, if (rawClick.btn > 0) 0 else 1)
        else rawClick

        val delayMs = when {
            TerminalListener.checkFcDelay() -> FIRST_CLICK_DELAY * 50
            randomDelay.value -> {
                val min = minRandomDelay.value.toInt().coerceAtLeast(0)
                val max = maxRandomDelay.value.toInt().coerceAtLeast(0)
                if (min == max) min else Random.nextInt(minOf(min, max), maxOf(min, max))
            }

            else -> autoDelay.value.toInt()
        }.coerceAtLeast(0)

        val delayTicks = delayMs / 50
        val initialWindowId = TerminalListener.lastWindowId

        if (delayMs == 0) click(finalClick)
        else Scheduler.schedule(delayMs, delayTicks) {
            if (TerminalListener.inTerm && initialWindowId == TerminalListener.lastWindowId) {
                click(finalClick)
            }
        }
    }

    fun shouldAutoSolve(type: TerminalType) = when (type) {
        TerminalType.NUMBERS -> autoNumbers.value
        TerminalType.COLORS -> autoColors.value
        TerminalType.MELODY -> autoMelody.value
        TerminalType.RUBIX -> autoRubix.value
        TerminalType.REDGREEN -> autoRedGreen.value
        TerminalType.STARTWITH -> autoStartWith.value
    }

    private fun click(click: TerminalClick) {
        lastClickedSlot = click.slotId
        TerminalSolver.click(click)
    }

    private fun sendClickPacket(slot: Int) {
        mc.gameMode?.handleInventoryMouseClick(
            TerminalListener.lastWindowId, slot, 2, ClickType.CLONE, mc.player
        )
        if (NoammAddons.debugFlags.contains("terminal")) {
            ChatUtils.modMessage("Auto-Clicked Melody $slot")
        }
    }

    fun reset() {
        lastClickTime = 0
        lastClickedSlot = null
    }

    fun register() {
        TerminalListener.packetRecivedListener.register()
        TerminalListener.packetSentListener.register()
        TerminalListener.tickListener.register()
        TerminalListener.worldChangeListener.register()
        Scheduler.tickListener.register()
        Scheduler.timeListener.register()
    }

    fun unregister() {
        TerminalListener.packetRecivedListener.unregister()
        TerminalListener.packetSentListener.unregister()
        TerminalListener.tickListener.unregister()
        TerminalListener.worldChangeListener.unregister()
        Scheduler.timeListener.unregister()
        Scheduler.tickListener.unregister()
    }
}