package com.github.tanokun.linkdiscord.command

import com.github.tanokun.linkdiscord.plugin
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.Bukkit


class CommandListener: ListenerAdapter() {
    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        if (e.name == "setup") {
            when (e.subcommandName) {
                "console" -> {
                    plugin.consoleChannelId = e.channel.idLong
                    e.replyEmbeds(EmbedBuilder().setTitle("在のチャンネルを遠隔コンソールに設定しました").build()).setEphemeral(false).queue()
                    Bukkit.getConsoleSender()
                        .sendMessage("[LinkDiscord] §b遠隔コンソールが設定されました (ChannelID: ${plugin.consoleChannelId})")
                }

                else -> {
                    e.replyEmbeds(EmbedBuilder().setTitle("サブコマンドが存在しません").build()).setEphemeral(false).queue()
                }
            }
        } else if (e.name == "cmd") {
            if (!plugin.users.contains(e.user.idLong)) {
                e.replyEmbeds(EmbedBuilder().setTitle("権限がありません").build()).setEphemeral(false).queue()
                return
            }

            val cmd = e.getOption("command")?.asString ?: ""
            Bukkit.getScheduler().runTask(plugin, Runnable {
                plugin.logger.info("DiscordUser: ${e.user.name} が「$cmd」を実行しました")
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
            })
            e.replyEmbeds(EmbedBuilder().setTitle("DiscordUser: ${e.user.name} が「$cmd」を実行しました").build()).setEphemeral(false).queue()
        }
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        if (e.channel.idLong != plugin.consoleChannelId || e.author.isBot) return
        val msg = e.message.contentRaw

        Bukkit.getOnlinePlayers().forEach {
            it.sendMessage("§7[Discord] §7${e.author.name}>> $msg")
        }
    }
}