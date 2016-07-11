%% simple animation example
% draws a line for one frame

sorts

	#co = {1,40}.
	#id = {line}.
	#drawing_command = draw_line(#id,#co,#co,#co,#co).
	#frame = {0,1}.

predicates
	
	animate(#drawing_command, #frame).

rules

	animate(draw_line(line,1,1,40,40), 1).