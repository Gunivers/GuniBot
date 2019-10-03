package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple4<A, B, C, D> extends Tuple implements Serializable {

    private static final long serialVersionUID = 8785115253519423457L;

    public final A value1;
    public final B value2;
    public final C value3;
    public final D value4;

    public Tuple4(A value1, B value2, C value3, D value4) {
	this.value1 = value1;
	this.value2 = value2;
	this.value3 = value3;
	this.value4 = value4;
    }

    public boolean equals(Tuple4<A, B, C, D> tuple) {
	return value1.equals(tuple.value1) && value2.equals(tuple.value2) && value3.equals(tuple.value3)
		&& value4.equals(tuple.value4);
    }
}
