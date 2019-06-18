package net.gunivers.gunibot.core.command.keys;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public enum KeyEnum {
	
	TYPE(KeyType::getInstance, Position.NOT_IN_ROOT),
	ID(KeyId::getInstance, Position.DEFAULT),
	MATCHES(KeyMatches::getInstance, Position.NOT_IN_ROOT),
	EXECUTE(KeyExecute::getInstance, Position.DEFAULT),
	KEEP_VALUE(KeyKeepValue::getInstance, Position.NOT_IN_ROOT),
	TAG(KeyTag::getInstance, Position.NOT_IN_ROOT),
	PERMISSIONS(KeyPermissions::getInstance, Position.ONLY_IN_ROOT),
	ALIASES(KeyAliases::getInstance, Position.ONLY_IN_ROOT),
	DESCRIPTION(KeyDescription::getInstance, Position.ONLY_IN_ROOT),
	ARGUMENTS(KeyArguments::getInstance, Position.DEFAULT);
	
	private Supplier<Key> clazz;
	private Position pos;
	
	KeyEnum(Supplier<Key> clazz, Position pos) {
		this.clazz = clazz;
		this.pos = pos;
	}

	public Key getClazz() {
		return clazz.get();
	}

	public static List<KeyEnum> getByPos(Position pos1, Position pos2) {
		List<KeyEnum> list = new LinkedList<>();
		for(KeyEnum ke : KeyEnum.values())
			if(ke.pos == pos1 || ke.pos == pos2)
				list.add(ke);
		return list;
	}
	
	public enum Position {
		ONLY_IN_ROOT,
		NOT_IN_ROOT,
		DEFAULT;
	}
	
}
