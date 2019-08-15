package net.gunivers.gunibot.utils.tuple;

import java.io.Serializable;

public class Tuple3<A, B, C> extends Tuple implements Serializable {
	
	private static final long serialVersionUID = -7669164929093003736L;
	
	public final A _1;
	public final B _2;
	public final C _3;

	public Tuple3(A value1, B value2, C value3) {
		_1 = value1;
		_2 = value2;
		_3 = value3;
	}

	public boolean equals(Tuple3<A, B, C> tuple) {
		return _1.equals(tuple._1) && _2.equals(tuple._2) && _3.equals(tuple._3);
	}
}
