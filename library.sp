% contains a library of all drawing commands

% canvas constants
#const canvasWidth = 500.
#const canvasHeight = 500.
#const canvasSize = 500. % equal to smaller of above dimensions

% frame constant
#const numFrames = 60.

sorts

  % each frame is equal to 1/60th of a second
  #frame = 0..numFrames.

  % sorts required to use drawing commands

	% [row] and [col] specify pixel coordinates
	% on the canvas
	#row = 1..canvasHeight.
	#col = 1..canvasWidth.

	% [ang]le is a number that specifies an angle
	% equal to ([ang]/8)*pi, in radians
	#ang = 0..16.

	% [rad]ius is a number for specifying the 
	% radius of a circle
	#rad = 1..canvasSize.

	% [text] is a string that is to be displayed
	#text = {string1, string2, string3}.

  % sorts required to use styling commands

  	% [thick]ness represents the width of a line
  	#thick = 1..canvasSize.

  	% font[size] represents the size of font to use
  	#size = 8..72.

  	% font[fam]ily is the text font type
  	#fam = {georgia,palatino,antiqua,times,arial,helvetica,arialBlack,impact,tahoma,verdana}.

  	% cap is the style for a line [end]ing
  	#end = {butt,round,square}.

  	% align is the [place] alignment for text
  	#place = {left,right,center,start,end}.

  	% [c]o[l]o[r] is the color for any object
  	#clr = {black,navy,darkBlue,mediumBlue,blue,darkGreen,green,teal,darkCyan,deepSkyBlue,darkTurquoise,mediumSpringGreen,lime,springGreen,aqua,cyan,midnightBlue,dodgerBlue,lightSeaGreen,forestGreen,seaGreen,darkSlateGray,darkSlateGrey,limeGreen,mediumSeaGreen,turquoise,royalBlue,steelBlue,darkSlateBlue,mediumTurquoise,indigo,darkOliveGreen,cadetBlue,cornflowerBlue,rebeccaPurple,mediumAquaMarine,dimGray,dimGrey,slateBlue,oliveDrab,slateGray,slateGrey,lightSlateGray,lightSlateGrey,mediumSlateBlue,lawnGreen,chartreuse,aquamarine,maroon,purple,olive,gray,grey,skyBlue,lightSkyBlue,blueViolet,darkRed,darkMagenta,saddleBrown,darkSeaGreen,lightGreen,mediumPurple,darkViolet,paleGreen,darkOrchid,yellowGreen,sienna,brown,darkGray,darkGrey,lightBlue,greenYellow,paleTurquoise,lightSteelBlue,powderBlue,fireBrick,darkGoldenRod,mediumOrchid,rosyBrown,darkKhaki,silver,mediumVioletRed,indianRed,peru,chocolate,tan,lightGray,lightGrey,thistle,orchid,goldenRod,paleVioletRed,crimson,gainsboro,plum,burlyWood,lightCyan,lavender,darkSalmon,violet,paleGoldenRod,lightCoral,khaki,aliceBlue,honeyDew,azure,sandyBrown,wheat,beige,whiteSmoke,mintCream,ghostWhite,salmon,antiqueWhite,linen,lightGoldenRodYellow,oldLace,red,fuchsia,magenta,deepPink,orangeRed,tomato,hotPink,coral,darkOrange,lightSalmon,orange,lightPink,pink,gold,peachPuff,navajoWhite,moccasin,bisque,mistyRose,blanchedAlmond,papayaWhip,lavenderBlush,seaShell,cornsilk,lemonChiffon,floralWhite,snow,yellow,lightYellow,ivory,white}.

  % sort for connection from drawing to styling

	% [obj]ect is like an ID to reference a part 
	% of the drawing for styling purposes
	#stylename = {obj1,obj2,obj3}.

  % sorts that are the drawing commands

  	% given two coordinate pairs, draw a line
  	% from the first to the second with the 
  	% styling of the object specified
	#line = draw_line(#stylename,#col,#row,#col,#row).

	% given three coordinate pairs, draw a quadratic
	% curve from the first to the third, using the
	% second as a quadratic point and with the
	% styling of the object specified
	#quad = draw_quad_curve(#stylename,#col,#row,#col,#row,#col,#row).

	% given four coordinate pairs, draw a line from
	% the first to the fourth, using the second and
	% third coordinate pairs as the first and second
	% bezier points respectively, with the styling
	% of the object specified
	#bezier = draw_bezier_curve(#stylename,#col,#row,#col,#row,#col,#row,#col,#row).

	% given a coordinate pair, a radius and two 
	% angles, draw an arc (portion of a circle) from 
	% the first angle specified to the second of the
	% given radius centered at the coordinate pair
	% with the styling of the object specified
	#arc = draw_arc_curve(#stylename,#col,#row,#rad,#ang,#ang).

	% given a coordinate pair and some text, draw 
	% the text at the coordinate point with the 
	% styling of the object specified
	#text = draw_text(#stylename,#text,#col,#row).

  % sorts that are the styling commands

  	% style the specified object to have a 
  	% specified line width
  	#width = line_width(#stylename,#thick).

  	% style the given object to have a specified
  	% font size and specified font family
  	#font = text_font(#stylename,#size,#fam).

  	% style the cap of a line ending for a
  	% specified object 
  	#cap = line_cap(#stylename,#end).

  	% style the alignment of some text for a
  	% specified object
  	#align = text_align(#stylename,#place).

  	% style the color of a specified object
  	#color = line_color(#stylename,#clr).

  % general drawing sort
  	#drawing_command = #line+#quad+#bezier+#arc+#text+#width+#font+#cap+#align+#color.

predicates
	
	% drawing command applies at specified frame
	animate(#drawing_command,#frame).

	% static drawing command
	draw(#drawing_command)

rules
	% produce animation


%	A "frame" is a set of drawing
%	commands. These drawing commands in a frame 
%	will be executed at a 
%	time that corresponds to (1/60) * frame 
%	since the start of the animation