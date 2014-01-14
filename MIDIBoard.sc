VecoLib {
	classvar <lib;

	*load_lib {
		lib = Environment.new;
		lib.use({
			(Platform.userExtensionDir +/+ "seco/seco/vecolib/midi.scd").standardizePath.load;
			(Platform.userExtensionDir +/+ "seco/seco/vecolib/preset.scd").standardizePath.load;
		})
	}

	*initClass {
		this.load_lib;
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
			controls[key] = MIDIController.new(val, key, keychannel, kind);
			controls[key].debug("YYYYYYYYYYYYYYYY");
		
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
	
}
