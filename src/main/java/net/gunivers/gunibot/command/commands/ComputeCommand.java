package net.gunivers.gunibot.command.commands;

import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.core.command.Command;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class ComputeCommand extends Command {

	public void compute(MessageCreateEvent e, List<String> args) {
		try {
			String expr = args.stream().collect(Collectors.joining(" "));
			Expression exp = new ExpressionBuilder(expr).build();
			e.getMessage().getChannel().block().createMessage(expr + " = **" + exp.evaluate() + "**").subscribe();
		} catch(ArithmeticException | IllegalArgumentException exc) {
			e.getMessage().getChannel().block().createMessage(exc.getMessage()).subscribe();
		}
	}

	@Override
	public String getSyntaxFile() {
		return "compute.json";
	}

}
