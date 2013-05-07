
+ AbstractEnv {

	levelScale { |scale|
		^this.copy.levels_(levels * scale)
	}

	levelBias { |bias|
		^this.copy.levels_(levels + bias)
	}

	timeScale { |scale|
		^this.copy.times_(times * scale)
	}

}
