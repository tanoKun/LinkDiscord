package com.github.tanokun.linkdiscord.appender

import com.github.tanokun.linkdiscord.plugin
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


object ConsoleAppender: AbstractAppender("ConsoleAppender", null, null) {
    private val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
    private val CONSOLE_COLOR_LIST = mutableListOf<String>()

    init {
        start()

        CONSOLE_COLOR_LIST.add(127.toChar() + "0")
        CONSOLE_COLOR_LIST.add(127.toChar() + "1")
        CONSOLE_COLOR_LIST.add(127.toChar() + "2")
        CONSOLE_COLOR_LIST.add(127.toChar() + "3")
        CONSOLE_COLOR_LIST.add(127.toChar() + "4")
        CONSOLE_COLOR_LIST.add(127.toChar() + "5")
        CONSOLE_COLOR_LIST.add(127.toChar() + "6")
        CONSOLE_COLOR_LIST.add(127.toChar() + "7")
        CONSOLE_COLOR_LIST.add(127.toChar() + "8")
        CONSOLE_COLOR_LIST.add(127.toChar() + "9")
        CONSOLE_COLOR_LIST.add(127.toChar() + "a")
        CONSOLE_COLOR_LIST.add(127.toChar() + "b")
        CONSOLE_COLOR_LIST.add(127.toChar() + "c")
        CONSOLE_COLOR_LIST.add(127.toChar() + "d")
        CONSOLE_COLOR_LIST.add(127.toChar() + "e")
        CONSOLE_COLOR_LIST.add(127.toChar() + "f")
        CONSOLE_COLOR_LIST.add(127.toChar() + "k")
        CONSOLE_COLOR_LIST.add(127.toChar() + "l")
        CONSOLE_COLOR_LIST.add(127.toChar() + "m")
        CONSOLE_COLOR_LIST.add(127.toChar() + "n")
        CONSOLE_COLOR_LIST.add(127.toChar() + "o")
        CONSOLE_COLOR_LIST.add(127.toChar() + "r")
    }

    override fun append(e: LogEvent) {
        val loggerName = if (
            e.loggerName.contains("net.minecraft.server")
            || e.loggerName == "Minecraft"
            || e.loggerName == "") ""
        else "[${e.loggerName}] "

        var message = e.message.formattedMessage

        CONSOLE_COLOR_LIST.forEach {
            message = message.replace(it, "")
        }

        if (e.thrown == null) {
            plugin.logs.add("[${simpleDateFormat.format(Date(e.timeMillis))} ${e.level.name()}]: $loggerName$message")
        } else {
            val thrown = e.thrown

            val sw = StringWriter(); val pw = PrintWriter(sw)
            thrown.printStackTrace(pw)
            pw.flush()

            val channel = plugin.bot.getTextChannelById(plugin.consoleChannelId) ?: return
            var text = ""
            "[${simpleDateFormat.format(Date(e.timeMillis))} ${e.level.name()}]: $loggerName$sw".split(System.lineSeparator()).forEach {
                if (text.length + it.length >= 2000) {
                    channel.sendMessage("```$text```").queue()
                    text = ""
                    return@forEach
                }
                text += it + System.lineSeparator()
            }

            if (text != "") channel.sendMessage("```$text```").queue()

            pw.close()
        }
    }
}
