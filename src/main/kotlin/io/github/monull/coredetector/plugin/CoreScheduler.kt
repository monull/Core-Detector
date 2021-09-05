package io.github.monull.coredetector.plugin

import org.bukkit.Material

class CoreScheduler(val plugin: CoreDetectorPlugin) : Runnable {
    override fun run() {
        plugin.manager.triggers.values.forEach {
            it.update()
            if (it.block.type == Material.AIR && it.update) {
                it.activate()
            }
            if (!it.update) it.block.type = Material.AIR
        }
    }
}