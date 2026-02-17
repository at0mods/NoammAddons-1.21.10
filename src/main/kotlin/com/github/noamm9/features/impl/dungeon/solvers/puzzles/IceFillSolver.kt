package com.github.noamm9.features.impl.dungeon.solvers.puzzles

import com.github.noamm9.NoammAddons.mc
import com.github.noamm9.NoammAddons.scope
import com.github.noamm9.event.impl.DungeonEvent
import com.github.noamm9.features.impl.dungeon.solvers.puzzles.PuzzleSolvers.icefillColor
import com.github.noamm9.utils.render.Render3D
import com.github.noamm9.utils.render.RenderContext
import com.github.noamm9.utils.world.WorldUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet


object IceFillSolver {
    private var puzzles = CopyOnWriteArraySet<IceFillPuzzle>()

    fun onRoomEnter(event: DungeonEvent.RoomEvent.onEnter) {
        if (event.room.name != "Ice Fill") return

        scope.launch {
            var i = 0
            while (i < 3) {
                i ++

                solve(event.room.centerPos)
                if (puzzles.size == 3) break
                delay(300)
            }
        }
    }

    fun onRenderWorld(ctx: RenderContext) {
        puzzles.forEach { it.draw(ctx, icefillColor.value) }
    }

    fun reset() = puzzles.clear()

    private fun solve(center: BlockPos) {
        reset()
        var previousEnd = mc.player?.blockPosition() ?: center

        val allIceBlocks = mutableSetOf<BlockPos>()

        for (dx in - 15 .. 15) for (dz in - 15 .. 15) for (dy in 69 .. 71) {
            val pos = center.offset(dx, dy - center.y, dz)
            val state = WorldUtils.getStateAt(pos)
            if (state.`is`(Blocks.ICE) || state.`is`(Blocks.PACKED_ICE)) {
                if (WorldUtils.getStateAt(pos.above()).isAir) {
                    allIceBlocks.add(pos.above())
                }
            }
        }

        val clusters = mutableListOf<MutableSet<BlockPos>>()
        val visited = mutableSetOf<BlockPos>()

        for (pos in allIceBlocks) {
            if (pos in visited) continue

            val cluster = mutableSetOf<BlockPos>()
            val queue = ArrayDeque<BlockPos>()
            queue.add(pos)
            visited.add(pos)

            while (! queue.isEmpty()) {
                val current = queue.removeFirst()
                cluster.add(current)
                for (dir in Direction.Plane.HORIZONTAL) {
                    val next = current.relative(dir)
                    if (next in allIceBlocks && next !in visited) {
                        visited.add(next)
                        queue.add(next)
                    }
                }
            }

            clusters.add(cluster)
        }

        clusters.sortBy { cluster -> cluster.minOf { it.distSqr(previousEnd) } }

        for ((i, cluster) in clusters.withIndex()) {
            val spaces = cluster.toList()

            val start = spaces.firstOrNull { pos ->
                Direction.Plane.HORIZONTAL.any { dir ->
                    val neighbor = pos.relative(dir)
                    val adjacentBlock = WorldUtils.getBlockAt(neighbor)
                    val adjacentBelow = WorldUtils.getBlockAt(neighbor.below())

                    i == 0 && (adjacentBlock == Blocks.POLISHED_ANDESITE || adjacentBelow == Blocks.POLISHED_ANDESITE) ||
                        adjacentBlock == Blocks.STONE_BRICK_STAIRS || adjacentBelow == Blocks.STONE_BRICK_STAIRS
                } && pos.distSqr(previousEnd) < 200
            } ?: spaces.minBy { it.distSqr(previousEnd) }

            val end = spaces.maxBy { it.distSqr(start) }

            val puzzle = IceFillPuzzle(spaces, start, end)
            puzzle.solve()

            if (puzzle.path.isNotEmpty()) {
                puzzles.add(puzzle)
                previousEnd = end
            }
        }
    }

    private class IceFillPuzzle(val spaces: List<BlockPos>, val start: BlockPos, val end: BlockPos) {
        var path = mutableListOf<BlockPos>()

        private val graph = spaces.associateWith { pos ->
            Direction.Plane.HORIZONTAL.map { pos.relative(it) }.filter { it in spaces }
        }

        fun solve() {
            val visited = mutableSetOf<BlockPos>()
            visited.add(start)

            val tempPath = ArrayList<BlockPos>(spaces.size)
            tempPath.add(start)

            if (dfs(start, visited, tempPath)) {
                path = tempPath
            }
        }

        private fun dfs(current: BlockPos, visited: MutableSet<BlockPos>, currentPath: MutableList<BlockPos>): Boolean {
            if (visited.size == spaces.size) return current == end
            if (current == end) return false
            val neighbors = graph[current] ?: return false

            val sortedNeighbors = neighbors.filter { it !in visited }.sortedBy { neighbor ->
                (graph[neighbor] ?: emptyList()).count { it !in visited }
            }

            for (next in sortedNeighbors) {
                visited.add(next)
                currentPath.add(next)

                if (dfs(next, visited, currentPath)) return true

                currentPath.removeAt(currentPath.size - 1)
                visited.remove(next)
            }

            return false
        }

        fun draw(ctx: RenderContext, color: Color) {
            if (path.isEmpty()) return

            path.forEachIndexed { index, pos ->
                if (index > 0) {
                    val prev = path[index - 1]
                    Render3D.renderLine(
                        ctx,
                        Vec3(prev.x + 0.5, prev.y + 0.01, prev.z + 0.5),
                        Vec3(pos.x + 0.5, pos.y + 0.01, pos.z + 0.5),
                        color, thickness = 7f
                    )
                }
            }
        }
    }
}