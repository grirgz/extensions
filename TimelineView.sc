// (c) 2006-2010, Thor Magnusson - www.ixi-audio.net
// GNU licence - google it.

// TODO
// change name para to timeline
// change node len to width and size to height
// determine coordinate system used by hooks


TimelineView : SCViewHolder {

	var <>viewport;
	var <>areasize;
	var <>paraNodes, connections; 
	var chosennode, mouseTracker;
	var <userView;
	var win;
	//var <bounds;
	var downAction, upAction, trackAction, keyDownAction, rightDownAction, overAction, connAction;
	var <>mouseDownAction;
	var <>mouseUpAction;
	var <>mouseMoveAction;
	var backgrDrawFunc;
	var background, fillcolor;
	var nodeCount, shape;
	var startSelPoint, endSelPoint, refPoint;
	var <selNodes, outlinecolor, selectFillColor, selectStrokeColor;
	var keytracker, conFlag; // experimental
	var nodeSize, swapNode;
	var font, fontColor;

	var <>nodeAlign=\debug;
	
	var refresh 			= true;	// false during 'reconstruct'
	var refreshDeferred	= false;
	var lazyRefreshFunc;
	
	*new { arg w, bounds; 
		^super.new.initParaSpace(w, bounds);
	}

	bounds { 
		^this.view.bounds
	}

	normRectToPixelRect { arg rect;
		var bounds = this.bounds;
		^Rect(
			rect.origin.x * bounds.extent.x / viewport.extent.x - ( viewport.origin.x * bounds.extent.x ), 
			rect.origin.y * bounds.extent.y / viewport.extent.y - ( viewport.origin.y * bounds.extent.y ),
			rect.width * bounds.extent.x/ viewport.extent.x,
			rect.height * bounds.extent.y / viewport.extent.y,
		);
	}

	pixelRectToNormRect { arg rect;
		var bounds = this.bounds;
		^Rect(
			rect.origin.x + ( viewport.origin.x * bounds.extent.x ) / bounds.extent.x * viewport.extent.x,
			rect.origin.y + ( viewport.origin.y * bounds.extent.y ) / bounds.extent.y * viewport.extent.y,
			rect.width / bounds.extent.x * viewport.extent.x,
			rect.height / bounds.extent.y * viewport.extent.y,
		);
	}

	normPointToPixelPoint { arg point;
		^this.normRectToPixelRect(Rect.fromPoints(point, point+Point(0,0))).origin;
	}

	pixelPointToNormPoint { arg point;
		^this.pixelRectToNormRect(Rect.fromPoints(point, point+Point(0,0))).origin;
	}
	
	initParaSpace { arg w, argbounds;
		var a, b, rect, relX, relY, pen;
		//bounds = argbounds ? Rect(20, 20, 400, 200);
		//bounds = Rect(bounds.left + 0.5, bounds.top + 0.5, bounds.width, bounds.height);

		//if((win= w).isNil, {
		//	win = GUI.window.new("ParaSpace",
		//		Rect(10, 250, bounds.left + bounds.width + 40, bounds.top + bounds.height+30));
		//	win.front;
		//});

		//mouseTracker = UserView.new(win, Rect(bounds.left, bounds.top, bounds.width, bounds.height));
		viewport = viewport ?? Rect(0,0,1,1);
		areasize = areasize ?? Point(2,128);
		mouseTracker = UserView.new;
		userView = mouseTracker;
		this.view = mouseTracker;
 		//bounds = mouseTracker.bounds; // thanks ron!
 		
		background = Color.white;
		this.view.background = Color.red(0.9);
		//fillcolor = Color.new255(103, 148, 103);
		//fillcolor = Color.green;
		outlinecolor = Color.red;
		selectFillColor = Color.green(alpha:0.2);
		selectStrokeColor = Color.black;
		paraNodes = List.new; // list of ParaNode objects
		connections = List.new; // list of arrays with connections eg. [2,3]
		nodeCount = 0;
		startSelPoint = 0@0;
		endSelPoint = 0@0;
		refPoint = 0@0;
		shape = "rect";
		conFlag = false;
		nodeSize = 8;
		font = Font("Arial", 9);
		fontColor = Color.black;
		pen	= GUI.pen;


		mouseTracker
			.canFocus_(true)
			.focusColor_(Color.clear.alpha_(0.0))
			//.relativeOrigin_(false)

			.mouseDownAction_({|me, px, py, mod, buttonNumber, clickCount|
				var bounds = this.bounds;
				var npos;
				npos = this.pixelPointToNormPoint(Point(px,py));
				mouseDownAction.(me, px, py, mod, buttonNumber, clickCount);
				[px, py, npos].debug("mouseDownAction_ npos");
				chosennode = this.findNode(npos.x, npos.y);
				if( (mod & 0x00040000) != 0, {	// == 262401
					//paraNodes.add(TimelineViewNode.new(x,y, fillcolor, bounds, nodeCount, nodeSize));
					//nodeCount = nodeCount + 1;
					//paraNodes.do({arg node; // deselect all nodes
					// 		node.outlinecolor = Color.black; 
					//		node.refloc = node.nodeloc;
					//});
					//startSelPoint = x-10@y-10;
					//endSelPoint =   x-10@y-10;
				}, {
					if(chosennode !=nil, { // a node is selected
						refPoint = npos; // var used here for reference in trackfunc
						chosennode.refloc = chosennode.nodeloc;
						
						if(conFlag == true, { // if selected and "c" then connection is possible
							paraNodes.do({arg node, i; 
								if(node === chosennode, {a = i;});
							});
							selNodes.do({arg selnode, j; 
								paraNodes.do({arg node, i; 
									if(node === selnode, {b = i;
										this.createConnection(a, b);
									});
								});
							});
						});
						downAction.value(chosennode);
					}, { // no node is selected
					 	paraNodes.do({arg node; // deselect all nodes
					 		node.outlinecolor = Color.black;  // FIXME: hardcoded N
							node.refloc = node.nodeloc;
					 	});
						startSelPoint = npos;
						endSelPoint = npos;
						this.refresh;
					});
				});
			})

			.mouseMoveAction_({|me, px, py, mod|
				var bounds = this.bounds;
				var x,y;
				var npos = this.pixelPointToNormPoint(Point(px,py));

				mouseMoveAction.(me, px, py, mod);
				if(chosennode != nil, { // a node is selected
					chosennode.setLoc_(chosennode.refloc + ( npos- refPoint ));
					block {|break|
						selNodes.do({arg node; 
							if(node === chosennode,{ // if the mousedown box is one of selected
								break.value( // then move the whole thing ...
									selNodes.do({arg node; // move selected boxes
										node.setLoc_(
											node.refloc + (npos - refPoint)
										);
										trackAction.value(node);
									});
								);
							}); 
						});
					};
					trackAction.value(chosennode);
					this.refresh;
				}, { // no node is selected
					endSelPoint = npos;
					this.refresh;
				});
			})

			.mouseOverAction_({arg me, x, y;
				var bounds = this.bounds;
				chosennode = this.findNode(x, y);
				if(chosennode != nil, {  
					relX = chosennode.nodeloc.x - bounds.left - 0.5;
					relY = chosennode.nodeloc.y - bounds.top - 0.5;
					overAction.value(chosennode);
				});
			})

			.mouseUpAction_({|me, x, y, mod|
				var bounds = this.bounds;
				mouseUpAction.(me, x, y, mod);
				if(chosennode !=nil, { // a node is selected
					upAction.value(chosennode);
					paraNodes.do({arg node; 
						node.refloc = node.nodeloc;
					});
					this.refresh;
				},{ // no node is selected
					// find which nodees are selected
					selNodes = List.new;
					paraNodes.do({arg node;
						// TODO: replace by Rect.fromPoints
						if(Rect(	startSelPoint.x, // + rect
								startSelPoint.y,									endSelPoint.x - startSelPoint.x,
								endSelPoint.y - startSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	endSelPoint.x, // - rect
								endSelPoint.y,									startSelPoint.x - endSelPoint.x,
								startSelPoint.y - endSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	startSelPoint.x, // + X and - Y rect
								endSelPoint.y,									endSelPoint.x - startSelPoint.x,
								startSelPoint.y - endSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
						if(Rect(	endSelPoint.x, // - Y and + X rect
								startSelPoint.y,									startSelPoint.x - endSelPoint.x,
								endSelPoint.y - startSelPoint.y)
								.containsPoint(node.nodeloc), {
									node.outlinecolor = outlinecolor;
									selNodes.add(node);
						});
					});
					startSelPoint = 0@0;
					endSelPoint = 0@0;
					this.refresh;
				});
			})

			.drawFunc_({		
				//var bounds = this.view.bounds;
				var bounds = this.bounds;
				var pstartSelPoint, pendSelPoint;
				var grid;
	
				pen.width = 1;
				pen.color = background; // background color
				pen.fillRect(Rect(0,0, bounds.width, bounds.height)); // background fill
				backgrDrawFunc.value; // background draw function

				// grid

				grid = DrawGrid(Rect(0,0,bounds.width,bounds.height), 
					ControlSpec(
							( areasize.x * viewport.origin.x ), 
							areasize.x * viewport.width + ( areasize.x * viewport.origin.x ), 
							\lin,
							0,
							0
					).grid,
					\midinote.asSpec.grid
				);
				grid.draw;
				
				// the lines

				pen.color = Color.black;
				connections.do({arg conn;
					pen.line(this.normPointToPixelPoint(paraNodes[conn[0]].nodeloc)+0.5, this.normPointToPixelPoint(paraNodes[conn[1]].nodeloc)+0.5);
				});
				pen.stroke;
				
				// the nodes or circles

				debug("start drawing nodes");
				bounds.debug("bounds");
				paraNodes.do({arg node;
					if(shape == "rect", {
						var rect;
						var pos;
						pen.color = node.color;
						pos = node.nodeloc;
						//[node.nodeloc.x, node.nodeloc.y].debug("nodeloc");
						//[pos.x , bounds.extent.x , viewport.extent.x , areasize.x].debug("x");
						//[pos.y , bounds.extent.y , viewport.extent.y , areasize.y].debug("y");
						//rect = Rect(
						//	pos.x * bounds.extent.x / viewport.extent.x - ( viewport.origin.x * bounds.extent.x ), 
						//	pos.y * bounds.extent.y / viewport.extent.y - ( viewport.origin.y * bounds.extent.y ),
						//	node.size * bounds.extent.x,
						//	1 * bounds.extent.y / areasize.y / viewport.extent.y,
						//);
						rect = this.normRectToPixelRect(node.rect);
						//[pos, bounds, viewport, areasize, node.rect, rect].debug("pos, bounds, viewport, areasize, node.rect, rect");
						//[node.rect, rect].debug("pos, bounds, viewport, areasize, node.rect, rect");
						pen.fillRect(rect);
						pen.color = node.outlinecolor;
						pen.strokeRect(rect);
					}, {
						pen.color = node.color;
						pen.fillOval(node.rect);
						pen.color = node.outlinecolor;
						pen.strokeOval(node.rect);
					});
					node.string.drawInRect(
						//Rect(node.rect.left+node.size+5, node.rect.top-3, 80, 16),   
						Rect(node.rect.left+5, node.rect.top+3, 120, node.rect.height),   
						font, fontColor
					);

				});
				debug("stop drawing nodes");
				pen.stroke;		
				
				// the selection node

				pstartSelPoint = this.normPointToPixelPoint(startSelPoint);
				pendSelPoint = this.normPointToPixelPoint(endSelPoint);

				pen.color = selectFillColor;
				pen.fillRect(Rect(	pstartSelPoint.x + 0.5, 
									pstartSelPoint.y + 0.5,
									pendSelPoint.x - pstartSelPoint.x,
									pendSelPoint.y - pstartSelPoint.y
									));
				pen.color = selectStrokeColor;
				pen.strokeRect(Rect(	pstartSelPoint.x + 0.5, 
									pstartSelPoint.y + 0.5,
									pendSelPoint.x - pstartSelPoint.x,
									pendSelPoint.y - pstartSelPoint.y
									));


				// background frame

				pen.color = Color.black;
				pen.strokeRect(Rect(0,0, bounds.width, bounds.height)); 
			})
			.keyDownAction_({ |me, key, modifiers, unicode, keycode |
				//if(unicode == 127, {
				//	selNodes.do({arg box; 
				//		paraNodes.copy.do({arg node, i; 
				//			if(box === node, {this.deleteNode(i)});
				//		})
				//	});
				//});
				if(unicode == 99, {conFlag = true;}); // c is for connecting
				keyDownAction.value(me, key, modifiers, unicode, keycode);
				this.refresh;
			})
			.keyUpAction_({ |me, key, modifiers, unicode |
				if(unicode == 99, {conFlag = false;}); // c is for connecting

			});
	}

	clearSpace {
		paraNodes = List.new;
		connections = List.new;
		nodeCount = 0;
		this.refresh;
	}

	gridPointToNormPoint { arg point;
		^(point / areasize)
	}

	createNode {arg x, y, size, color, refresh=true;
		var bounds = this.bounds;
		size = this.gridPointToNormPoint(size);
		this.createNode1(x/areasize.x, y/areasize.y, size, color, refresh);
	}
	
	createNode1 {arg argX, argY, size, color, refresh=true;
		var x, y;
		var bounds = this.bounds;
		x = argX;
		y = argY;
		[argX, argY, x,y, bounds].debug("createNode1");
		fillcolor = color ? fillcolor;
		paraNodes.add(TimelineViewNode.new(x, y, fillcolor, bounds, nodeCount, size, nodeAlign));
		nodeCount = nodeCount + 1;
		if(refresh == true, {this.refresh});
	}
		
	createConnection {arg node1, node2, refresh=true;
		if((nodeCount < node1) || (nodeCount < node2), {
			"Can't connect - there aren't that many nodes".postln;
		}, {
			block {|break|
				connections.do({arg conn; 
					if((conn == [node1, node2]) || (conn == [node2, node1]), {
						break.value;
					});	
				});
				// if not broken out of the block, then add the connection
				connections.add([node1, node2]);
				connAction.value(paraNodes[node1], paraNodes[node2]);
				if(refresh == true, {this.refresh});
			}
		});
	}

	deleteConnection {arg node1, node2, refresh=true;
		connections.do({arg conn, i; if((conn == [node1, node2]) || (conn == [node2, node1]),
			 { connections.removeAt(i)})});
		if(refresh == true, {this.refresh});
	}

	deleteConnections { arg refresh=true; // delete all connections
		connections = List.new; // list of arrays with connections eg. [2,3]
		if(refresh == true, {this.refresh});
	}
	
	deleteNode {arg nodenr, refresh=true; var del;
		del = 0;
		connections.copy.do({arg conn, i; 
			if(conn.includes(nodenr), { connections.removeAt((i-del)); del=del+1;})
		});
		connections.do({arg conn, i; 
			if(conn[0]>nodenr,{conn[0]=conn[0]-1});if(conn[1]>nodenr,{conn[1]= conn[1]-1});
		});
		if(paraNodes.size > 0, {paraNodes.removeAt(nodenr)});
		if(refresh == true, {this.refresh});
	}
	
	setNodeLoc_ {arg index, argX, argY, refresh=true;
//		var x, y;
//		x = argX+bounds.left;
//		y = argY+bounds.top;
		paraNodes[index].setLoc_(Point(argX+0.5, argY+0.5));
		if(refresh == true, {this.refresh});
	}
	
	setNodeLocAction_ {arg index, argX, argY, action, refresh=true;
//		var x, y;
//		x = argX+bounds.left;
//		y = argY+bounds.top;
		paraNodes[index].setLoc_(Point(argX, argY));
		switch (action)
			{\down} 	{downAction.value(paraNodes[index])}
			{\up} 	{upAction.value(paraNodes[index])}
			{\track} 	{trackAction.value(paraNodes[index])};
		if(refresh == true, {this.refresh});
	}
	
	getNodeLoc {arg index;
		var x, y;
		x = paraNodes[index].nodeloc.x-0.5;
		y = paraNodes[index].nodeloc.y-0.5;
		^[x, y];
	}

	setNodeLoc1_ {arg index, argX, argY, refresh=true;
		var x, y;
		var bounds = this.bounds;
		x = (argX * bounds.width).round(1);
		y = (argY * bounds.height).round(1);
		paraNodes[index].setLoc_(Point(x+0.5, y+0.5));
		if(refresh == true, {this.refresh});
	}

	setNodeLoc1Action_ {arg index, argX, argY, action, refresh=true;
		var x, y;
		var bounds = this.bounds;
		x = (argX * bounds.width).round(1);
		y = (argY * bounds.height).round(1);
		paraNodes[index].setLoc_(Point(x+bounds.left, y+bounds.top));
		switch (action)
			{\down} 	{downAction.value(paraNodes[index])}
			{\up} 	{upAction.value(paraNodes[index])}
			{\track} 	{trackAction.value(paraNodes[index])};
		if(refresh == true, {this.refresh});
	}

	getNodeLoc1 {arg index;
		var bounds = this.bounds;
		var x, y;
		x = paraNodes[index].nodeloc.x  / bounds.width;
		y = paraNodes[index].nodeloc.y  / bounds.height;
		^[x, y];
	}
	
	getNodeStates {
		var locs, color, size, string;
		locs = List.new; color = List.new; size = List.new; string = List.new;
		paraNodes.do({arg node; 
			locs.add(node.nodeloc);
			color.add(node.color); 
			size.add(node.size);
			string.add(node.string);
		});
		^[locs, connections, color, size, string];
	}

	setNodeStates_ {arg array; // array with [locs, connections, color, size, string]
		var bounds = this.bounds;
		if(array[0].isNil == false, {
			paraNodes = List.new; 
			array[0].do({arg loc; 
				paraNodes.add(TimelineViewNode.new(loc.x, loc.y, fillcolor, bounds, nodeCount, Point(1,1), nodeAlign)); // TODO: size
				nodeCount = nodeCount + 1;
				})
		});
		if(array[1].isNil == false, { connections = array[1];});
		if(array[2].isNil == false, { paraNodes.do({arg node, i; node.setColor_(array[2][i];)})});
		if(array[3].isNil == false, { paraNodes.do({arg node, i; node.extent_(array[3][i];)})});
		if(array[4].isNil == false, { paraNodes.do({arg node, i; node.string = array[4][i];})});
		this.refresh;
	}

	setBackgrColor_ {arg color, refresh=true;
		background = color;
		if(refresh == true, {this.refresh});
	}
		
	setFillColor_ {arg color, refresh=true;
		fillcolor = color;
		paraNodes.do({arg node; 
			node.setColor_(color);
		});
		if(refresh == true, {this.refresh});
	}
	
	setOutlineColor_ {arg color;
		outlinecolor = color;
		this.refresh;
	}
	
	setSelectFillColor_ {arg color, refresh=true;
		selectFillColor = color;
		if(refresh == true, {this.refresh});
	}

	setSelectStrokeColor_ {arg color, refresh=true;
		selectStrokeColor = color;
		if(refresh == true, {this.refresh});
	}
	
	setShape_ {arg argshape, refresh=true;
		shape = argshape;
		if(refresh == true, {this.refresh});
	}
	
	reconstruct { arg aFunc;
		refresh = false;
		aFunc.value( this );
		refresh = true;
		this.refresh;
	}

	refresh {
		if( refresh, { {mouseTracker.refresh}.defer; });
	}

	lazyRefresh {
		if( refreshDeferred.not, {
			AppClock.sched( 0.02, lazyRefreshFunc );
			refreshDeferred = true;
		});
	}
				
	setNodeSize_ {arg index, size, refresh=true;
		paraNodes[index].extent_(size);
		if(refresh == true, {this.refresh});
	}

	getNodeSize {arg index;
		^paraNodes[index].extent;
	}
	
	setNodeColor_ {arg index, color, refresh=true;
		paraNodes[index].setColor_(color);
		if(refresh == true, {this.refresh});
	}
	
	getNodeColor {arg index;
		^paraNodes[index].getColor;	
	}
	
	setFont_ {arg f;
		font = f;
	}
	
	setFontColor_ {arg fc;
		fontColor = fc;
	}
	
	setNodeString_ {arg index, string;
		paraNodes[index].string = string;
		this.refresh;		
	}
	
	getNodeString {arg index;
		^paraNodes[index].string;
	}
	// PASSED FUNCTIONS OF MOUSE OR BACKGROUND
	nodeDownAction_ { arg func;
		downAction = func;
	}
	
	nodeUpAction_ { arg func;
		upAction = func;
	}
	
	nodeTrackAction_ { arg func;
		trackAction = func;
	}
	
	nodeOverAction_ { arg func;
		overAction = func;
		win.acceptsMouseOver = true;
	}
	
	connectAction_ {arg func;
		connAction = func;
	}
	
	setMouseOverState_ {arg state;
		win.acceptsMouseOver = state;
	}
	
	keyDownAction_ {arg func;
		keyDownAction = func;
	}
	
	setBackgrDrawFunc_ { arg func;
		backgrDrawFunc = func;
		this.refresh;
	}
	
	// local function
	findNode {arg x, y;
		paraNodes.do({arg node; 
			if(node.rect.containsPoint(Point.new(x,y)), {
				^node;
			});
		});
		^nil;
	}
}

TimelineViewNode {
	var <>fillrect, <>state, <>size, <rect, <>nodeloc, <>refloc, <>color, <>outlinecolor;
	var <width, <height;
	var <>spritenum, <>temp;
	var <>len;
	var <>align;
	var bounds;
	var <>string;
	
	*new { arg x, y, color, bounds, spnum, size, align=\topLeft; 
		^super.new.initGridNode(x, y, color, bounds, spnum, size, align);
	}

	compute_rect { arg point;
		//align.debug("TimelineViewNode: align");
		switch(align,
			\centerLeft, {
				//align.debug("TimelineViewNode1: align");
				rect = Rect(nodeloc.x+0.5, nodeloc.y-(height/2)+0.5, width, height);
			},
			\center,  {
				//align.debug("TimelineViewNode2: align");
				rect = Rect(nodeloc.x-(width/2)+0.5, nodeloc.y-(height/2)+0.5, width, height);
			},
			\debug,  {
				//align.debug("TimelineViewNode2: align");
				rect = Rect(nodeloc.x, nodeloc.y, width, height);
			},
			{
				rect = Rect(nodeloc.x+0.5, nodeloc.y+0.5, width, height);
			}
		);
	}
	
	initGridNode { arg argX, argY, argcolor, argbounds, spnum, argsize, argalign;
		spritenum = spnum;
		nodeloc =  Point(argX, argY);	
		refloc = nodeloc;
		color = argcolor;	
		outlinecolor = Color.black;
		this.extent = argsize;
		bounds = argbounds;
		align = argalign;
		this.compute_rect;
		string = "";
		temp = nil;
	}
		
	setLoc_ {arg point;
		nodeloc = point;
		// keep paranode inside the bounds
		if((point.x) > bounds.width, {nodeloc.x = bounds.width - 0.5});
		if((point.x) < 0, {nodeloc.x = 0.5});
		if((point.y) > bounds.height, {nodeloc.y = bounds.height -0.5});
		if((point.y) < 0, {nodeloc.y = 0.5});
		this.compute_rect;
	}
	
	origin_ { arg point;
		this.setLoc_(point)
	}

	origin {
		nodeloc
	}
		
	setState_ {arg argstate;
		state = argstate;
	}
	
	getState {
		^state;
	}

	width_ { arg val;
		width = val;	
		this.compute_rect;
	}
	
	height_ {arg val;
		height = val;
		this.compute_rect;
	}

	extent {
		^Point(width, height)
	}

	extent_ { arg point;
		width = point.x;
		height = point.y;
	}
	
	setColor_ {arg argcolor;
		color = argcolor;
	}
	
	getColor {
		^color;
	}
}
