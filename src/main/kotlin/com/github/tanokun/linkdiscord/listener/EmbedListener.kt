package com.github.tanokun.linkdiscord.listener

import com.github.tanokun.linkdiscord.plugin
import net.dv8tion.jda.api.EmbedBuilder
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.awt.Color

class EmbedListener: Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val embed = EmbedBuilder().setColor(Color.GREEN).setTitle("${e.player.name}がサーバーに参加しました").build()
        val channel = plugin.bot.getTextChannelById(plugin.consoleChannelId) ?: return
        channel.sendMessageEmbeds(embed).queue()
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val embed = EmbedBuilder().setColor(Color.CYAN).setTitle("${e.player.name}がサーバーから離脱しました").build()
        val channel = plugin.bot.getTextChannelById(plugin.consoleChannelId) ?: return
        channel.sendMessageEmbeds(embed).queue()
    }
}