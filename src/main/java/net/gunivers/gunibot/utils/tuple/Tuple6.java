package net.gunivers.gunibot.utils.tuple;

public class Tuple6<A, B, C, D, E, F> extends Tuple {
	
	private static final long serialVersionUID = 7L;
	
	public final A _1;
	public final B _2;
	public final C _3;
	public final D _4;
	public final E _5;
	public final F _6;

	 Tuple6(A value1, B value2, C value3, D value4, E value5, F value6) {
		_1 = value1;
		_2 = value2;
		_3 = value3;
		_4 = value4;
		_5 = value5;
		_6 = value6;
	}

	@Override
	public String toString()
	{
		return "T6[" + _1 +','+ _2 +','+ _3 +','+ _4 +','+ _5 +','+ _6 + "]";
	}
}
