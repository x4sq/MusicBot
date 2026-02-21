package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of VolumeCmd.
 */
public class SlashVolumeCmd extends SlashDJCommand
{
    public SlashVolumeCmd(Bot bot)
    {
        super(bot);
        this.name = "volume";
        this.help = "sets or shows the current volume";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "level",
                        "Volume level (0-150). Omit to show current volume.", false)
                        .setMinValue(0).setMaxValue(150)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        int volume = handler.getPlayer().getVolume();

        if (!event.hasOption("level"))
        {
            event.reply(FormatUtil.volumeIcon(volume) + " Current volume is `" + volume + "`").queue();
            return;
        }

        int nvolume = (int) event.optLong("level", -1);
        if (nvolume < 0 || nvolume > 150)
        {
            event.reply(event.getClient().getError()
                    + " Volume must be a valid integer between 0 and 150!").setEphemeral(true).queue();
            return;
        }

        handler.getPlayer().setVolume(nvolume);
        settings.setVolume(nvolume);
        event.reply(FormatUtil.volumeIcon(nvolume) + " Volume changed from `" + volume + "` to `" + nvolume + "`").queue();
    }
}
