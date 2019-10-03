package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple1<A> extends Tuple implements Serializable {

    private static final long serialVersionUID = -7472781773054022348L;

    public final A value1;

    Tuple1(A value) {
	value1 = value;
    }

    public boolean equals(Tuple1<A> tuple) {
	return value1.equals(tuple.value1);
    }
}
