PolyTouchResponder : MIDIResponder {
    classvar <painit = false,<par,<panumr;

    *new { arg function, src, chan, note, value, install=true,swallowEvent=false;
        ^super.new.function_(function).swallowEvent_(swallowEvent)
            .matchEvent_(MIDIEvent(nil, src, chan, note, value))
            .init(install)
    }
    *initialized { ^painit }
    *responders { ^panumr.select(_.notNil).flat ++ par }
    *add { arg resp;
        var temp;
        if(this.initialized.not,{ this.init });
        if((temp = resp.matchEvent.ctlnum).isNumber) {
            panumr[temp] = panumr[temp].add(resp);
        } {
            par = par.add(resp);
        };
    }
    *remove { arg resp;
        var temp;
        if((temp = resp.matchEvent.ctlnum).isNumber) {
            panumr[temp].remove(resp)
        } {
            par.remove(resp);
        };
    }
    *init {
        if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
        painit = true;
        par = [];
        panumr = Array.newClear(128);
        MIDIIn.polytouch = { arg src,chan,note,val;
            // first try pa num specific
            // then try non-specific (matches any pa )
            [panumr[note], par].any({ |stack|
                stack.notNil and: {stack.any({ |r| r.respond(src,chan,note,val) })}
            })
        };
    }
    learn {
        var oneShot;
        oneShot = PolyTouchResponder({ |src,chan,note,value|
                    this.matchEvent_(MIDIEvent(nil,src,chan,note,nil));
                    oneShot.remove;
                },nil,nil,nil,nil,true,true)
    }

    matchEvent_ { |midiEvent|
            // if note changes from non-number to number, or vice versa,
            // this responder is going to move between par and panumr
        if(matchEvent.notNil and:
                { matchEvent.note.isNumber !== midiEvent.note.isNumber })
        {
            this.remove;
            matchEvent = midiEvent;
            this.class.add(this);
        } {
            matchEvent = midiEvent;
        }
    }
}

