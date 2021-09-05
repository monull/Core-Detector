package io.github.monull.coredetector

import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scoreboard.Team
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class CoreTriggerManager(
    private val folder: File
) {
    val triggers = HashMap<Block, CoreTrigger>()

    init {
        folder.mkdirs()
        load()
    }

    fun registerTrigger(block: Block, name: String, color: BarColor, team: Team? = null) {
        triggers.computeIfAbsent(block) {
            CoreTrigger(UUID.randomUUID()).apply {
                this.block = block
                this.color = color
                this.name = name
                this.type = block.type
                this.bossBar = Bukkit.createBossBar(name, color, BarStyle.SEGMENTED_10).apply {
                    isVisible = true
                }
                this.team = team
            }
        }
    }

    fun getTrigger(block: Block): CoreTrigger? {
        return triggers[block]
    }

    private fun load() {
        folder.listFiles { _, name -> name.endsWith(".yml") }!!.forEach { file ->
            val uniqueId = UUID.fromString(file.nameWithoutExtension)
            val config = YamlConfiguration.loadConfiguration(file)
            val trigger = CoreTrigger(uniqueId).apply { load(config) }

            triggers[trigger.block] = trigger
        }
    }

    fun save() {
        folder.mkdirs()

        triggers.values.forEach { trigger ->
            val config = trigger.save()
            val file = File(folder, "${trigger.uniqueId}.yml")
            config.save(file)
        }
    }
}