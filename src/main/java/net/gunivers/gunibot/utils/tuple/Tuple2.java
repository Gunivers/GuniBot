package net.gunivers.gunibot.utils.tuple;

import java.io.Serializable;

public class Tuple2<A, B> extends Tuple implements Serializable {
	
	private static final long serialVersionUID = -184640039472655359L;
	
	public final A _1;
	public final B _2;
	
	Tuple2(A value1, B value2) {
		this._1 = value1;
		this._2 = value2;
	}
	
	public boolean equals(Tuple2<A, B> tuple) {
		return _1.equals(tuple._1) && _2.equals(tuple._2);
	}
}
