package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

public class Tuple7<A, B, C, D, E, F, G> extends Tuple implements Serializable {

    private static final long serialVersionUID = -4913311265403889120L;

    public final A value1;
    public final B value2;
    public final C value3;
    public final D value4;
    public final E value5;
    public final F value6;
    public final G value7;

    Tuple7(A value1, B value2, C value3, D value4, E value5, F value6, G value7) {
	this.value1 = value1;
	this.value2 = value2;
	this.value3 = value3;
	this.value4 = value4;
	this.value5 = value5;
	this.value6 = value6;
	this.value7 = value7;
    }

    public boolean equals(Tuple7<A, B, C, D, E, F, G> tuple) {
	return value1.equals(tuple.value1) && value2.equals(tuple.value2) && value3.equals(tuple.value3)
		&& value4.equals(tuple.value4) && value5.equals(tuple.value5) && value6.equals(tuple.value6)
		&& value7.equals(tuple.value7);
    }
}
