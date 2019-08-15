package net.gunivers.gunibot.utils.tuple;

import java.io.Serializable;

public class Tuple4<A, B, C, D> extends Tuple implements Serializable {
	
	private static final long serialVersionUID = 8785115253519423457L;
	
	public final A _1;
	public final B _2;
	public final C _3;
	public final D _4;

	public Tuple4(A value1, B value2, C value3, D value4) {
		_1 = value1;
		_2 = value2;
		_3 = value3;
		_4 = value4;
	}
	
	public boolean equals(Tuple4<A, B, C, D> tuple) {
		return _1.equals(tuple._1) && _2.equals(tuple._2) && _3.equals(tuple._3) && _4.equals(tuple._4);
	}
}
