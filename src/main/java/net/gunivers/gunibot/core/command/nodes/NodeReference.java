package net.gunivers.gunibot.core.command.nodes;

import java.util.Arrays;
import java.util.List;

import net.gunivers.gunibot.core.command.CommandParser;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.keys.KeyEnum;

public class NodeReference extends TypeNode {

	private String id;
	
	@Override
	protected boolean matchesNode(String s) {
		return true;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		if(s.matches("[0-9a-z_]+")) {
			id = s;
			CommandParser.addReference(id);
		} else
			throw new JsonCommandFormatException("Le couple (matches, " + s + ") ne corresponds pas au type reference");
	}

	@Override
	public List<KeyEnum> blacklist() {
		return Arrays.asList(KeyEnum.EXECUTE, KeyEnum.ARGUMENTS, KeyEnum.KEEP_VALUE);
	}
	
	@Override
	public List<KeyEnum> mandatory() {
		return Arrays.asList(KeyEnum.MATCHES);
	}
}
