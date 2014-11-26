VecoLib {
	classvar <lib;

	*load_lib {
		lib = Environment.new;
		lib.use({
			debug("loading midi.scd");
			(Platform.userExtensionDir +/+ "seco/seco/vecolib/midi.scd").standardizePath.load;
			debug("loading preset.scd");
			(Platform.userExtensionDir +/+ "seco/seco/vecolib/preset.scd").standardizePath.load;
			debug("loading rack.scd");
			(Platform.userExtensionDir +/+ "seco/seco/vecolib/rack.scd").standardizePath.load;
		})
	}

	*initClass {
		//Class.initClassTree(ControlSpec);
		//this.load_lib;
	}
}

MIDIController {
	*new { arg ... args;
		^VecoLib.lib[\class_midi_cc_controller].new(*args)
	}
}

MIDIBoard {
	classvar <controls;
	classvar <>permanent;

	*initClass {
		//root = "~/Musique/sc/samplekit".standardizePath;
		controls = IdentityDictionary.new;
		permanent = false;
	}

	*define { arg channel, defs;
		var source_uid = nil;
		if(channel.isSequenceableCollection) {
			source_uid = channel[1];
			channel = channel[0]
		};
		defs.pairsDo { arg key, val;
			var kind=\cc, keychannel;
			if(val.class == Association) {
				kind = val.key;
				val = val.value;
			};
			if(val.isSequenceableCollection) {
				keychannel = val[1];
				val = val[0]
			} {
				keychannel = channel;
			};
			key.debug("kkKKey");
			val.debug("kkKKeyVVVVVVVVVVVVV");
			kind.debug("kkKKeykinddddddddddd");
			controls[key].changed(\free_map);
			controls[key] = MIDIController.new(val, key, keychannel, kind);
			controls[key].source_uid = source_uid;
			controls[key].install_midi_responder;
			//controls[key].debug("YYYYYYYYYYYYYYYY");
		
		};
	}

	*map { arg ... args;
		if(args[0].isSequenceableCollection) {
			args[0].do { arg val;
				var key;
				val = val.copy;
				//val.debug("val");
				key = val.removeAt(0);
				//key.debug("array key");
				//val.debug("val2");
				if(controls[key].isNil) {
					("MIDIBoard: Error: no control with this name:" ++ key).postln;
				} {
					controls[key].map(*val);
				}
			}
		} {
			var key;
			//args.debug("val");
			args = args.copy;
			key = args.removeAt(0);
			key.debug("key");
			//args.debug("val2");
			if(controls[key].isNil) {
				("MIDIBoard: Error: no control with this name:" ++ key).postln;
			} {
				controls[key].map(*args);
			}
		}
	}

	*unmap { arg ... args;
		var key;
		key = args.removeAt(0);
		controls[key].unmap(*args);
	}
}

Rack {
	*new { arg ... args;
		^VecoLib.lib[\class_rack].new(\noname,\noname, *args);
	}

	*newFrom { arg ... args;
		^VecoLib.lib[\class_rack].new_from_object(*args);
	}
}

PresetDef {
	classvar <>all;

	*initClass {
		all = IdentityDictionary.new;
	}

	*new { arg key ... args;
		var res;

		if(all[key].notNil) {
			res = all[key];
		} {
			res = VecoLib.lib[\class_preset].new(*args);
			res.node_uname = key;
			res.name = key;
			all[key] = res;
		};
		^res;

	}

	*force { arg key ... args;
		var res;
		res = VecoLib.lib[\class_preset].new(*args);
		res.node_uname = key;
		res.name = key;
		all[key] = res;
		^res;
		
	}
	
}

+ Pdef {
	setBusMode { arg ... argnames;
		var exclude = Set[\legato, \sustain, \dur, \stretch];
		(argnames.asSet.difference(exclude)).do { arg argname;
			var bus;
			var oldval;
			oldval = this.get(argname);
			if(oldval.class == Float) {
				BusDef(\pdef+++ this.key +++argname, \control);
				bus = BusDef(\pdef+++ this.key +++argname);
				bus.set(oldval);
				this.set(argname, bus.asMap)
			}
		}
	}

	asRack {
		^Rack.newFrom(this)
	}
	
}
