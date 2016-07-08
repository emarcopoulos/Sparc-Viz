%% simple static animation example
% draws a line on the page

sorts

	#co = {1,40}.
	#id = {line}.
	#drawing_command = draw_line(#id,#co,#co,#co,#co).

predicates
	
	draw(#drawing_command).

rules

	draw(draw_line(line,1,1,40,40)).