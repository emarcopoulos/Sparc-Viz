%% a test for the static drawing solution

% simply draws a line

sorts
	#co = {1,40}.
	#id = {line}.

predicates
	draw_line(#id,#co,#co,#co,#co).

rules
	draw_line(line,1,1,40,40).