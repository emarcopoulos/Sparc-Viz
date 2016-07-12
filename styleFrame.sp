% example sparc animation using styling

sorts
	#frame = {0,1,2}.
	#id = {line}.
	#co = {1,40}.
	#color = {green,blue}.
	#draw_line = draw_line(#id,#co,#co,#co,#co).
	#line_color = line_color(#id,#color).
	#drawing_command = #draw_line+#line_color.
predicates
	animate(#drawing_command,#frame).
rules
	animate(draw_line(line,1,1,40,40),0).
	animate(draw_line(line,1,1,40,1),F).
	animate(line_color(line,green),0).
	animate(line_color(line,blue),1).