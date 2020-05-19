package com.greenstones.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AbstractMapper<I, O> {

	private List<Reducer<I, O>> reducers = new ArrayList<Reducer<I, O>>();

	Supplier<O> instanceFactory;

	public AbstractMapper(Supplier<O> instanceFactory) {
		super();
		this.instanceFactory = instanceFactory;
	}

	public AbstractMapper(Class<O> type) {
		this(() -> {
			try {
				return type.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public O map(I in) {
		O out = instanceFactory.get();
		populate(in, out);
		return out;
	}

	protected void populate(I e, O o) {
		reducers.forEach(r -> {
			r.reduce(o, e);
		});
	}

	public AbstractMapper<I, O> add(Reducer<I, O> reducer) {
		reducers.add(reducer);
		return this;
	}

}
