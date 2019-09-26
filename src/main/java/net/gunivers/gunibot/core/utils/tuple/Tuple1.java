package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple1<A> extends Tuple implements Serializable {
	
	
	private static final long serialVersionUID = -7472781773054022348L;
	
	public final A _1;
	
	Tuple1(A value) {
		_1 = value;
	}
	
	public boolean equals(Tuple1<A> tuple) {
		return _1.equals(tuple._1);
	}
}
