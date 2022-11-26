package com.github.tanokun.linkdiscord

import com.github.tanokun.linkdiscord.appender.ConsoleAppender
import com.github.tanokun.linkdiscord.command.CommandListener
import com.github.tanokun.linkdiscord.listener.EmbedListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import javax.security.auth.login.LoginException


lateinit var plugin: LinkDiscord private set

class LinkDiscord : JavaPlugin() {
    lateinit var bot: JDA private set

    private lateinit var bukkitAudiences: BukkitAudiences

    private lateinit var activity: String

    var consoleChannelId: Long = lazy { config.getLong("consoleChannel") }.value
        set(value) {
            config.set("consoleChannel", value)
            saveConfig()
            field = value
        }

    val users: MutableList<Long> = lazy { config.getLongList("users") }.value

    val logs = mutableListOf<String>()

    override fun onLoad() {
        plugin = this
        saveDefaultConfig()

        try {
            bot = JDABuilder.createDefault(config.getString("botToken"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(CommandListener())
                .build()
        } catch (e: LoginException) {
            LogManager.getRootLogger().warn("botTakenが間違っているようです。ご確認ください。")
            return
        }

        logger()

        bot.updateCommands().queue()

        bot.upsertCommand("setup", "データの設定をします")
            .addSubcommands(SubcommandData("console", "遠隔コンソールの設定をします"))
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            .queue()

        bot.upsertCommand("cmd", "コンソールからコマンドを実行します")
            .addOption(OptionType.STRING, "command", "コマンドの内容")
            .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
            .queue()
    }

    override fun onEnable() {
        bukkitAudiences = BukkitAudiences.create(plugin)
        activity = config.getString("activity") ?: ""

        EmbedListener()

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            val channel = plugin.bot.getTextChannelById(plugin.consoleChannelId) ?: return@Runnable
            if (logs.isEmpty()) return@Runnable

            var text = ""
            logs.forEach {
                if (text.length + it.length >= 2000) {
                    channel.sendMessage(text).queue()
                    text = ""
                    return@forEach
                }
                text += it + System.lineSeparator()
            }

            if (text != "") channel.sendMessage(text).queue()

            logs.clear()

        }, 1L, 20L)

        Bukkit.getScheduler().runTaskTimer(this, Runnable {
            bot.presence.activity = Activity.playing(activityFormat())
        }, 1L, 100L)
    }

    override fun onDisable() {
        bot.shutdownNow()

        val log: Logger = LogManager.getRootLogger() as Logger
        log.removeAppender(ConsoleAppender)
    }

    private fun logger() {
        val log: Logger = LogManager.getRootLogger() as Logger
        log.addAppender(ConsoleAppender)
    }

    private fun activityFormat(): String {
        return activity.replace("[onlinePlayerSize]", Bukkit.getOnlinePlayers().size.toString())
            .replace("[maxPlayerSize]", Bukkit.getServer().maxPlayers.toString())
    }
}