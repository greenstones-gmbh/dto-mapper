package com.greenstones.dto.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.greenstones.dto.Department;
import com.greenstones.dto.Employee;
import com.greenstones.dto.old.Mapper;
import com.greenstones.dto.old.Reducer;

import lombok.AllArgsConstructor;

public class ReverseMapperExamples {

	public static class Mapper<I, O> {

		List<Reducer<I, O>> reducers = new ArrayList<Reducer<I, O>>();

		public void add(Reducer<I, O> reducer) {
			reducers.add(reducer);
		}

		public O map(I input, O output) {
			reducers.forEach(r -> {
				r.reduce(output, input);
			});
			return output;
		}

	}

	public static class Operations {

//		public static <I, O> void copy(Source<I> source, Target<O> target, Transform<I, O> transform) {
//
//		}
//
//		public static <E> void copy(Source<E> source, Target<E> target) {
//
//		}

		public static <I, O, PI, PO> Reducer<I, O> copy(Function<I, PI> source, BiConsumer<O, PO> target,
				Function<PI, PO> transform) {
			return copy(new CopyReducer<I, O, PI, PO>(source, target, transform));

		}

		public static <I, O, P> Reducer<I, O> copy(Function<I, P> source, BiConsumer<O, P> target) {
			return copy(new CopyReducer<I, O, P, P>(source, target, v -> v));

		}

		public static <I, O, PI, PO> Reducer<I, O> copy(CopyReducer<I, O, PI, PO> props) {
			// return copy(step.source, step.target, step.transform);
			return null;
		}

		public static <I, O> Reducer<I, O> copy(CopyReducer<I, O, ?, ?>... props) {
			// return copy(step.source, step.target, step.transform);
			Arrays.asList(props).forEach(p -> copy(p));
			return null;
		}

//		public static <I, O, P> Reducer<I, O>  copy(Function<I,P> source, BiConsumer<O,P> target, Function<P,P>  transform){
//			return null;
//
//		}

	}

	public static class Copy {

		public static <I, O> Function<I, O> from(String prop) {
			return null;
		}

		public static <O, P> BiConsumer<O, P> to(String prop) {
			return null;
		}

		public static <I, O> Reducer<I, O> prop(String prop) {
			return null;
		}

	}

	@AllArgsConstructor
	public static class CopyReducer<I, O, PI, PO> implements Reducer<I, O> {
		Function<I, PI> source;
		BiConsumer<O, PO> target;
		Function<PI, PO> transform;

		public void copy(I in, O out) {
			PI pi = source.apply(in);
			PO po = transform.apply(pi);
			target.accept(out, po);
		}

		@Override
		public O reduce(O out, I in) {
			copy(in, out);
			return out;
		}

	}

	public static interface Source<I> {

	}

	public static interface Target<O> {

	}

	public static interface Transform<I, O> {

	}

	public static void main(String[] args) {

		Reducer<Employee, Department> r = Operations.props(e -> e.props(), populate, t -> props);

		Mapper<Employee, Department> m = new Mapper<Employee, Department>();
		m.add(Operations.props(e -> e.props(), populate, v -> props + "1"));
		m.add(Operations.props(Copy.from("username"), Copy.to("name")));
		m.add(Copy.prop("username"));

		Reducer<Employee, Department> p1 = Copy.prop("a");
		Reducer<Employee, Department> p2 = Copy.prop("b");
		
		Operations.props().from("a").to("b").;

	}
}
