package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SkipratioCmd.
 */
public class SlashSkipratioCmd extends SlashAdminCommand
{
    public SlashSkipratioCmd(Bot bot)
    {
        this.name = "setskip";
        this.help = "sets a server-specific skip percentage";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "percentage",
                        "Skip vote percentage required (0-100, default 55)", true)
                        .setMinValue(0).setMaxValue(100)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        int val = (int) event.optLong("percentage", -1);
        if (val < 0 || val > 100)
        {
            event.reply(event.getClient().getError()
                    + " The provided value must be between 0 and 100!").setEphemeral(true).queue();
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        s.setSkipRatio(val / 100.0);
        event.reply(event.getClient().getSuccess() + " Skip percentage has been set to `" + val
                + "%` of listeners on *" + event.getGuild().getName() + "*").queue();
    }
}
