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
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 * Base class for slash-command versions of music commands.
 * Replicates the voice/channel checks from {@link com.jagrosh.jmusicbot.commands.MusicCommand}.
 */
public abstract class SlashMusicCommand extends SlashCommand
{
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;

    public SlashMusicCommand(Bot bot)
    {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }

    @Override
    protected void execute(SlashCommandEvent event)
    {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());

        bot.getPlayerManager().setUpHandler(event.getGuild());

        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler())
                .isMusicPlaying(event.getJDA()))
        {
            event.reply(event.getClient().getError() + " There must be music playing to use that!").setEphemeral(true).queue();
            return;
        }

        if (beListening)
        {
            AudioChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null)
                current = settings.getVoiceChannel(event.getGuild());

            GuildVoiceState userState = event.getMember().getVoiceState();
            if (userState.getChannel() == null || userState.isDeafened()
                    || (current != null && !userState.getChannel().equals(current)))
            {
                event.reply("You must be listening in "
                        + (current == null ? "a voice channel" : current.getAsMention())
                        + " to use that!").setEphemeral(true).queue();
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel()))
            {
                event.reply("You cannot use that command in an AFK channel!").setEphemeral(true).queue();
                return;
            }

            if (event.getGuild().getSelfMember().getVoiceState().getChannel() == null)
            {
                try
                {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                }
                catch (PermissionException ex)
                {
                    event.reply(event.getClient().getError() + " I am unable to connect to "
                            + userState.getChannel().getAsMention() + "!").setEphemeral(true).queue();
                    return;
                }
            }
        }

        doCommand(event);
    }

    public abstract void doCommand(SlashCommandEvent event);
}
