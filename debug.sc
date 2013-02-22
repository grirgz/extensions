Debug {
	classvar <>enableDebug = true;
}

+ Ptrace {

	embedInStream { arg inval;
		var func, collected;
		printStream = printStream ? Post;
		if(key.isNil) {
			collected = pattern.collect {|item| 
				if(Debug.enableDebug) {
					printStream << prefix << item << Char.nl;
				};
				item
			}
		} {
			func = { |val, item, prefix|
				if(Debug.enableDebug) {
					if(val.isKindOf(Function) and: { item.isKindOf(Environment) })
						{
							val = item.use { val.value };
							printStream << prefix << val << "\t(printed function value)\n";
						} {
							printStream << prefix << val << Char.nl;
						};
				}
			}.flop;
			collected = pattern.collect {|item|
				var val = item.atAll(key.asArray).unbubble;
				func.value(val, item, prefix);
				item
			}
		};
		^collected.embedInStream(inval)
	}

}

+ Object {

	debug { arg caller;
		if(Debug.enableDebug) {
			if(caller.notNil,{
				Post << caller << ": " << this << Char.nl;
			},{
				Post << this << Char.nl;
			});
		}
	}
}

+ RawArray { // string, signal

	debug { arg caller;
		if(Debug.enableDebug) {
			if(caller.notNil,{
				Post << caller << ": " << this << Char.nl;
			},{
				Post << this << Char.nl;
			});
		}
	}
}

+ Collection {

	debug { arg caller;
		if(Debug.enableDebug) {
			if(caller.notNil,{
				if(this.size < 10,{
					Post << caller << ": " << this << Char.nl;
				},{
					Post << caller << ": " <<* this << Char.nl;
				});
			},{
				Post <<* this << Char.nl;
			});
		}
	}
}
