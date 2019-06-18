package net.gunivers.gunibot.core.command.nodes;

import java.util.function.Supplier;

public enum NodeEnum {

	INTEGER(NodeInt::new),
	BOOLEAN(NodeBoolean::new),
	REFERENCE(NodeReference::new),
	STRING(NodeString::new);
	
	private Supplier<TypeNode> fun;
	
	NodeEnum(Supplier<TypeNode> function) {
		fun = function;
	}
	
	public TypeNode createInstance() {
		return fun.get();
	}
	
	public static NodeEnum valueOfIgnoreCase(String s) {
		return NodeEnum.valueOf(s.toUpperCase());
	}
}
