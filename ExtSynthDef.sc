+ Instr {

    //asNamedSynthDef { arg name, args,outClass=\Out;
    //    var synthDef;
	//	"arrate".debug;
    //    synthDef = NamedInstrSynthDef.new;
    //    synthDef.build(this,this.convertArgs(args),outClass, name);
    //    ^synthDef
    //}

    asNamedSynthDef { arg name, rate, prependArgs;
		var instr_name;
		instr_name = this.dotNotation;
		
		SynthDef(name ?? instr_name, { arg out = 0;
			var ou;
			ou = SynthDef.wrap(Instr(instr_name).func, rate, prependArgs);
			ou = Out.ar(out, ou);
		}).add;
	}

	addSynthDef { arg rate, prependArgs, metadata;
		var instr_name;
		instr_name = this.dotNotation;
		
		SynthDef(instr_name, { arg out = 0;
			var ou;
			ou = SynthDef.wrap(Instr(instr_name).func, rate, prependArgs);
			ou = Out.ar(out, ou);
		}, metadata:metadata).add;
	}

	storeSynthDef { arg rate, prependArgs, metadata;
		var instr_name;
		instr_name = this.dotNotation;
		
		SynthDef(instr_name, { arg out = 0;
			var ou;
			ou = SynthDef.wrap(Instr(instr_name).func, rate, prependArgs);
			ou = Out.ar(out, ou);
		}, metadata:metadata).store;
	}

	wrap { arg args, rates, prependArgs;
		var fun;
		var instr_name = this.dotNotation;
		var instr = Instr(instr_name);
		var argnames;
		args = args ?? ();
		argnames = instr.func.argNames.collect({arg x, i; x -> instr.func.def.prototypeFrame[i] }).reject({ arg x; args[x.key].notNil });
		instr.func.def.argumentString.debug("argstr");
		~args = args;
		if(argnames.size > 0) {
			fun = "{ arg " ++ argnames.collect({arg x; x.key ++ " = " ++ x.value }).join(", ") ++ ";";
		} {
			fun = "{";
		};
		fun = fun ++ " Instr(\"" ++ instr.dotNotation ++ "\").value((";
		if(argnames.size > 0) {
			fun = fun ++ argnames.collect({arg x; x.key ++ ":" ++ x.key }).join(", ");
			fun = fun ++ ", ";
		} {
			// noop
		};
		fun = fun ++ args.keys.collect({ arg x; x ++ ": ~args[\\"++x++"]" }).asArray.join(", ") ++ ")); }";
		[instr_name, fun].debug("fun");
		^SynthDef.wrap( fun.interpret, rates, prependArgs)
	}
}

