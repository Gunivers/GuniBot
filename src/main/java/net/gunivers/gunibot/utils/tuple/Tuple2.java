package net.gunivers.gunibot.utils.tuple;

public class Tuple2<A, B> extends Tuple {
	
	private static final long serialVersionUID = 3L;
	
	public final A _1;
	public final B _2;
	
	Tuple2(A value1, B value2) {
		this._1 = value1;
		this._2 = value2;
	}
}
