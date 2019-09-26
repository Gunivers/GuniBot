package net.gunivers.gunibot.core.utils.tuple;

import java.io.Serializable;

@SuppressWarnings({ "unchecked" })
public abstract class Tuple implements Serializable {

	protected static final long serialVersionUID = 9L;
	
	public static <A> Tuple1<A> newTuple(A value) {
		return new Tuple1<A>(value);
	}

	public static <A, B> Tuple2<A, B> newTuple(A value1, B value2) {
		return new Tuple2<A, B>(value1, value2);
	}
	
	public static <A, B, C> Tuple3<A, B, C> newTuple(A value1, B value2, C value3) {
		return new Tuple3<A, B, C>(value1, value2, value3);
	}
	
	public static <A, B, C, D> Tuple4<A, B, C, D> newTuple(A value1, B value2, C value3, D value4) {
		return new Tuple4<A, B, C, D>(value1, value2, value3, value4);
	}
	
	public static <A, B, C, D, E> Tuple5<A, B, C, D, E> newTuple(A value1, B value2, C value3, D value4, E value5) {
		return new Tuple5<A, B, C, D, E>(value1, value2, value3, value4, value5);
	}
	
	public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> newTuple(A value1, B value2, C value3, D value4, E value5, F value6) {
		return new Tuple6<A, B, C, D, E, F>(value1, value2, value3, value4, value5, value6);
	}
	
	public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> newTuple(A value1, B value2, C value3, D value4, E value5, F value6, G value7) {
		return new Tuple7<A, B, C, D, E, F, G>(value1, value2, value3, value4, value5, value6, value7);
	}
	
	public static <A> Tuple1<A> castTo(Tuple t, Tuple1<Class<? extends A>> pattern) {
		try {
			Tuple1<Object> tuple = (Tuple1<Object>) t;
			return new Tuple1<A>(pattern._1.cast(tuple._1));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
	
	public static <A, B> Tuple2<A, B> castTo(Tuple t, Tuple2<Class<? extends A>, Class<? extends B>> pattern) {
		try {
			Tuple2<Object, Object> tuple = (Tuple2<Object, Object>) t;
			return new Tuple2<A, B>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
	
	public static <A, B, C> Tuple3<A, B, C> castTo(Tuple t, Tuple3<Class<? extends A>, Class<? extends B>, Class<? extends C>> pattern) {
		try {
			Tuple3<Object, Object, Object> tuple = (Tuple3<Object, Object, Object>) t;
			return new Tuple3<A, B, C>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2), pattern._3.cast(tuple._3));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
	
	public static <A, B, C, D> Tuple4<A, B, C, D> castTo(Tuple t, Tuple4<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>> pattern) {
		try {
			Tuple4<Object, Object, Object, Object> tuple = (Tuple4<Object, Object, Object, Object>) t;
			return new Tuple4<A, B, C, D>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2), pattern._3.cast(tuple._3), pattern._4.cast(tuple._4));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
	
	public static <A, B, C, D, E> Tuple5<A, B, C, D, E> castTo(Tuple t, Tuple5<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>> pattern) {
		try {
			Tuple5<Object, Object, Object, Object, Object> tuple = (Tuple5<Object, Object, Object, Object, Object>) t;
			return new Tuple5<A, B, C, D, E>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2), pattern._3.cast(tuple._3), pattern._4.cast(tuple._4), pattern._5.cast(tuple._5));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
	
	public static <A, B, C, D, E, F> Tuple6<A, B, C, D, E, F> castTo(Tuple t, Tuple6<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>, Class<? extends F>> pattern) {
		try {
			Tuple6<Object, Object, Object, Object, Object, Object> tuple = (Tuple6<Object, Object, Object, Object, Object, Object>) t;
			return new Tuple6<A, B, C, D, E, F>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2), pattern._3.cast(tuple._3), pattern._4.cast(tuple._4), pattern._5.cast(tuple._5), pattern._6.cast(tuple._6));
		} catch(ClassCastException e) {
			 throw new ArrayStoreException("Cast could not be done.");
		}
	}
		
		public static <A, B, C, D, E, F, G> Tuple7<A, B, C, D, E, F, G> castTo(Tuple t, Tuple7<Class<? extends A>, Class<? extends B>, Class<? extends C>, Class<? extends D>, Class<? extends E>, Class<? extends F>, Class<? extends G>> pattern) {
			try {
				Tuple7<Object, Object, Object, Object, Object, Object, Object> tuple = (Tuple7<Object, Object, Object, Object, Object, Object, Object>) t;
				return new Tuple7<A, B, C, D, E, F, G>(pattern._1.cast(tuple._1), pattern._2.cast(tuple._2), pattern._3.cast(tuple._3), pattern._4.cast(tuple._4), pattern._5.cast(tuple._5), pattern._6.cast(tuple._6), pattern._7.cast(tuple._7));
			} catch(ClassCastException e) {
				 throw new ArrayStoreException("Cast could not be done.");
			}
	}
	
}
