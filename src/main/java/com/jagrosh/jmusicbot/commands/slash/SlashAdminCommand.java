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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;

/**
 * Base class for slash-command versions of admin commands.
 * Replicates the admin permission check from {@link com.jagrosh.jmusicbot.commands.AdminCommand}.
 */
public abstract class SlashAdminCommand extends SlashCommand
{
    public SlashAdminCommand()
    {
        this.category = new Category("Admin");
        this.guildOnly = true;
    }

    @Override
    protected final void execute(SlashCommandEvent event)
    {
        boolean isOwner = event.getUser().getId().equals(event.getClient().getOwnerId());
        boolean isAdmin = event.getGuild() != null
                && event.getMember().hasPermission(Permission.MANAGE_SERVER);

        if (!isOwner && !isAdmin)
        {
            event.reply(event.getClient().getError()
                    + " Only server admins can use that command!").setEphemeral(true).queue();
            return;
        }
        doCommand(event);
    }

    public abstract void doCommand(SlashCommandEvent event);
}
