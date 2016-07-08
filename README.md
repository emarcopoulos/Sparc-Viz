# Sparc Visualization through Animation Project
## REU TTU Summer 2016
###### Worked on by Elias Marcopoulos, Crisel Suarez.

#### Project Description
Sparc is a declarative language that takes an input and gives an answer set in return. Sometimes, it is hard to understand these answer sets, as they can be quite long and complicated. The project we are working on is to extend the work done by Maede Rayatidamavandi to make these answer sets drawable. The extension will be to turn these drawings into possible animations. These animations are good visualizations of answer sets that represent systems in which the passage of time exists.

#### Files
animate.java
- File originally called solution.java, taken from the staff working in the KRLab at Texas Tech University
- solution.java was the source code for creating static drawings from Sparc answer sets
- Our goal is to modify this file to be able to create animations as well as static drawings from Sparc answer sets

animaFrame.sp
- Example SPARC code that animates a line using the predicate animate(#drawing_command, #frame).

animaStatic.sp
- Example SPARC code that draws a line using the predicate draw(#drawing_command).

expectedOutput_animaFrame.html
- JavaScript/HTML5 code of what the animate.java will output in order for an animation to be executed. 

expectedOutput_animaStatic.html
-JavaScript/HTML5 code of what the animate.java will output in order for a static drawing to be executed.

library.sp
- All the drawing commands found in Maede's Drawing Report written in a SPARC file.

staticDrawing.sp
-SPARC code that draws a line. 
