package net.gunivers.gunibot.utils.tuple;

public class Tuple1<A> extends Tuple {
	
	private static final long serialVersionUID = 2L;
	
	public final A _1;
	
	Tuple1(A value) {
		_1 = value;
	}
	
	public boolean equals(Tuple1<A> tuple) {
		return _1.equals(tuple._1);
	}
}
