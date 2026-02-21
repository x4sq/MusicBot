package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of QueueTypeCmd.
 */
public class SlashQueueTypeCmd extends SlashAdminCommand
{
    public SlashQueueTypeCmd(Bot bot)
    {
        this.name = "queuetype";
        this.help = "changes the queue type";
        this.aliases = bot.getConfig().getAliases(this.name);

        OptionData option = new OptionData(OptionType.STRING, "type",
                "Queue type to use (omit to show current type)", false);
        for (String name : QueueType.getNames())
            option.addChoice(name, name.toUpperCase());
        this.options = Collections.singletonList(option);
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());

        if (!event.hasOption("type"))
        {
            QueueType currentType = settings.getQueueType();
            event.reply(currentType.getEmoji() + " Current queue type is: `"
                    + currentType.getUserFriendlyName() + "`.").queue();
            return;
        }

        String typeName = event.optString("type", "").toUpperCase();
        QueueType value;
        try
        {
            value = QueueType.valueOf(typeName);
        }
        catch (IllegalArgumentException e)
        {
            event.reply(event.getClient().getError() + " Invalid queue type. Valid types are: ["
                    + String.join("|", QueueType.getNames()) + "]").setEphemeral(true).queue();
            return;
        }

        if (settings.getQueueType() != value)
        {
            settings.setQueueType(value);
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler != null)
                handler.setQueueType(value);
        }

        event.reply(value.getEmoji() + " Queue type was set to `" + value.getUserFriendlyName() + "`.").queue();
    }
}
