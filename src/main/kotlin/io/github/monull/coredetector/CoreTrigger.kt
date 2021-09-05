package io.github.monull.coredetector

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scoreboard.Team
import java.util.*
import kotlin.collections.ArrayList

class CoreTrigger(val uniqueId: UUID) {
    lateinit var name: String
    lateinit var color: BarColor
    lateinit var block: Block
    lateinit var type: Material
    var team: Team? = null
    var health = 100.0
    var trigger = ArrayList<String>()
    var update = true

    lateinit var bossBar: BossBar

    fun activate() {
        health--
        block.type = type
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
                bossBar.isVisible = false
                bossBar.removeAll()
                trigger.forEach { command ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                }
                team?.players?.forEach {
                    it.player?.gameMode = GameMode.SPECTATOR
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

        config["name"] = name
        config["color"] = color.name
        config["triggers"] = trigger
        config["health"] = health
        config["update"] = update
        config["type"] = type.toString()
        config["team"] = team?.name

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

        name = config.getString("name")!!
        color = BarColor.valueOf(config.getString("color")!!)
        trigger = config.getStringList("triggers") as ArrayList<String>
        health = config.getDouble("health")
        update = config.getBoolean("update")
        type = Material.valueOf(config.getString("type")!!)
        bossBar = Bukkit.createBossBar(name, color, BarStyle.SEGMENTED_10).apply {
            isVisible = true
        }
        if (config.getString("team") != null) {
            team = Bukkit.getScoreboardManager().mainScoreboard.getTeam(config.getString("team")!!)
        }
    }
}