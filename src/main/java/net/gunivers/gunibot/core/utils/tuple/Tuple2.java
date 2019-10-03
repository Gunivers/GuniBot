package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple2<A, B> extends Tuple implements Serializable {

    private static final long serialVersionUID = -184640039472655359L;

    public final A value1;
    public final B value2;

    Tuple2(A value1, B value2) {
	this.value1 = value1;
	this.value2 = value2;
    }

    public boolean equals(Tuple2<A, B> tuple) {
	return value1.equals(tuple.value1) && value2.equals(tuple.value2);
    }
}
