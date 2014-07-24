package org.chrisjr.loom.util;

import java.util.Iterator;

import org.apache.commons.math3.fraction.BigFraction;
import org.chrisjr.loom.*;
import org.chrisjr.loom.time.Interval;

import abc.notation.*;
import abc.parser.TuneParser;

public class AbcTools {
	public static final TuneParser parser = new TuneParser();

	public static Pattern fromString(Loom loom, String tuneString) {
		EventCollection tuneEvents = eventsFromString(tuneString);
		return new Pattern(loom, tuneEvents);
	}

	public static EventCollection eventsFromString(String tuneString) {
		EventCollection events = new EventCollection();

		Tune tune = null;
		try {
			tune = parser.parse(tuneString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (tune != null) {
			BigFraction start = new BigFraction(0);
			Iterator<MusicElement> it = tune.getMusic().iterator();

			KeySignature sig = null;

			while (it.hasNext()) {
				MusicElement elem = it.next();
				if (elem instanceof KeySignature) {
					sig = (KeySignature) elem;
				} else if (elem instanceof Note) {
					Note note = (Note) elem;

					byte whitekey = note.getStrictHeight();
					short dur = note.getDuration();
					BigFraction duration = new BigFraction(dur, 96);

					if (whitekey != Note.REST) {
						// dealing with actual note

						byte accidental = AccidentalType.NONE;
						if (note.hasAccidental()) {
							accidental = note.getAccidental();
						} else {
							if (sig != null) {
								accidental = sig.getAccidentalFor(whitekey);
							}
						}
						byte octave = note.getOctaveTransposition();

						int octaveStart = 60 + (12 * octave);

						int midinote = octaveStart + whitekey;

						switch (accidental) {
						case AccidentalType.SHARP:
							midinote++;
							break;
						case AccidentalType.FLAT:
							midinote--;
							break;
						case AccidentalType.NONE:
						case AccidentalType.NATURAL:
						default:
							break;
						}
						Interval interval = new Interval(start,
								start.add(duration));
						events.add(new Event(interval, midinote / 127.0));
					}

					start = start.add(duration);

				}
			}
		}

		return events;
	}
}