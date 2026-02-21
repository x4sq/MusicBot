package com.jagrosh.jmusicbot.commands.slash.general;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 * Slash command version of SettingsCmd.
 */
public class SlashSettingsCmd extends SlashCommand
{
    private static final String EMOJI = "\uD83C\uDFA7"; // 🎧
    private final Bot bot;

    public SlashSettingsCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "settings";
        this.help = "shows the bot settings for this server";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }

    @Override
    protected void execute(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        Role role = s.getRole(event.getGuild());

        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getMember().getColors().getPrimary())
                .setDescription("Text Channel: " + (tchan == null ? "Any" : "**#" + tchan.getName() + "**")
                        + "\nVoice Channel: " + (vchan == null ? "Any" : vchan.getAsMention())
                        + "\nDJ Role: " + (role == null ? "None" : "**" + role.getName() + "**")
                        + "\nCustom Prefix: " + (s.getPrefix() == null ? "None" : "`" + s.getPrefix() + "`")
                        + "\nRepeat Mode: " + (s.getRepeatMode() == RepeatMode.OFF
                                ? s.getRepeatMode().getUserFriendlyName()
                                : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                        + "\nQueue Type: " + (s.getQueueType() == QueueType.FAIR
                                ? s.getQueueType().getUserFriendlyName()
                                : "**" + s.getQueueType().getUserFriendlyName() + "**")
                        + "\nDefault Playlist: " + (s.getDefaultPlaylist() == null ? "None"
                                : "**" + s.getDefaultPlaylist() + "**"))
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream()
                                .filter(g -> g.getSelfMember().getVoiceState().getChannel() != null)
                                .count()
                        + " audio connections", null);

        event.reply(EMOJI + " **" + FormatUtil.filter(event.getJDA().getSelfUser().getName())
                + "** settings:")
                .addEmbeds(ebuilder.build())
                .queue();
    }
}
