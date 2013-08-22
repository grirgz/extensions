
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
	*init { arg initfun;
		if(main.isNil) {
			this.force_init(initfun)
		}
	}

}
