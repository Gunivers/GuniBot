package net.gunivers.gunibot.command.lib.nodes;

import java.util.function.Supplier;

public enum EnumNode {

	INTEGER(NodeInteger::new),
	BOOLEAN(NodeBoolean::new),
	STRING(NodeString::new),
	
	USER(NodeUser::new),
	
	TEXT_CHANNEL(NodeTextChannel::new),
	
	INFINITY(NodeInfinite::new),
	;
	
	private Supplier<TypeNode<?>> fun;
	
	EnumNode(Supplier<TypeNode<?>> function) {
		fun = function;
	}
	
	public TypeNode<?> createInstance() {
		return fun.get();
	}
	
	public static EnumNode valueOfIgnoreCase(String s) {
		return EnumNode.valueOf(s.toUpperCase());
	}
	
	public static void main(String... args) {
		EnumNode n = EnumNode.valueOf("test");
		System.out.println(n);
	}
	
}
