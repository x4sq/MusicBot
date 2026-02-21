package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of ForceRemoveCmd.
 * Uses Discord's native USER option type for clean user selection.
 */
public class SlashForceRemoveCmd extends SlashDJCommand
{
    public SlashForceRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "forceremove";
        this.help = "removes all entries by a user from the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
        this.beListening = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "user",
                        "The user whose songs to remove from the queue", true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty())
        {
            event.reply("There is nothing in the queue!").setEphemeral(true).queue();
            return;
        }

        User target = event.optUser("user");
        if (target == null)
        {
            event.reply("Unable to find the specified user!").setEphemeral(true).queue();
            return;
        }

        int count = handler.getQueue().removeAll(target.getIdLong());
        if (count == 0)
        {
            event.reply(event.getClient().getWarning() + " **" + target.getName()
                    + "** doesn't have any songs in the queue!").queue();
        }
        else
        {
            event.reply(event.getClient().getSuccess() + " Successfully removed `" + count
                    + "` entries from " + FormatUtil.formatUsername(target) + ".").queue();
        }
    }
}
