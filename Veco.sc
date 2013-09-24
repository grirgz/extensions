
Veco {
	classvar <>main;
	classvar <>fxgroup;
	*force_init { arg initfun;
		if(initfun.isNil) {
			(Platform.userExtensionDir +/+ "seco/seco/veco/main.scd").standardizePath.load;
			main = ~sceneset;
		} {
			main = initfun.();
		};
		^main;

	}

	//*init_groups {
	//	fxgroup = Group.after(1);
	//}

	*new { arg name, pat;
		var node = main.get_node_by_uname(name);
		^node;
	}
	*init { arg initfun;
		if(main.isNil) {
			this.force_init(initfun)
		}
	}

	*save { arg uname;
		//^main.get_nodeclip_by_uname(uname);
		^main.get_node_by_uname(uname).data;
	}

	*load_file { arg path;
		(main.project_path +/+ path).load;
	}

	*load_lib { arg path;
		(main.lib_path +/+ path).load;
	}

}


BufDef {
	classvar <>client = \veco;
	classvar <>all;
	classvar <>root;

	*initClass {
		all = IdentityDictionary.new;
		root = "~/Musique/sc/samplekit".standardizePath;
	}

	*new { arg name, path;
		path = this.my_new(name, path);
		path.debug("BufDef.new: path");
		^BufferPool.get_stereo_sample(client, path);
	}

	*mono { arg name, path;
		path = this.my_new(name, path);
		^BufferPool.get_mono_sample(client, path);
	}

	*my_new { arg name, path;
		if(path.isNil) {
			path = all.at(name);
			path = this.relpath_to_abspath(path);
		} {
			all.put(name, path);
			path = this.relpath_to_abspath(path);
		};
		^path;
	}

	*relpath_to_abspath { arg path;
		if(PathName(path).isRelativePath) {
			^(root +/+ path)
		} {
			^path
		}
	}

	*freeClient {
		BufferPool.release_client(client)
	}

	*freeAll {
		BufferPool.reset;	
	}

}

BusDef {
	
	classvar <>client = \veco;
	classvar <>all;
	classvar <>root;

	*initClass {
		all = IdentityDictionary.new;
		root = "~/Musique/sc/samplekit".standardizePath;
	}

	*new { arg name, rate, channels;
		var bus;

		if(all.at(name).notNil or: {rate.isNil}) {
			bus = all.at(name)
		} {
			if(channels.isNil) {
				if(rate == \audio) {
					channels = 2
				} {
					channels = 1
				};
			};
			bus = Bus.alloc(rate, Server.default, channels);
			all.put(name, bus);
		};
		^bus;
	}

	*free { arg name;
		all.at(name).free;
		all.put(name, nil);
	}

	*freeClient {
		this.freeAll;
	}

	*freeAll {
		all.do { _.free };	
		all = IdentityDictionary.new;
	}

}


Sdef {
	//classvar storage;
	//classvar specs;

	*initClass {
		///storage = IdentityDictionary.new;
		///specs = IdentityDictionary.new;
	}
	
	*new { arg node_uname, name, kind, spec;
		var bus;
		var storage = ~score_storage;
		if(kind.isNil) {
			^storage.get(node_uname, name)
		} {
			^storage.define(node_uname, name, kind, spec);
		}
	}

	*load_data_from_clip { arg source_clip, dest_clip;
		var storage = ~score_storage;
		^storage.load_data_from_clip(source_clip, dest_clip);
	}

	*load_clip_data { arg clip;
		var storage = ~score_storage;
		^storage.load_clip_data(clip);


	}
}
//Veco.save('2.2').stepseq


+ NodeProxy {
	isource_ { arg obj;
		if(this.isPlaying == true) {
			this.put(nil, obj, 0)
		} {
			this.put(nil, obj, 0, nil, false);
		}
	}
}


//Sdef {
//	classvar storage;
//	classvar specs;
//
//	*initClass {
//		storage = IdentityDictionary.new;
//		specs = IdentityDictionary.new;
//	}
//	
//	*new { arg node_name, name, kind, spec;
//		var bus;
//		switch(kind, 
//			nil, {
//				^Pfunc({storage[node_name+++name]});
//			},
//			\var, {
//				storage[node_name+++name] = storage[node_name+++name] ?? 1;
//				specs[node_name+++name] = spec;
//				^storage[node_name+++name];
//			},
//			\sampler, {
//
//			}
//		);
//	}
//
//	*pbind { arg node_name, name, key;
//		key = key ?? name;
//		Pbind(key, Pfunc({storage[node_name+++name]}))
//	}
//
//	*get_value { arg node_name, name, val;
//		^storage[node_name+++name]
//	}
//
//	*set_value { arg node_name, name, val;
//		storage[node_name+++name] = val;
//		^storage[node_name+++name]
//	}
//
//	*edit { arg node_name, name, spec;
//		spec = spec ?? specs[node_name+++name];
//		~edit_sdef_variable.(node_name, name, spec);
//	}
//
//
//
//}
