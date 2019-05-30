package net.gunivers.gunibot.utils.tuple;

public class Tuple3<A, B, C> extends Tuple {
	
	private static final long serialVersionUID = 4L;
	
	public final A _1;
	public final B _2;
	public final C _3;

	public Tuple3(A value1, B value2, C value3) {
		_1 = value1;
		_2 = value2;
		_3 = value3;
	}

}
