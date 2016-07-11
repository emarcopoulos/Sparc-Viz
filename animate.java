/*
	in the current folder you can find a file called test.txt
	you have to run this java file on test.txt. What this code is doing is to create a vector of strings containg all visual atoms 
	in the answer set of the SPARC program written in test.txt.


	go to main function: (line about 400)
		- the variable vizAtoms is the vector containing visual atoms (answer set of test.txt)
		- We will call the void function "translate" on vizAtoms and print the translation of atoms
		- inside function translate you can see there are 4 translation functions related to each paerson's part. 
		  Implement each function based on the explanation that you see. One function per person. 
		  
	Important Note: If you write the SPARC program incorrect you can not see any result. Now the test file contains 
					a correct SPARC program. You can test whether you get the correct answer set first by printing the 
					elements of vizAtoms, and when you are sure you have the correct atoms proceed to translation. 
		
	
	

	For Sonali... if you want to test your program on your specific part atoms what you have to do is write a SPARC program 
	which its answer set is your atoms. The following is an example:
	
	sorts
	#n={1,2,3}.
	#style={a}.
	#color={blue,red}.
	#cap={round}.

	predicates
	draw_bezier_curve(#style,#n,#n,#n,#n,#n,#n,#n,#n).
	line_width(#style,#n).
	line_cap(#style,#cap).
	line_color(#style,#color).
	
	rules
	draw_bezier_curve(a,1,1,1,1,1,1,1,1).
	line_width(a,1).
	line_cap(a,round).
	line_color(a,red).

		
	
		Then after running getVizAtoms on the test, vizAtoms will be the vector:
			[draw_bezier_curve(a,1,1,1,1,1,1,1,1), line_width(a,1), line_cap(a,round), line_color(a,red)]	
	


*/



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.io.*; 


class PathTo {
	public static String java = "C:\\Program Files\\Java\\jdk1.8.0_65\\bin\\java";
	public static String sparcJar = "..\\sparc.jar";
}


class StreamGobbler
extends Thread {

	InputStream is;
	String type;
	Object lock;
	boolean ready = false;

	StreamGobbler (InputStream is, String type, Object lock) {
		this.is = is;
		this.type = type;
		this.lock = lock;

	}

	@Override
	public void run () {	 
        int count = 0;
        final int maxCount = 100000000;
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null ) {
				count += line.length();
				if(count > maxCount) {
			    	 System.err.println("ERROR: the output from the solver exceeds "  + Integer.toString(maxCount)+  " characters."
			    	 		+ "\n Your program has too many answer sets and we can't process all of them.");
			    	 // finish him!
			    	 Runtime.getRuntime().halt(0);	
				}
				//System.out.println(type + ">" + line);
				if(type.equals("STDOUT")) {	
					OsUtils.result.append(line).append("\n");  		
				}
				else if(type.equals("ERROR")) {	
					    OsUtils.errors.append(line).append("\n");
				}

			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		synchronized (lock) {
			ready = true;
			lock.notifyAll();
		}

	}

	public boolean isReady() {
		return ready;
	}
}

abstract class AnswerSetParser {
    public abstract  ArrayList<AnswerSet> getAnswerSets(String output);
}

 class DLVAnswerSetParser extends AnswerSetParser{
    private Scanner sc;
    private String buf;
    int bufIdx;
    public DLVAnswerSetParser() {

    }

    private Character nextChar() {
        if(buf==null || bufIdx==buf.length()) {
           if(sc.hasNext()) {
               buf=sc.next();
               bufIdx=0;
           }
           else {
               return null;
           }
        }
        return buf.charAt(bufIdx++);
    }

    @Override
    public ArrayList<AnswerSet> getAnswerSets(String output) {
        sc=new Scanner(new StringReader(output));
        ArrayList<AnswerSet> result=new ArrayList<AnswerSet>();
        Character next=null;
        while((next=nextChar())!=null) {
            if(next=='{') {
                StringBuilder sb=new StringBuilder();
                while((next=nextChar())!='}') {
                    sb.append(next);
                }
                AnswerSet answerSet=new AnswerSet();
               
                answerSet.literals.addAll(StringListUtils.splitCommaSequence(sb.toString()));
                result.add(answerSet);
            }
        }

        return result;
    }
}

