package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple5<A, B, C, D, E> extends Tuple implements Serializable {

    private static final long serialVersionUID = -1376976129640882320L;

    public final A value1;
    public final B value2;
    public final C value3;
    public final D value4;
    public final E value5;

    public Tuple5(A value1, B value2, C value3, D value4, E value5) {
	this.value1 = value1;
	this.value2 = value2;
	this.value3 = value3;
	this.value4 = value4;
	this.value5 = value5;
    }

    public boolean equals(Tuple5<A, B, C, D, E> tuple) {
	return value1.equals(tuple.value1) && value2.equals(tuple.value2) && value3.equals(tuple.value3)
		&& value4.equals(tuple.value4) && value5.equals(tuple.value5);
    }
}
