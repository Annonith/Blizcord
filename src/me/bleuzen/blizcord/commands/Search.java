package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.Bot;
import me.bleuzen.blizcord.PlayerThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Search extends Command {

	@Override
	public String getName() {
		return "search";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify a video title. Put it behind this command.``").queue();
			return;
		}

		Bot.join(); // try to join if not already

		if(Bot.joined) { // if successfully joined
			PlayerThread.loadAndPlay(channel, ("ytsearch:" + arg), true, false); // use "ytsearch:" prefix of lavaplayer
		}
	}

}
