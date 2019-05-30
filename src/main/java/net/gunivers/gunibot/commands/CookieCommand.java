package net.gunivers.gunibot.commands;

import java.util.List;

import net.gunivers.gunibot.commands.lib.Command;

public class CookieCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "cookie.json";
	}
	
	public void giveCookie(List<String> args) {
		System.out.println(args.get(0) + " cookies for theo");
	}
	
	public void giveCookieAll(List<String> args) {
		System.out.println(args.get(0) + " Cookie for leirof");
	}

}
