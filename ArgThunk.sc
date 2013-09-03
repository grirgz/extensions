
ArgThunk : AbstractFunction {
	// a thunk is an unevaluated value.
	// it gets evaluated once and then always returns that value.
	// also known as a "promise" in Scheme.
	// thunks have no arguments.
	var function, value;

	*new { arg function;
		^super.newCopyArgs(function)
	}
	value { arg ... args;
		^value ?? { value = function.valueArray(args); function = nil; value }
	}
}

//ThunkDef {
//	classvar <>all;
//	classvar <>allfree;
//	var <key;
//	var <>freefunction;
//
//	*initClass {
//		all = IdentityDictionary.new;
//		//Class.initClassTree(Event);
//	}
//
//	*new { arg name, function, freefunction;
//
//		if(function.isNil) {
//			// access
//			all.at(name).value;
//		} {
//			// define
//			if(freefunction.notNil) {
//				// freeable object
//				if(all.at(name).notNil) {
//					// already exists, free it
//					if(allfree.at(name).notNil) {
//						allfree.at(name).value(all.at(name))
//					}
//				};
//
//			}
//			all.put(name, Thunk(function))
//		}
//	}
//
//}
//
//BusDef {
//	
//}