class ClingoAnswerSetParser extends AnswerSetParser{

    @Override
    public ArrayList<AnswerSet> getAnswerSets(String output) {
        Scanner sc=new Scanner(new StringReader(output));
        ArrayList<AnswerSet> result=new ArrayList<AnswerSet>();
        while(sc.hasNext()) {
          String nextLine=sc.nextLine();
          if(nextLine.startsWith("Answer")) {
              AnswerSet answerSet=new AnswerSet();
              String answerSetLine=sc.nextLine();
              if(answerSetLine.length()>0 && !answerSetLine.matches("/^\\s*$/")) {
            	   String[] list= answerSetLine.split("\\s+");
                   Collections.addAll(answerSet.literals,list);
              }
           
              result.add(answerSet);
          }
        }
        return result;
    }
}


class OsUtils {

	public static StringBuffer result;
	public static StringBuffer errors;

	public static void runCommand(String path, String options,String input) {
		Object lockStdOut = new Object();
		Object lockStdErr = new Object();
		Process process = null;
		OsUtils.errors = new StringBuffer();
		OsUtils.result = new StringBuffer();
		try {
			//create a new process for dlv
			process = Runtime.getRuntime().exec(path+options);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
	
		}   
		      
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),"ERROR",lockStdErr);
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),"STDOUT",lockStdOut);
		// kick them off
		errorGobbler.start();
		outputGobbler.start();
		try {
			// write the provided input to the process
			if(input != null) {
				OutputStream stdin = process.getOutputStream();	 
				stdin.write(input.getBytes(), 0, input.length());
				stdin.flush();
				stdin.close();
			}
			try {
				process.waitFor();			            				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// wait until error stream is ready:
			synchronized(lockStdErr){
				while (!errorGobbler.isReady()){
					try {
						lockStdErr.wait();
					} catch (InterruptedException e) {
						// This should never happen!
						e.printStackTrace();
					}
				}
			}

			// wait until std out stream is ready:
			synchronized(lockStdOut){
				while (!outputGobbler.isReady()){
					try {
						lockStdOut.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}    
		} catch (IOException ex) {
			ex.printStackTrace(); // this exception should not occur!
		}
	}

}





class AnswerSet {
    public ArrayList<String> literals; // literals is an array list of strings
	
	
    public AnswerSet() {
        literals=new ArrayList<String>();
    }
}



class Parser{

    public static String computeAnswerSets(String programPath) {
	       OsUtils.runCommand(PathTo.java, " -jar "+PathTo.sparcJar+" -A "+programPath, "");
		   
		   return OsUtils.result.toString();
	}

		

	
	
	//isVusual takes an atom as input and checks whether it is from drawing commands or not; returns true if it is visual, false otherwise.
	
	
	public static boolean isNumeric(String str)  {  
		try  
		{  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
		return false;  
		}  
		return true;  
	}
	
	
	public static int toInt(String literal){
		int result;
		
		
		if (isNumeric(literal)){
		result = Integer.parseInt(literal);	}
		else {result=-1;}
		//System.out.println(result);
		
		return result;

	}	
	
	
	public static int elementNum(String literal){
		int num=1;
		char a_char;
		for(int j=0; j<literal.length();j++){
			a_char = literal.charAt(j);
			if (a_char==','){
				num++;
			}
		}
		return num;
	}	
	
	
	public static String getElement(String literal, int index){

//		String test=new String();
		int i=0;
		int start=0,end=0;
		int parameter=0;
		char a_char = literal.charAt(i);
		
		while (parameter<index){
			while ((literal.charAt(i)!='(' && literal.charAt(i)!=',')&& i<literal.length()-1) i++;
			i=i+2;
			start=i-1;
			while ((literal.charAt(i)!=')' && literal.charAt(i)!=',')&& i<literal.length()-1) i++;
			end=i;
			parameter++;
			
		}
		
		return literal.substring(start,end);
	}
	
	
	public static String getPredicate(String literal){
		int i=0;
		char a_char = literal.charAt(i);
		while (literal.charAt(i)!='(' && i<literal.length()-1) i++;
		//System.out.println(literal.substring(0,i));
		return literal.substring(0,i);
	}
	
	public static int getMaxFrame(Vector<String> vizAtoms) {
		int max = 0;
		for (String vizAtom : vizAtoms) {
			int temp = getFrame(vizAtom);
			if (temp > max) {
				max = temp;
			}
		}
	}

	public static int getFrame(String vizAtom) {
		int i = 0;
		while (vizAtom.charAt(i) != ')' && i < vizAtom.length()) i++;
		return toInt(vizAtom.substring(i,vizAtom.length()));
	}

	public static Vector<String> getDrawingCommands(Vector<String> vizAtoms) {
		Vector<String> drawing_commands=new Vector<String>();
		for(String vizAtom : vizAtoms) {
			drawing_commands.add(getDrawingCommand(vizAtom));
		}
		return drawing_commands;
	}

	public static String getDrawingCommand(String literal){
		int i=0;
		while (literal.charAt(i)!=')' && i < literal.length()) i++;
		// if the predicate is 'draw' then offset of 6, if 'animate' then offset of 9
		if (literal.charAt(0) == 'd') {
			return literal.substring(5,i+1);
		}
		return literal.substring(8,i+1);
	}
	
	
	public static boolean isVisual(String literal) {
 	  	switch (getPredicate(literal))
 	  	{
 	  		case "animate": return true;
 	  		case "draw_line": return true;
 	  	}
		return false;
	}
	
	
	public static boolean isCap(String Cap) {
		if(Cap.equals("butt")) return true; 
		if(Cap.equals("round")) return true;
		if(Cap.equals("square")) return true; 
		
		return false;
	}
	
	public static boolean isAlign(String Align) {
		if(Align.equals("left")) return true; 
		if(Align.equals("right")) return true;
		if(Align.equals("center")) return true; 
		if(Align.equals("start")) return true;
		if(Align.equals("end")) return true; 
		
		return false;
	}
	
	public static boolean isFontFamily(String fontFamily ) {

		if(fontFamily.equals("tahoma")) return true; 
		if(fontFamily.equals("arial")) return true;
		return false;
	}
	
	public static boolean isColor(String color) {
		if(color.equals("blue")) return true; 
		if(color.equals("green")) return true;
		if(color.equals("red")) return true; 
		if(color.equals("black")) return true;
		return false;
	}
	
	public static boolean isTrueCommand(Vector<String> drawing_commands) {
		for (int i = 0; i < drawing_commands.size(); i++) {
 			switch (getPredicate(drawing_commands.get(i)))
	 			
	 		{
				case "draw_line":  continue;
				case "line_width": continue;
				case "line_cap": continue;
				case "line_color": continue;
				case "draw_quad_curve": continue;
				case "draw_bezier_curve": continue;
				case "draw_arc_curve": continue;
				case "draw_text": continue;
				case "text_font": continue;
				case "text_align": continue;
				case "text_color": continue;
	 		} 
	 	  	System.out.println("Error 0.0: "+getPredicate(drawing_commands.get(i))+" is not a drawing command");
	 	  	return false;	
		}
		return true;
	}
	public static boolean isTrueVisual(Vector<String> vizAtoms) {
				
		for(int i=0;i<vizAtoms.size();i++){
			//System.out.println(getPredicate(vizAtoms.get(i)));
			
			if (getPredicate(vizAtoms.get(i)).equals("draw_line")){	
				
				if(elementNum(vizAtoms.get(i))!=5){
					System.out.println("Error 1.1");
					return false;
				}		
			
				if(toInt(getElement(vizAtoms.get(i),2))>500 || toInt(getElement(vizAtoms.get(i),2))<0){
					System.out.println("Error 2.1.1");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),3))>500 || toInt(getElement(vizAtoms.get(i),3))<0){
					System.out.println("Error 2.1.2");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),4))>500 || toInt(getElement(vizAtoms.get(i),4))<0){
					System.out.println("Error 2.1.3");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),5))>500 || toInt(getElement(vizAtoms.get(i),5))<0){
					System.out.println("Error 2.1.4");
					return false;
				}
			}		
			
			if (getPredicate(vizAtoms.get(i)).equals("draw_quad_curve")){	
				
				if(elementNum(vizAtoms.get(i))!=7){
					System.out.println("Error 1.2");
					return false;
				}		
			
				if(toInt(getElement(vizAtoms.get(i),2))>500 || toInt(getElement(vizAtoms.get(i),2))<0){
					System.out.println("Error 2.2.1");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),3))>500 || toInt(getElement(vizAtoms.get(i),3))<0){
					System.out.println("Error 2.2.2");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),4))>500 || toInt(getElement(vizAtoms.get(i),4))<0){
					System.out.println("Error 2.2.3");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),5))>500 || toInt(getElement(vizAtoms.get(i),5))<0){
					System.out.println("Error 2.2.4");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),6))>500 || toInt(getElement(vizAtoms.get(i),6))<0){
					System.out.println("Error 2.2.5");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),7))>500 || toInt(getElement(vizAtoms.get(i),7))<0){
					System.out.println("Error 2.2.6");
					return false;
				}

			}
			
			if (getPredicate(vizAtoms.get(i)).equals("draw_arc_curve")){	
				
				if(elementNum(vizAtoms.get(i))!=6){
					System.out.println("Error 1.3");
					return false;
				}		
			
				if(toInt(getElement(vizAtoms.get(i),2))>500 || toInt(getElement(vizAtoms.get(i),2))<0){
					System.out.println("Error 2.3.1");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),3))>500 || toInt(getElement(vizAtoms.get(i),3))<0){
					System.out.println("Error 2.3.2");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),4))>500 || toInt(getElement(vizAtoms.get(i),4))<1){
					System.out.println("Error 2.3.3");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),5))>16 || toInt(getElement(vizAtoms.get(i),5))<1){
					System.out.println("Error 2.3.4");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),6))>16 || toInt(getElement(vizAtoms.get(i),6))<1){
					System.out.println("Error 2.3.5");
					return false;
				}


			}
			
			if (getPredicate(vizAtoms.get(i)).equals("draw_text")){	
			//System.out.println("inside draw text");
				
				if(elementNum(vizAtoms.get(i))!=4){
					System.out.println("Error 1.4");
					return false;
				}		
			
				if(toInt(getElement(vizAtoms.get(i),3))>500 || toInt(getElement(vizAtoms.get(i),3))<0){
					System.out.println("Error 2.4.1");
					return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),4))>500 || toInt(getElement(vizAtoms.get(i),4))<0){
					//System.out.println(toInt(getElement(vizAtoms.get(i),4)));
					System.out.println("Error 2.4.2");
					return false;
				}
				
				

			}	
			
			if (getPredicate(vizAtoms.get(i)).equals("line_width")){	
				
				if(elementNum(vizAtoms.get(i))!=2){
					System.out.println("Error 1.5");
					return false;
				}		
			
				if(toInt(getElement(vizAtoms.get(i),2))>500 || toInt(getElement(vizAtoms.get(i),2))<1){
					System.out.println("Error 2.5.1");
					return false;
				}

			}	

			if (getPredicate(vizAtoms.get(i)).equals("line_cap")){	
				
				if(elementNum(vizAtoms.get(i))!=2){
					System.out.println("Error 1.6");
					return false;
				}		
			
				if(!isCap(getElement(vizAtoms.get(i),2)) ){
		
						System.out.println("Error 2.6.1");
						return false;
				}
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("line_color")){	
				
				if(elementNum(vizAtoms.get(i))!=2){
					System.out.println("Error 1.7");
					return false;
				}		
			
				if(!isColor(getElement(vizAtoms.get(i),2))){	
						System.out.println("Error 2.7.1");
						return false;
				}

			}
			
			if (getPredicate(vizAtoms.get(i)).equals("text_font")){	
				
				if(elementNum(vizAtoms.get(i))!=3){
					System.out.println("Error 1.8");
					return false;
				}		
			
				if(!isFontFamily(getElement(vizAtoms.get(i),3))){	
						System.out.println("Error 2.8.1");
						return false;
				}
				
				if(toInt(getElement(vizAtoms.get(i),2))>72 || toInt(getElement(vizAtoms.get(i),2))<8){
					System.out.println("Error 2.8.2");
					return false;
				}
				
				

			}
			
			if (getPredicate(vizAtoms.get(i)).equals("text_align")){	
				if(elementNum(vizAtoms.get(i))!=2){
					System.out.println("Error 1.9");
					return false;
				}		
			
				if(!isAlign(getElement(vizAtoms.get(i),2))){
						
						System.out.println("Error 2.9.1");
						return false;
				}
			}	
			
			if (getPredicate(vizAtoms.get(i)).equals("text_color")){	
				
				if(elementNum(vizAtoms.get(i))!=2){
					System.out.println("Error 1.10");
					return false;
				}		
			
				if(!isColor(getElement(vizAtoms.get(i),2))){	
						System.out.println("Error 2.10.1");
						return false;
				}

			}
			
		}
		
		

		return true;

	}			
				

	
	
	
	
	//getVizAtoms takes an arrayList answer set literals, and return a vector of string  A containing visual atoms.
	// e.g. A[0] is the first visual atoms found in S ....
	
	public static Vector<String> getVizAtoms(ArrayList<String> literals){
		
		Vector<String> vizAtoms=new Vector<String>();
		for(String literal : literals) {
			if (isVisual(literal)) {
				vizAtoms.add(literal);
			}
		}
		return vizAtoms;
	}
	
	
	public static ArrayList<AnswerSet>  parseResult(String result) {
	        DLVAnswerSetParser  parser = new DLVAnswerSetParser();
			 return parser.getAnswerSets(result);
	}
	
	
	

		
		
		
	public static Vector<String> translate_draw_text(String literal, Vector<String> vizAtoms){
		Vector<String> javaScript = new Vector<String>();
		
		for (int i=0;i<vizAtoms.size();i++){
			
			if (getPredicate(vizAtoms.get(i)).equals("text_font") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.font=\""+getElement(vizAtoms.get(i),2)+"px "+getElement(vizAtoms.get(i),3)+"\";");
				
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("text_align") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.textAlign=\""+getElement(vizAtoms.get(i),2)+"\";");
				
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("text_color") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.fillStyle=\""+getElement(vizAtoms.get(i),2)+"\";");
				
			}
	
		}
		javaScript.add("ctx.fillText(\""+getElement(literal,2)+"\","+getElement(literal,3)+","+getElement(literal,4)+");");
		return javaScript;
	}
	
	public static Vector<String> translate_quad_curve(String literal, Vector<String> vizAtoms){
		Vector<String> javaScript = new Vector<String>();
		javaScript.add("ctx.beginPath();");
		javaScript.add("ctx.moveTo("+getElement(literal,2)+","+getElement(literal,3)+");");
		javaScript.add("ctx.quadraticCurveTo("+getElement(literal,4)+","+getElement(literal,5)+","+getElement(literal,6)+","+getElement(literal,7)+");");
		for (int i=0;i<vizAtoms.size();i++){
			if (getPredicate(vizAtoms.get(i)).equals("line_width") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineWidth="+getElement(vizAtoms.get(i),2)+";");
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("line_cap") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineCap=\""+getElement(vizAtoms.get(i),2)+"\";");
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("line_color") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.strokeStyle=\""+""+getElement(vizAtoms.get(i),2)+"\";");
			}
		}
		javaScript.add("ctx.stroke();");
		return javaScript;
	}
	
	public static Vector<String> translate_arc_curve(String literal, Vector<String> vizAtoms){
		Vector<String> javaScript = new Vector<String>();
		javaScript.add("ctx.beginPath();");
		javaScript.add("ctx.moveTo("+getElement(literal,2)+","+getElement(literal,3)+");");
		
		
		int a=Integer.valueOf(getElement(literal,5));
		int b=Integer.valueOf(getElement(literal,6));
		a=a/8; b=b/8;
		
		javaScript.add("ctx.arc("+getElement(literal,2)+","+getElement(literal,3)+","+getElement(literal,4)+","+a+"*Math.PI"+","+b+"*Math.PI"+");");
		for (int i=0;i<vizAtoms.size();i++){
			if (getPredicate(vizAtoms.get(i)).equals("line_width") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineWidth="+getElement(vizAtoms.get(i),2)+";");	
			}
			if (getPredicate(vizAtoms.get(i)).equals("line_cap") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineCap=\""+getElement(vizAtoms.get(i),2)+"\";");
			}
			if (getPredicate(vizAtoms.get(i)).equals("line_color") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.strokeStyle=\""+""+getElement(vizAtoms.get(i),2)+"\";");	
			}
		}
		javaScript.add("ctx.stroke();");
		return javaScript;
	}
	
	public static Vector<String> translate_bezier_curve(String literal, Vector<String> vizAtoms){
		Vector<String> javaScript = new Vector<String>();
		javaScript.add("ctx.beginPath();");
		javaScript.add("ctx.moveTo("+getElement(literal,2)+","+getElement(literal,3)+");");
		javaScript.add("ctx.bezierCurveTo("+getElement(literal,4)+","+getElement(literal,5)+","+getElement(literal,6)+","+getElement(literal,7)+","+getElement(literal,8)+","+getElement(literal,9)+");");
		for (int i=0;i<vizAtoms.size();i++){
			if (getPredicate(vizAtoms.get(i)).equals("line_width") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineWidth="+getElement(vizAtoms.get(i),2)+";");
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("line_cap") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineCap=\""+getElement(vizAtoms.get(i),2)+"\";");
			}
			
			if (getPredicate(vizAtoms.get(i)).equals("line_color") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.strokeStyle=\""+""+getElement(vizAtoms.get(i),2)+"\";");
			}
		}
		javaScript.add("ctx.stroke();");
		return javaScript;
	}
	
	
	
	
	
	public static Vector<String> translate_draw_line(String literal, Vector<String> vizAtoms){
		Vector<String> javaScript = new Vector<String>();
		javaScript.add("ctx.beginPath();");
		javaScript.add("ctx.moveTo("+getElement(literal,2)+","+getElement(literal,3)+");");
		javaScript.add("ctx.lineTo("+getElement(literal,4)+","+getElement(literal,5)+");");
		for (int i=0;i<vizAtoms.size();i++){
			if (getPredicate(vizAtoms.get(i)).equals("line_width") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineWidth="+getElement(vizAtoms.get(i),2)+";");	
			}
			if (getPredicate(vizAtoms.get(i)).equals("line_cap") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.lineCap=\""+getElement(vizAtoms.get(i),2)+"\";");
			}
			if (getPredicate(vizAtoms.get(i)).equals("line_color") && getElement(literal,1).equals(getElement(vizAtoms.get(i),1))){
				javaScript.add("ctx.strokeStyle=\""+""+getElement(vizAtoms.get(i),2)+"\";");	
			}
		}
		javaScript.add("ctx.stroke();");
		return javaScript;
	}
	
	
	
	public static void translate(Vector<String> vizAtoms, Vector<String> drawing_commands){
		
		Vector<String> javaScript = new Vector<String>();
		
		System.out.println("<canvas id=\"myCanvas\" width=\"500\" height=\"500\" style=\"border:1px solid\">");
		System.out.println("</canvas>");
		System.out.println("<script>");
		System.out.println("var c = document.getElementById(\"myCanvas\");");
		System.out.println("var ctx = c.getContext(\"2d\");");


		
		for (int i=0;i<drawing_commands.size();i++){

			if (getPredicate(drawing_commands.get(i)).equals("draw_text")){
				
				javaScript.addAll(translate_draw_text(drawing_commands.get(i), drawing_commands));
				
				/*
				translate_draw_text(drawing_commands.get(i), drawing_commands); //this is function which returns void and 
				
																//translates drawing_commands.get(i) which is a draw_text atom.
																	/
																/we also pass the whole array drawing_commands to the function to 
																//extract the style of that specific draw_test 
																//e.g. assume the atom is draw_text(a,...)
																//you have to find all the styling predicates 
																//related to "a" by going through atoms in drawing_commands and 
																// translate darw_text(...) based on that styling. You can 
																	//
																find the information related to translation in excel 
																//file library2. Also you can find the translation algorithm 
				*/												//for each part in TranslationAlgorithm.txt file.
				
			}
			
			if (getPredicate(drawing_commands.get(i)).equals("draw_quad_curve")){
				
				javaScript.addAll(translate_quad_curve(drawing_commands.get(i), drawing_commands)); 
				

			}
			
			if (getPredicate(drawing_commands.get(i)).equals("draw_arc_curve")){
				
				javaScript.addAll(translate_arc_curve(drawing_commands.get(i), drawing_commands));  
				

			}
			
			if (getPredicate(drawing_commands.get(i)).equals("draw_line")){   
				
		
				javaScript.addAll(translate_draw_line(drawing_commands.get(i), drawing_commands));  
				
			}
			
			if (getPredicate(drawing_commands.get(i)).equals("draw_bezier_curve")){   
				
		
				javaScript.addAll(translate_bezier_curve(drawing_commands.get(i), drawing_commands));  
				
			}	
			
		} 
		
		for (int i = 0; i < javaScript.size(); i++) {
			System.out.println(javaScript.get(i));
		}
		System.out.println("</script>");		

		
	}
	
	
	
	//main
	
    public static void main(String []args) {

	
		//initialize two vectors of strings for storing vizatoms and the drawing commands
		Vector<String> vizAtoms=new Vector<String>();
		Vector<String> draw_commands=new Vector<String>();
		
		String result = computeAnswerSets(args[0]);
		ArrayList<AnswerSet> answerSets = parseResult(result);

		
		//for each answer set we get the drawing commands by calling getVizAtoms
		//therefore
		
		int numOfAS=0;
		for(AnswerSet a: answerSets) {
			
			ArrayList<String> literals = a.literals;
			
				vizAtoms=getVizAtoms(literals);
			numOfAS++;	
		}
		
		if(numOfAS>1){
			System.out.println("Warning 1: More than one answer set");
			return;
			
		}
		

		
		if(numOfAS>0 && vizAtoms.isEmpty()){
			
			System.out.println("Warning 2: The answer set for your program is either an empty set or it contains no drawing command.");
			return;	
		}
		
		if(numOfAS==0){
			System.out.println("Warning 2: No answer set");
			return;
		}
		//System.out.println("print outside");
		
		//must create new array for just the drawing commands inside the vizatoms
		draw_commands = getDrawingCommands(vizAtoms);
		//System.out.println(vizAtoms.get(0));
		//for(String command : draw_commands) System.out.println(command);

		//now  draw_commands is the vector containing all drawing commands
		//translate function takes vizAtoms and draw_commands and returns nothing, but it prints the translation of
		//commands in vizAtoms		
		if(isTrueCommand(draw_commands) && isTrueVisual(draw_commands)){
			//System.out.println("print is true");
			translate(vizAtoms, draw_commands);
			
		}
		else{
			//System.out.println("print is false");
			//isTrueVisual(vizAtoms);
		} 
			
		
		
		
	}
}

