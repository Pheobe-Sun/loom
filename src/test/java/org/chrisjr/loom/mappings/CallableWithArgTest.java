package org.chrisjr.loom.mappings;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.*;
import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.util.CallableNoop;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CallableWithArgTest {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	private class Incrementer implements Callable<Void> {
		final AtomicInteger[] counters;
		final int index;

		public Incrementer(final AtomicInteger[] counters, int index) {
			this.counters = counters;
			this.index = index;
		}

		@Override
		public Void call() throws Exception {
			counters[index].getAndIncrement();
			return null;
		}
	}

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		pattern = new Pattern(loom);

		loom.play();
	}

	@After
	public void tearDown() throws Exception {
		loom.dispose();
	}

	@Test
	public void onOnsetMultiple() {
		int n = 100;

		AtomicInteger[] counters = new AtomicInteger[n];
		Callable[] callables = new Callable[n];
		for (int i = 0; i < n; i++) {
			counters[i] = new AtomicInteger();
			callables[i] = new Incrementer(counters, i);
		}

		Integer[] values = new Integer[n * 2];

		for (int i = 0; i < n * 2; i++) {
			values[i] = i < n ? n - i : i - n + 1;
		}

		pattern.extend(values);

		pattern.onOnset(callables);

		for (int i = 0; i < n * 2; i++) {
			scheduler.setElapsedMillis((long) ((1000.0 / (n * 2)) * i));
			assertThat(counters[values[i] - 1].get(),
					is(equalTo(i < n ? 1 : 2)));
		}

	}
}
