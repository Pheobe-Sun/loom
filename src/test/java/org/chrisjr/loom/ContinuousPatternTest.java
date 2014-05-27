package org.chrisjr.loom;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.chrisjr.loom.continuous.*;
import org.chrisjr.loom.time.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContinuousPatternTest {
	private Loom loom;
	private NonRealTimeScheduler scheduler;
	private ContinuousPattern pattern;

	@Before
	public void setUp() throws Exception {
		scheduler = new NonRealTimeScheduler();
		loom = new Loom(null, scheduler);
		scheduler.play();
	}

	@After
	public void tearDown() throws Exception {
		scheduler = null;
		loom = null;
	}

	@Test
	public void sinePattern() {
		pattern = new ContinuousPattern(loom, new SineFunction());
		
		final double epsilon = 1e-4;
		
		scheduler.setElapsedMillis(250);
		assertThat(pattern.getValue(), is(closeTo(1.0, epsilon)));
		scheduler.setElapsedMillis(500);
		assertThat(pattern.getValue(), is(closeTo(0.5, epsilon)));
		scheduler.setElapsedMillis(750);
		assertThat(pattern.getValue(), is(closeTo(0.0, epsilon)));
		scheduler.setElapsedMillis(1000);
		assertThat(pattern.getValue(), is(closeTo(0.5, epsilon)));
	}

	@Test
	public void canBeCloned() throws CloneNotSupportedException {
		pattern = new ContinuousPattern(loom, new SineFunction());
		
		ContinuousPattern pattern2 = pattern.clone();
		
		//TODO this test makes no sense until patterns can be modified.
		
		final double epsilon = 1e-3;
		
		scheduler.setElapsedMillis(250);
		assertThat(pattern.getValue(), is(closeTo(1.0, epsilon)));

		assertThat(pattern2.getValue(), is(closeTo(1.0, epsilon)));

	}
}
