
Veco {
	classvar <>main;
	*force_init { arg initfun;
		if(initfun.isNil) {
			(Platform.userExtensionDir +/+ "seco/seco/veco/main.scd").standardizePath.load;
			main = ~sceneset;
		} {
			main = initfun.();
		};
		^main;

	}
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

}


//Veco.save('2.2').stepseq
