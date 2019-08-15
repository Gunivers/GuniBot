package net.gunivers.gunibot.core.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtils {

	public static <T, U, V> List<V> zip(List<T> list1, List<U> list2, BiFunction<T, U, V> function) {
		if(list1.size() != list2.size())
			throw new IllegalArgumentException("Streams size are not equal");
		else {
			List<V> result = new LinkedList<>();
			for(int i = 0; i < list1.size(); i++)
				result.add(function.apply(list1.get(i), list2.get(i)));
			return result;
		}
	}
	
	public static <T, U> List<U> mapWithIndex(List<T> list, BiFunction<T, Integer, U> function) {
		return zip(list, IntStream.range(0, (int)list.size()).boxed().collect(Collectors.toList()), function);
	}

}
