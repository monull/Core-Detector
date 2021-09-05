package io.github.monull.coredetector.plugin

import io.github.monull.coredetector.CoreTriggerManager
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component.text
import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team

class CoreDetectorPlugin : JavaPlugin() {
    lateinit var manager: CoreTriggerManager

    override fun onEnable() {
        manager = CoreTriggerManager(dataFolder)

        server.scheduler.runTaskTimer(this, CoreScheduler(this), 0L, 1L)
        setupCommands()
    }

    private fun setupCommands() = kommand {
        register("coretrigger", "ct") {

            val barColorArgument = dynamic { _, input ->
                BarColor.valueOf(input)
            }.apply {
                suggests {
                    val barColors = BarColor.values()
                    val colors = arrayListOf<String>()
                    barColors.forEach { colors += it.name.uppercase() }
                    suggest(colors)
                }
            }
            then("register") {
                then("name" to string()) {
                    then("color" to barColorArgument) {
                        requires { playerOrNull != null }
                        then("team" to team()) {
                            executes {
                                val sender = sender as Player
                                val block = sender.getTargetBlock(64)
                                val team: Team = it["team"]

                                if (block == null) {
                                    feedback(text("지정할 블럭이 없습니다."))
                                } else {
                                    manager.registerTrigger(block, it["name"], it["color"], team)
                                    feedback(text("${block.world.name} ${block.x} ${block.y} ${block.z} ${team.name}"))
                                }
                            }
                        }
                        executes {
                            val sender = sender as Player
                            val block = sender.getTargetBlock(64)

                            if (block == null) {
                                feedback(text("지정할 블럭이 없습니다."))
                            } else {
                                manager.registerTrigger(block, it["name"], it["color"])
                                feedback(text("${block.world.name} ${block.x} ${block.y} ${block.z}"))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        manager.save()
    }
}