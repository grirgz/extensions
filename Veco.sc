
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
		^main.get_nodeclip_by_uname(uname);
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

		if(rate.isNil) {
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

	*freeClient {
		this.freeAll;
	}

	*freeAll {
		all.do { _.free };	
		all = IdentityDictionary.new;
	}

}

//Veco.save('2.2').stepseq
