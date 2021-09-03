package io.github.monull.coredetector

import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*
import kotlin.collections.ArrayList

class CoreTrigger(val uniqueId: UUID) {
    lateinit var name: String
    lateinit var color: BarColor
    lateinit var block: Block
    var health = 100.0
    var trigger = ArrayList<String>()
    var update = true

    var bossBar = Bukkit.createBossBar(name, color, BarStyle.SEGMENTED_10).apply {
        isVisible = true
    }

    fun activate() {
        health--
    }

    fun update() {
        if (update) {
            bossBar.progress = (health / 100.0).coerceIn(0.0, 1.0)
            block.location.getNearbyPlayers(30.0).forEach { player ->
                bossBar.addPlayer(player)
            }
            bossBar.players.forEach { player ->
                if (block.location.distance(player.location) > 30) {
                    bossBar.removePlayer(player)
                }
            }

            if (bossBar.progress == 0.0) {
                trigger.forEach { command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                }
                update = false
            }
        }
    }

    fun save(): YamlConfiguration {
        val config = YamlConfiguration()
        config.createSection("block").let { section ->
            section["world"] = block.world.name
            section["x"] = block.x
            section["y"] = block.y
            section["z"] = block.z
        }

        config["triggers"] = trigger

        return config
    }

    fun load(config: YamlConfiguration) {
        this.block = config.getConfigurationSection("block")!!.let { section ->
            Bukkit.getWorld(section.getString("world")!!
            )!!.getBlockAt(
                section.getInt("x"),
                section.getInt("y"),
                section.getInt("z")
            )
        }

        trigger = config.getStringList("triggers") as ArrayList<String>
    }
}