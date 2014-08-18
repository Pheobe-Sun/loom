package org.chrisjr.loom;

import java.util.ArrayList;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.time.Interval;
import org.chrisjr.loom.time.IntervalMath;
import org.chrisjr.loom.util.MidiTools.Note;

/**
 * An Event has a start, an end, and a value. The start and end are expressed as
 * fractions of a cycle, and the value is constant during that time.
 * 
 * (For "events" that vary over time, use a ContinuousPattern.)
 * 
 * @author chrisjr
 */
public class Event {
	final private Interval interval;
	final private double value;
	final private Event parentEvent;

	public Event(Interval interval, double value) {
		this(interval, value, null);
	}

	public Event(Interval interval, double value, Event parentEvent) {
		this.interval = interval;
		this.value = value;
		this.parentEvent = parentEvent;
	}

	/**
	 * Gets the interval of the event.
	 * 
	 * @return the interval during which this event is active
	 */
	public Interval getInterval() {
		return interval;
	}

	/**
	 * Gets the value of the event.
	 * 
	 * @return the value of the event, a number between 0.0 and 1.0 inclusive
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Gets the event that "spawned" this event, e.g. in case this event has
	 * been split by an EventRewriter or an EventBoundaryProxy.
	 * 
	 * @return the parent event
	 */
	public Event getParentEvent() {
		return parentEvent;
	}

	/**
	 * Returns true if the event's start is less than or equal to the query
	 * interval's end, and the event's end is after the query interval starts.
	 * 
	 * For example: if the event goes from 0 to 1 and the query interval is 0.99
	 * to 1.0, containedBy will return true. If the query interval is instead
	 * 1.0 to 1.01, it will return false.
	 * 
	 * @param queryInterval
	 * @return whether the event falls within the interval
	 */
	public boolean containedBy(Interval queryInterval) {
		BigFraction queryStart = queryInterval.getStart();
		BigFraction queryEnd = queryInterval.getEnd();

		BigFraction start = interval.getStart();
		BigFraction end = interval.getEnd();

		boolean startsBeforeOrAtQueryEnd = start.compareTo(queryEnd) <= 0;
		boolean endsAfterQueryStart = end.compareTo(queryStart) > 0;

		return startsBeforeOrAtQueryEnd && endsAfterQueryStart;
	}

	public static Event note(double duration, Note note) {
		return evt(duration, ((double) note.ordinal()) / 127);
	}

	public static Event rest(double duration) {
		return evt(duration, 0.0);
	}

	public static Event rest(BigFraction duration) {
		return evt(duration, 0.0);
	}

	public static Event evt(double duration, double value) {
		return evt(IntervalMath.toFraction(duration), value);
	}

	public static Event evt(BigFraction duration, double value) {
		return new Event(Interval.zeroTo(duration), value);
	}

	/**
	 * Sequences events one after another. For example, given two events that
	 * last from 0 to 1, the output will have one event from 0 to 1 and another
	 * from 1 to 2. Events with a value of 0.0 (i.e. rests) will be ignored.
	 * 
	 * @param events
	 *            the original events, each with a time interval of 0 to
	 *            `duration`
	 * @return the sequenced events
	 */
	public static Event[] seq(Event... events) {
		ArrayList<Event> sequenced = new ArrayList<Event>();
		// Event[] sequenced = new Event[events.length];
		BigFraction offset = BigFraction.ZERO;
		for (int i = 0; i < events.length; i++) {
			Event oldEvent = events[i];
			Interval duration = oldEvent.getInterval();

			if (oldEvent.getValue() != 0.0) {
				sequenced.add(new Event(duration.add(offset), oldEvent
						.getValue()));
			}
			offset = offset.add(duration.getSize());
		}
		return sequenced.toArray(new Event[] {});
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		if (interval == null) {
			if (other.interval != null)
				return false;
		} else if (!interval.equals(other.interval))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Event(" + interval.toString() + " == " + String.valueOf(value)
				+ ")";
	}
}
