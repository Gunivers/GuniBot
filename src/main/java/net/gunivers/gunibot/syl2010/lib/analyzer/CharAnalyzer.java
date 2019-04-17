package net.gunivers.gunibot.syl2010.lib.analyzer;

public class CharAnalyzer extends ElementList<Character> {

	private static final long serialVersionUID = 1L;

	public CharAnalyzer(Character[] chars) {
		super(chars);
	}

	public CharAnalyzer(CharAnalyzer element_list) {
		super(element_list);
	}

	public CharAnalyzer(String arg) {
		this(charToCharacterArray(arg.toCharArray()));
	}

	private static Character[] charToCharacterArray(char[] chars) {
		Character[] char_list = new Character[chars.length];
		for(int i=0;i<chars.length;i++) {
			char_list[i] = Character.valueOf(chars[i]);
		}
		return char_list;
	}

}
