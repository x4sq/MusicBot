package com.jagrosh.jmusicbot.commands.slash.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.slash.SlashAdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 * Slash command version of SetdjCmd.
 * Uses Discord's native ROLE option type.
 */
public class SlashSetdjCmd extends SlashAdminCommand
{
    public SlashSetdjCmd(Bot bot)
    {
        this.name = "setdj";
        this.help = "sets the DJ role for certain music commands";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.options = Collections.singletonList(
                new OptionData(OptionType.ROLE, "role",
                        "Role to assign as DJ (omit to clear the DJ role)", false)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());

        if (!event.hasOption("role"))
        {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess()
                    + " DJ role cleared; Only Admins can use the DJ commands.").queue();
            return;
        }

        Role role = event.optRole("role");
        if (role == null)
        {
            event.reply(event.getClient().getError() + " Could not find the specified role.")
                    .setEphemeral(true).queue();
            return;
        }

        s.setDJRole(role);
        event.reply(event.getClient().getSuccess() + " DJ commands can now be used by users with the **"
                + role.getName() + "** role.").queue();
    }
}
