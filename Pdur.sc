
Pdur : FilterPattern {
	// same as Pfindur but bugged, why I wrote this ????

	var <>dur, <>tolerance;
	*new { arg dur, pattern, tolerance = 0.001;
		^super.new(pattern).dur_(dur).tolerance_(tolerance)
	}
	storeArgs { ^[dur,pattern,tolerance] }
	asStream { | cleanup| ^Routine({ arg inval; this.embedInStream(inval, cleanup) }) }

	embedInStream { arg event, cleanup;
		var item, delta, elapsed = 0.0, nextElapsed, inevent,
			localdur = dur.value(event);
		var stream = pattern.asStream;

		cleanup ?? { cleanup = EventStreamCleanup.new };
		loop {
			inevent = stream.next(event).asEvent ?? { ^event };
			cleanup.update(inevent);
			delta = inevent.delta;
			nextElapsed = elapsed + delta;
			if (nextElapsed.roundUp(tolerance) >= localdur) {
				// must always copy an event before altering it.
				// fix delta time and yield to play the event.
				//inevent = inevent.copy.put(\delta, localdur - elapsed).yield;
				^cleanup.exit(inevent);
			};

			elapsed = nextElapsed;
			event = inevent.yield;

		}
	}
}
