
Plast : FilterPattern {
	var <>repeat;
	*new { arg pattern, repeat=inf;
		^super.new(pattern).repeat_(repeat)
	}
	storeArgs { ^[pattern, repeat] }
	embedInStream { arg event;
		var inevent, nn;

		var stream = pattern.asStream;
		var lastevent;

		loop {

			inevent = stream.next(event);
			if(inevent.notNil) {
				event = inevent.copy.yield;
				lastevent = inevent;
			} {
				repeat.do {
					event.debug("Plast:event");
					event = lastevent;
					event.yield;
				};
				^event;
			};
		}
		^event;
	}
}
