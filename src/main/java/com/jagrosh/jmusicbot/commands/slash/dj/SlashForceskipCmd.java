package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;

/**
 * Slash command version of ForceskipCmd.
 */
public class SlashForceskipCmd extends SlashDJCommand
{
    public SlashForceskipCmd(Bot bot)
    {
        super(bot);
        this.name = "forceskip";
        this.help = "skips the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        event.reply(event.getClient().getSuccess() + " Skipped **"
                + handler.getPlayer().getPlayingTrack().getInfo().title + "** "
                + (rm.getOwner() == 0L ? "(autoplay)"
                        : "(requested by **" + FormatUtil.formatUsername(rm.user) + "**)")).queue();
        handler.getPlayer().stopTrack();
    }
}
