/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.slash;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

/**
 * Base class for slash-command versions of DJ commands.
 * Replicates the DJ permission check from {@link com.jagrosh.jmusicbot.commands.DJCommand}.
 */
public abstract class SlashDJCommand extends SlashMusicCommand
{
    public SlashDJCommand(Bot bot)
    {
        super(bot);
        this.category = new Category("DJ");
    }

    /**
     * Checks whether the user invoking the slash command has DJ permissions.
     */
    public static boolean checkDJPermission(SlashCommandEvent event)
    {
        if (event.getUser().getId().equals(event.getClient().getOwnerId()))
            return true;
        if (event.getGuild() == null)
            return true;
        if (event.getMember().hasPermission(Permission.MANAGE_SERVER))
            return true;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        Role dj = settings.getRole(event.getGuild());
        return dj != null && (event.getMember().getRoles().contains(dj)
                || dj.getIdLong() == event.getGuild().getIdLong());
    }

    /**
     * Adds DJ permission check before delegating to the parent music checks and then doCommand().
     */
    @Override
    protected void execute(SlashCommandEvent event)
    {
        if (!checkDJPermission(event))
        {
            event.reply(event.getClient().getError() + " Only DJs can use that command!").setEphemeral(true).queue();
            return;
        }
        // Delegate to SlashMusicCommand.execute() which runs bePlaying/beListening checks then calls doCommand()
        super.execute(event);
    }
}
