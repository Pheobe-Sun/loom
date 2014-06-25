package org.chrisjr.loom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.chrisjr.loom.time.NonRealTimeScheduler;
import org.chrisjr.loom.transforms.Transforms;
import org.chrisjr.loom.util.CallableOnChange;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PatternTransformations {

	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private Pattern pattern;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		scheduler.play();

		pattern = new Pattern(loom);
		pattern.extend("0123");
		pattern.asInt(0, 3);
	}

	@After
	public void tearDown() throws Exception {
		scheduler = null;
		loom = null;
		pattern = null;
	}

	@Test
	public void halfSpeed() {
		pattern.speed(0.5);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((500 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	@Test
	public void doubleSpeed() {
		pattern.speed(2);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((125 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	@Test
	public void shiftRight() {
		pattern.loop();
		pattern.shift(0.25);

		for (int i = 0; i < 8; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((i + 1) % 4)));
		}
	}

	@Test
	public void shiftRightEveryCycle() {
		pattern.loop();
		pattern.every(1, new Transforms.Shift(1, 4));

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				int time = (j * 1000) + (250 * i);
				scheduler.setElapsedMillis(time);
				int expected = (i + j) % 4;
				assertThat(pattern.asInt(), is(equalTo(expected)));
			}
		}
	}

	@Test
	public void shiftLeftEveryCycle() {
		pattern.loop();
		pattern.every(1, new Transforms.Shift(-1, 4));

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				int time = (j * 1000) + (250 * i);
				scheduler.setElapsedMillis(time);
				int expected = (i + (4 - j)) % 4;
				assertThat(pattern.asInt(), is(equalTo(expected)));
			}
		}
	}

	@Test
	public void shiftRightEveryHalfCycle() {
		pattern.loop();
		pattern.every(0.5, new Transforms.Shift(1, 4));

		for (int i = 0; i < 16; i++) {
			int time = i * 250;
			scheduler.setElapsedMillis(time);
			int j = i / 2;
			int expected = (i + j) % 4;
			assertThat(pattern.asInt(), is(equalTo(expected)));
		}
	}

	@Test
	public void shiftRightEveryOtherCycle() {
		pattern.loop();
		pattern.every(2, new Transforms.Shift(1, 4));

		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < 4; i++) {
				int time = (j * 1000) + (250 * i);
				scheduler.setElapsedMillis(time);
				int expected = (i + (j / 2)) % 4;
				assertThat(pattern.asInt(), is(equalTo(expected)));
			}
		}
	}

	@Test
	public void shiftLeft() {
		pattern.loop();
		pattern.shift(-0.25);

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((i + 3) % 4)));
		}
	}

	@Test
	public void reverse() {
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo((3 - i) % 4)));
		}
	}

	@Test
	public void reverseTwiceIsUnchanged() {
		pattern.reverse();
		pattern.reverse();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(i)));
		}
	}

	public void checkIfReversing(int beatLength, int beatsTillReverse) {
		for (int i = 0; i < 32; i++) {
			long time = (beatLength * i) + 1;
			boolean reversed = (i / beatsTillReverse) % 2 == 1;
			scheduler.setElapsedMillis(time);
			int expecting = reversed ? (3 - (i % 4)) % 4 : (i % 4);
			assertThat(pattern.asInt(), is(equalTo(expecting)));
		}
	}

	@Test
	public void reverseEveryCycle() {
		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 4);
	}

	@Test
	public void reverseEveryHalfCycle() {
		pattern.every(0.5, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 2);
	}

	@Test
	public void reverseEveryOtherCycle() {
		pattern.every(2, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(250, 8);
	}

	@Test
	public void speedUpAndReverse() {
		pattern.speed(5);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(50, 4);
	}

	@Test
	public void slowAndReverse() {
		pattern.speed(0.5);

		pattern.every(1, new Transforms.Reverse());

		pattern.loop();

		checkIfReversing(500, 4);
	}

	@Test
	public void reverseThenSlow() {
		pattern.every(1, new Transforms.Reverse());
		pattern.speed(0.1);
		pattern.loop();

		checkIfReversing(2500, 4);
	}

	@Test
	public void invert() {
		pattern.invert();

		for (int i = 0; i < 4; i++) {
			scheduler.setElapsedMillis((250 * i) + 1);
			assertThat(pattern.asInt(), is(equalTo(4 - (i + 1))));
		}
	}

	@Test
	public void onOnset() {
		pattern.clear();
		pattern.extend("1101");

		final AtomicInteger counter = new AtomicInteger();
		pattern.onOnset(new Callable<Void>() {
			@Override
			public Void call() {
				counter.incrementAndGet();
				return null;
			}
		});

		scheduler.setElapsedMillis(1001);
		assertThat(counter.get(), is(equalTo(4)));
	}

	@Test
	public void onRelease() {
		pattern.clear();
		pattern.extend("1101");

		final AtomicInteger counter = new AtomicInteger();
		pattern.onRelease(new Callable<Void>() {
			@Override
			public Void call() {
				counter.incrementAndGet();
				return null;
			}
		});

		scheduler.setElapsedMillis(1001);
		assertThat(counter.get(), is(equalTo(4)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void noteOnAndOff() {
		final AtomicInteger noteOffs = new AtomicInteger();
		final AtomicInteger noteOns = new AtomicInteger();

		Callable<Void> noteOffInc = new Callable<Void>() {
			@Override
			public Void call() {
				noteOffs.getAndIncrement();
				return null;
			}
		};

		Callable<Void> noteOnInc = new Callable<Void>() {
			@Override
			public Void call() {
				noteOns.getAndIncrement();
				return null;
			}
		};

		pattern.extend("1111");

		ConcretePattern trigger = ConcretePattern.forEach(pattern
				.getConcretePattern());
		trigger.asStatefulCallable(CallableOnChange.fromCallables(noteOffInc,
				noteOnInc));
		pattern.addChild(trigger);

		scheduler.setElapsedMillis(501);
		assertThat(noteOffs.get(), is(equalTo(2)));
		assertThat(noteOns.get(), is(equalTo(3)));
		scheduler.setElapsedMillis(1000);
		assertThat(noteOffs.get(), is(equalTo(4)));
		assertThat(noteOns.get(), is(equalTo(4)));

	}
}
