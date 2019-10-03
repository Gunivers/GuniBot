package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

@SuppressWarnings({ "unchecked" })
public abstract class Tuple implements Serializable {

    protected static final long serialVersionUID = 9L;

    public static <A> Tuple1<A> newTuple(A value) {
	return new Tuple1<>(value);
    }

    public static <A, B> Tuple2<A, B> newTuple(A value1, B value2) {
	return new Tuple2<>(value1, value2);
    }

    public static <A, B, C> Tuple3<A, B, C> newTuple(A value1, B value2, C value3) {
	return new Tuple3<>(value1, value2, value3);
    }

    public static <A, B, C, D> Tuple4<A, B, C, D> newTuple(A value1, B value2, C value3, D value4) {
	return new Tuple4<>(value1, value2, value3, value4);
    }

    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> newTuple(A value1, B value2, C value3, D value4, E value5) {
	return new Tuple5<>(value1, value2, value3, value4, value5);
    }

    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> newTuple(A value1, B value2, C value3, D value4, E value5,
	    F value6) {
	return new Tuple6<>(value1, value2, value3, value4, value5, value6);
    }

    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> newTuple(A value1, B value2, C value3, D value4,
	    E value5, F value6, G value7) {
	return new Tuple7<>(value1, value2, value3, value4, value5, value6, value7);
    }

    public static <A> Tuple1<A> castTo(Tuple oldTuple, Tuple1<Class<? extends A>> pattern) {
	try {
	    Tuple1<Object> tuple = (Tuple1<Object>) oldTuple;
	    return new Tuple1<>(pattern.value1.cast(tuple.value1));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B> Tuple2<A, B> castTo(Tuple oldTuple, Tuple2<Class<? extends A>, Class<? extends B>> pattern) {
	try {
	    Tuple2<Object, Object> tuple = (Tuple2<Object, Object>) oldTuple;
	    return new Tuple2<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B, C> Tuple3<A, B, C> castTo(Tuple oldTuple,
	    Tuple3<Class<? extends A>, Class<? extends B>, Class<? extends C>> pattern) {
	try {
	    Tuple3<Object, Object, Object> tuple = (Tuple3<Object, Object, Object>) oldTuple;
	    return new Tuple3<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2),
		    pattern.value3.cast(tuple.value3));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B, C, D> Tuple4<A, B, C, D> castTo(Tuple oldTuple,
	    Tuple4<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>> pattern) {
	try {
	    Tuple4<Object, Object, Object, Object> tuple = (Tuple4<Object, Object, Object, Object>) oldTuple;
	    return new Tuple4<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2),
		    pattern.value3.cast(tuple.value3), pattern.value4.cast(tuple.value4));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B, C, D, E> Tuple5<A, B, C, D, E> castTo(Tuple oldTuple,
	    Tuple5<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>> pattern) {
	try {
	    Tuple5<Object, Object, Object, Object, Object> tuple = (Tuple5<Object, Object, Object, Object, Object>) oldTuple;
	    return new Tuple5<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2),
		    pattern.value3.cast(tuple.value3), pattern.value4.cast(tuple.value4),
		    pattern.value5.cast(tuple.value5));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> castTo(Tuple oldTuple,
	    Tuple6<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>, Class<? extends F>> pattern) {
	try {
	    Tuple6<Object, Object, Object, Object, Object, Object> tuple = (Tuple6<Object, Object, Object, Object, Object, Object>) oldTuple;
	    return new Tuple6<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2),
		    pattern.value3.cast(tuple.value3), pattern.value4.cast(tuple.value4),
		    pattern.value5.cast(tuple.value5), pattern.value6.cast(tuple.value6));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

    public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> castTo(Tuple oldTuple,
	    Tuple7<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>, Class<? extends F>, Class<? extends G>> pattern) {
	try {
	    Tuple7<Object, Object, Object, Object, Object, Object, Object> tuple = (Tuple7<Object, Object, Object, Object, Object, Object, Object>) oldTuple;
	    return new Tuple7<>(pattern.value1.cast(tuple.value1), pattern.value2.cast(tuple.value2),
		    pattern.value3.cast(tuple.value3), pattern.value4.cast(tuple.value4),
		    pattern.value5.cast(tuple.value5), pattern.value6.cast(tuple.value6),
		    pattern.value7.cast(tuple.value7));
	} catch (ClassCastException e) {
	    throw new ArrayStoreException("Cast could not be done.");
	}
    }

}
