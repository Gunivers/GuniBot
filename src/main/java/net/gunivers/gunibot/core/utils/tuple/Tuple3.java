package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple3<A, B, C> extends Tuple implements Serializable {

    private static final long serialVersionUID = -7669164929093003736L;

    public final A value1;
    public final B value2;
    public final C value3;

    public Tuple3(A value1, B value2, C value3) {
	this.value1 = value1;
	this.value2 = value2;
	this.value3 = value3;
    }

    public boolean equals(Tuple3<A, B, C> tuple) {
	return value1.equals(tuple.value1) && value2.equals(tuple.value2) && value3.equals(tuple.value3);
    }
}
