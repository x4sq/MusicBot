package com.jagrosh.jmusicbot.commands.slash.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashDJCommand;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of RepeatCmd.
 */
public class SlashRepeatCmd extends SlashDJCommand
{
    public SlashRepeatCmd(Bot bot)
    {
        super(bot);
        this.name = "repeat";
        this.help = "sets the repeat mode for the queue";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "mode",
                        "Repeat mode: off, all, or single. Omit to toggle between off and all.", false)
                        .addChoice("off", "off")
                        .addChoice("all", "all")
                        .addChoice("single", "single")
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        String args = event.optString("mode", "");
        RepeatMode value;

        if (args.isEmpty())
        {
            value = settings.getRepeatMode() == RepeatMode.OFF ? RepeatMode.ALL : RepeatMode.OFF;
        }
        else
        {
            switch (args.toLowerCase())
            {
                case "off":
                case "false":
                    value = RepeatMode.OFF;
                    break;
                case "all":
                case "on":
                case "true":
                    value = RepeatMode.ALL;
                    break;
                case "single":
                case "one":
                    value = RepeatMode.SINGLE;
                    break;
                default:
                    event.reply("Valid options are `off`, `all`, or `single`.")
                            .setEphemeral(true).queue();
                    return;
            }
        }

        settings.setRepeatMode(value);
        event.reply(event.getClient().getSuccess()
                + " Repeat mode is now `" + value.getUserFriendlyName() + "`").queue();
    }
}