class StringListUtils {

	public static String getSeparatedList(ArrayList<String> list,String separator) {
		if(list==null)
			return null;
		StringBuilder commaSepList = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0)
				commaSepList.append(separator);
			commaSepList.append(list.get(i).toString());
		}
		return commaSepList.toString();
	}
	

	public static String getSeparatedList(HashSet<String> list,String separator) {
         return getSeparatedList(new ArrayList<String>(list),separator);
	}
	
	/**
	 * Split term into record name and its arguments
	 * @param term string representing the term
	 * @return split result or null of the term is not a record
	 */
	public static Pair<String,ArrayList<String>> splitTerm(String term) {
		if(term.indexOf('(')==-1)
			return null;
		String recordName=term.substring(0,term.indexOf('('));
		String argumentString=term.substring(term.indexOf('(')+1,term.length()-1);
		return new Pair<String,ArrayList<String>>(recordName,splitCommaSequence(argumentString));
	}
	
	public static ArrayList<String> splitCommaSequence(String argumentString) {
		ArrayList<String> arguments=new ArrayList<String>();
		int parCount=0;
		int lastBeginIndex=0;

		for(int i=1;i<argumentString.length();i++) {
			if(argumentString.charAt(i)=='(')
				parCount++;
			if(argumentString.charAt(i)==')')
				parCount--;
			if(argumentString.charAt(i)==',' && parCount==0) {
				arguments.add(argumentString.substring(lastBeginIndex, i));
				lastBeginIndex=i+1;
			}
		}
		String toAdd = argumentString.substring(lastBeginIndex,argumentString.length());
		if(toAdd.length() != 0 && !toAdd.matches("/^\\s*$/") ) {
			arguments.add(toAdd);
		}
		
		return arguments;
		
	}
}


/**
* Created with IntelliJ IDEA.
* User: iensen
* Date: 1/13/13
* Time: 7:46 PM
* To change this template use File | Settings | File Templates.
*/
class Pair<FIRST, SECOND>{

        public final FIRST first;
        public final SECOND second;

        public Pair(FIRST first, SECOND second) {
            this.first = first;
            this.second = second;
        }
}