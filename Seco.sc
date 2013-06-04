Seco {
	classvar <>main;
	*init { arg initfun;
		if(initfun.isNil) {
			"~/code/sc/seco/main.sc".standardizePath.load;
			main = ~mk_sequencer.value;
		} {
			main = initfun.();
		};
		^main;
	}

}

Mpattern {
	classvar <>main;
	*force_init { arg close_previous = false;
		if(close_previous) {
			if(main.notNil) {
				main.destructor;
				main.close_side_gui;
			}
		};

		main = Seco.init;
		^main;
	}

	*init { 
		"init mpattern".debug;
		if(main.isNil) {
			main = Seco.init;
		};
		//main.debug("mpattern: main");
		^main;
	}
}

Mdef : Mpattern {
	*new { arg name, pat, instr=nil;
		this.init;
		if(main.isNil) {
			"Seco not initialized!!".debug;
			if(pat.isNil) {
				^Pdef(name);
			} {
				^Pdef(name, pat);
			}
		} {
			^main.node_manager.mpdef(name, pat, instr);
		}
	}

	*samplekit { arg name, list;
		this.init;
		if(main.isNil) {
			"Seco not initialized!!".debug;
		} {
			^main.samplekit_manager.add_samplekit(name, list);
		}
	}

	*sampledict { arg dict, prefix=nil;
		^main.samplekit_manager.add_to_sampledict(dict, prefix);
	}

	*env { arg name, pat;
		this.init;
		if(main.isNil) {
			"Seco not initialized!!".debug;
		} {
			^main.node_manager.mdefenv(name, pat);
		}
		
	}

	*sample { arg samplekit=\default, slot=0, channels=\stereo;
		^main.samplekit_manager.slot_to_bufnum(slot, samplekit, channels);
	}

	*dsample { arg name;
		^main.samplekit_manager.buffer_from_sampledict(name);
	}

	*dmsample { arg name;
		^main.samplekit_manager.mono_buffer_from_sampledict(name);
	}

	*psample { arg name, kind=\note;
		
	}

	*node { arg name;
		this.init;
		^main.get_node(name);
	}

	*node_by_index { arg idx;
		^main.panels.side.get_current_group.get_view_children[idx];
	}

	*show { arg name;
		^main.panels.side.set_current_group(main.get_node(name));
	}

	*showdef { arg name;
		main.node_manager.set_default_group(name);
		^main.panels.side.set_current_group(main.get_node(name));
	}

	*sampler { arg name, samples;
		^main.node_manager.mdefsampler(name, samples);
	}

	*scoreset { arg name;
		^main.node_manager.mdef_scoreset(name);
	}

	*scorepat { arg name;
		^main.node_manager.mdef_scorepat(name);
	}

	*side_gui {
		this.init;
		main.make_side_gui;
	}

}

Mgroup : Mpattern {
	

}


//Mdefa {
//	*new { arg name, pat
		//Mdef.main.node_manager_.mpdefa(name, 
//	}

//}
