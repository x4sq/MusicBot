package com.jagrosh.jmusicbot.commands.slash.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.slash.SlashMusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of RemoveCmd.
 */
public class SlashRemoveCmd extends SlashMusicCommand
{
    public SlashRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "remove";
        this.help = "removes a song from the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "position",
                        "Position in the queue to remove (use 0 to remove all your songs)", true)
                        .setMinValue(0)
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

        int pos = (int) event.optLong("position", 0);

        // Position 0 means "remove all my songs"
        if (pos == 0)
        {
            int count = handler.getQueue().removeAll(event.getUser().getIdLong());
            if (count == 0)
                event.reply(event.getClient().getWarning() + " You don't have any songs in the queue!").queue();
            else
                event.reply(event.getClient().getSuccess() + " Successfully removed your " + count + " entries.").queue();
            return;
        }

        if (pos < 1 || pos > handler.getQueue().size())
        {
            event.reply("Position must be a valid integer between 1 and " + handler.getQueue().size() + "!")
                    .setEphemeral(true).queue();
            return;
        }

        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));

        QueuedTrack qt = handler.getQueue().get(pos - 1);
        if (qt.getIdentifier() == event.getUser().getIdLong())
        {
            handler.getQueue().remove(pos - 1);
            event.reply(event.getClient().getSuccess() + " Removed **" + qt.getTrack().getInfo().title
                    + "** from the queue").queue();
        }
        else if (isDJ)
        {
            handler.getQueue().remove(pos - 1);
            User u;
            try
            {
                u = event.getJDA().getUserById(qt.getIdentifier());
            }
            catch (Exception e)
            {
                u = null;
            }
            event.reply(event.getClient().getSuccess() + " Removed **" + qt.getTrack().getInfo().title
                    + "** from the queue (requested by "
                    + (u == null ? "someone" : "**" + u.getName() + "**") + ")").queue();
        }
        else
        {
            event.reply("You cannot remove **" + qt.getTrack().getInfo().title
                    + "** because you didn't add it!").setEphemeral(true).queue();
        }
    }
}
