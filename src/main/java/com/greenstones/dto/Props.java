package com.greenstones.dto;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.BeanWrapperImpl;

public class Props {

	public static <E> Reducer<E, Data> copy(String... props) {
		return (data, e) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			Arrays.asList(props).forEach(prop -> {
				data.put(prop, bw.getPropertyValue(prop));
			});
			return data;
		};
	}

	public static <E> Reducer<E, Data> copyTo(String prop, String toProp) {
		return (data, e) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			Object v = bw.getPropertyValue(prop);
			data.put(toProp, v);
			return data;
		};
	}

	public static <E> Reducer<E, Data> copyAll() {
		return (data, e) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			Arrays
					.asList(bw.getPropertyDescriptors())
						.stream()
						.map(pd -> pd.getName())
						.filter(prop -> !"class".equals(prop))
						.forEach(prop -> {
							data.put(prop, bw.getPropertyValue(prop));
						});

			return data;
		};
	}

	public static <E> Reducer<E, Data> exclude(String... props) {
		return (data, e) -> {
			Arrays.asList(props).forEach(prop -> {
				data.remove(prop);
			});

			return data;
		};
	}

	public static <E> Reducer<E, Data> add(String name, Function<E, Object> transform) {
		return (data, e) -> {
			data.put(name, transform.apply(e));
			return data;
		};
	}

	public static <E, T> Reducer<E, Data> copy(String prop, Function<T, Object> transform) {
		return (data, e) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			@SuppressWarnings("unchecked")
			T v = (T) bw.getPropertyValue(prop);
			data.put(prop, transform.apply(v));
			return data;
		};
	}

	public static <E, T> Reducer<E, Data> copy(String prop, Mapper<T> builder) {
		return (data, e) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);

			Object ov = bw.getPropertyValue(prop);
			if (ov instanceof Iterable) {
				@SuppressWarnings("unchecked")
				Iterable<T> iterable = (Iterable<T>) ov;
				data
						.put(prop,
								StreamSupport
										.stream(iterable.spliterator(), false)
											.map(builder::map)
											.collect(Collectors.toList()));

			} else {
				@SuppressWarnings("unchecked")
				T v = (T) ov;
				data.put(prop, builder.map(v));
			}
			return data;
		};
	}

}
