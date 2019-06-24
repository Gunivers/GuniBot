package net.gunivers.gunibot.utils.tuple;

public class Tuple5<A, B, C, D, E> extends Tuple {
	
	private static final long serialVersionUID = 6L;

	public final A _1;
	public final B _2;
	public final C _3;
	public final D _4;
	public final E _5;

	public Tuple5(A value1, B value2, C value3, D value4, E value5) {
		_1 = value1;
		_2 = value2;
		_3 = value3;
		_4 = value4;
		_5 = value5;
	}
	
	public boolean equals(Tuple5<A, B, C, D, E> tuple) {
		return _1.equals(tuple._1) && _2.equals(tuple._2) && _3.equals(tuple._3) && _4.equals(tuple._4) && _5.equals(tuple._5);
	}
}
