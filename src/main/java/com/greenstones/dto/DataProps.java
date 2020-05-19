package com.greenstones.dto;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.BeanWrapperImpl;

public class DataProps {

	public static <E> Reducer<Data, E> add(String prop, Function<Data, Object> transform) {

		return (e, data) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			bw.setPropertyValue(prop, transform.apply(data));
			return e;
		};
	}

	public static <E, T> Reducer<Data, E> copy(String prop, Function<Object, T> transform) {
		return (e, data) -> {
			Object object = data.get(prop);
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			bw.setPropertyValue(prop, transform.apply(object));
			return e;
		};
	}

	public static <E, T> Reducer<Data, E> copy1(String prop, DataMapper<T> builder) {
		Function<Object, T> transform = obj -> {
			return builder.map((Data) obj);
		};
		return copy(prop, transform);

	}

	public static <E, T> Reducer<Data, E> copy(String prop, DataMapper<T> builder) {
		return (e, data) -> {
			Object object = data.get(prop);
			BeanWrapperImpl bw = new BeanWrapperImpl(e);

			if (object instanceof Iterable) {
				@SuppressWarnings("unchecked")
				Iterable<Data> iterable = (Iterable<Data>) object;
				bw
						.setPropertyValue(prop,
								StreamSupport
										.stream(iterable.spliterator(), false)
											.map(builder::map)
											.collect(Collectors.toList()));

			} else {
				Data v = (Data) object;
				bw.setPropertyValue(prop, builder.map(v));
			}

			return e;
		};
	}

	public static <E> Reducer<Data, E> copy(String... props) {
		return (e, data) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			Arrays.asList(props).forEach(prop -> {
				bw.setPropertyValue(prop, data.get(prop));
			});
			return e;
		};
	}

	public static <E> Reducer<Data, E> copyAll() {
		return (e, data) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);

			Arrays
					.asList(bw.getPropertyDescriptors())
						.stream()
						.map(pd -> pd.getName())
						.filter(prop -> data.getProps().keySet().contains(prop))
						.forEach(prop -> {
							Object value = data.get(prop);

							if (bw.getPropertyDescriptor(prop).getPropertyType().isInstance(value)) {
								bw.setPropertyValue(prop, value);
							}

						});

			return e;
		};
	}

	public static <E> Reducer<Data, E> exclude(String... props) {
		return (e, data) -> {
			BeanWrapperImpl bw = new BeanWrapperImpl(e);
			Arrays.asList(props).forEach(prop -> {
				bw.setPropertyValue(prop, null);
			});

			return e;
		};
	}

}
