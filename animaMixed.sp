% mixes static drawing and animation

sorts
	#frame = {0,1,2}.
	#id = {line, lineTwo}.
	#co = {1,40}.
	#color = {green,blue}.
	#draw_line = draw_line(#id,#co,#co,#co,#co).
	#line_color = line_color(#id,#color).
	#drawing_command = #draw_line+#line_color.
predicates
	animate(#drawing_command,#frame).
	draw(#drawing_command).
rules
	animate(draw_line(line,1,1,40,40),0).
	animate(draw_line(line,1,1,40,1),F).
	animate(line_color(line,green),0).
	animate(line_color(line,blue),1).
	draw(draw_line(lineTwo,1,40,40,1)).
	draw(line_color(lineTwo,green)).