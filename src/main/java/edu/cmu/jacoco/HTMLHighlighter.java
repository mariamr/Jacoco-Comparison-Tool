package edu.cmu.jacoco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jacoco.core.analysis.ICounter;

public class HTMLHighlighter implements CodeHighlighter{

	private BufferedReader source;
	private BufferedWriter target;
	private String className;
	
	@Override
	public void setClassName(String name) {
		this.className = name;		
	}

	public boolean setSource(File source) {
		if (source.exists()) {
			try {
				this.source = new BufferedReader(new FileReader(source.getAbsoluteFile()));
                                return true;
                                
			} catch (FileNotFoundException e) {
				e.printStackTrace();
                                return false;
			}
		}
		else {
			System.out.println("Source does not exists " + source.getPath());
                        return false;
		}
	}

	public void setTarget(File targetDirectory, String pkg, String className) {
		try {
			
			File targetFile = new File(targetDirectory, pkg.replaceAll("/", "."));
			
			if (!targetFile.exists()) {

				Files.createDirectories(Paths.get(targetFile.getPath()));
			}
				

			className = className.replaceFirst(pkg, "");
			targetFile = new File(targetFile, className);

			
			this.target = new BufferedWriter(new FileWriter(targetFile.getAbsoluteFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void renderHeader() {
		try {
			target.write("<?xml version='1.0' encoding='UTF-8'?>");
			target.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
			target.write("<html xmlns='http://www.w3.org/1999/xhtml' lang='en'>");

			target.write("<head>");		
			
			target.write("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/>");
			target.write(String.format("<title>%s</title>", className));
			renderStyleSheets();
			renderScripts();
			
			target.write("</head>");	
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private void renderStyleSheets() {
		try {
			target.write("<link rel='stylesheet' href='../.resources/report.css' type='text/css'/>");
			target.write("<link rel='stylesheet' href='../.resources/prettify.css' type='text/css'/>");
			target.write("<link rel='stylesheet' href='../.resources/custom.css' type='text/css'/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void renderScripts() {
		try {
			target.write("<script type='text/javascript' src='../.resources/prettify.js'></script>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	@Override
	public void renderCodePrefix() {
		
		try {			
			target.write("<body onload=\"window['PR_TAB_WIDTH']=4;prettyPrint()\">");
			target.write(String.format("<h1>%s</h1>", className));
			renderLegend();
			
			target.write("<pre class='source lang-java linenums'>");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void renderFooter() {
		
		try {			
			target.write("</body>");
			target.write("</html>");
			
			// render the legend
			renderLegend();
			
			target.close();
			source.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private void renderLegend() {
		try {
			target.write("<table class='legend'>");
			target.write("<tr><td class='nc'>Not Covered</td>");
			target.write("<td class='ac'>Covered by test suite 1</td>");
			target.write("<td class='bc'>Covered by test suite 2</td>");
			target.write("<td class='uc'>Covered by the union test suite</td>");
			target.write("<td class='apc'>Partially Covered by test suite 1</td>");
			target.write("<td class='bpc'>Partially Covered by test suite 2</td>");
			target.write("<td class='upc'>Partially Covered by the union test suite</td></tr>");
			target.write("</table>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void renderCodeSuffix() {
		try {
			target.write("</pre>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void renderLine(String title, int lineNo, int aBranchStatus, int bBranchStatus, int uBranchStatus, int aLineStatus, int bLineStatus, int uLineStatus) {
		
		String line, style; 
		
		try {
			if ((line = source.readLine()) != null) {
				style = getCoverageType(aBranchStatus, bBranchStatus, uBranchStatus);
				
				style = style.equals("empty") ? getCoverageType(aLineStatus, bLineStatus, uLineStatus) : style;
				
				if (style.equals("empty")) {
					target.write(line);
					target.write("\n");
				}
				else {
					target.write(String.format("<span class='%s' id='L%d' title='%s'>", style, lineNo, title));
					target.write(line);	
					target.write("\n");	
					target.write("</span>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}			
		
	}
	
	private String getCoverageType(int aStatus, int bStatus, int uStatus) {
		
		if (uStatus == ICounter.NOT_COVERED) {
			return "nc";
		}
		else if (uStatus == ICounter.EMPTY) {
			return "empty";
		}	
		else if (aStatus == ICounter.NOT_COVERED) {
			return bStatus == ICounter.FULLY_COVERED ? "bc" : "bpc";
		}
		else if (bStatus == ICounter.NOT_COVERED) {
			return aStatus == ICounter.FULLY_COVERED ? "ac" : "apc";		
		}
		else {
			if (uStatus == ICounter.FULLY_COVERED) {
				if (aStatus == ICounter.PARTLY_COVERED && bStatus == ICounter.PARTLY_COVERED) {
					return "uc abpc";
				}
				else 
					return "uc";
			}
			else 
				return "upc";
		}
	}
	

	public void renderTrailingLines() {
		
		String line; 
		
		try {
			while((line = source.readLine()) != null) {
				target.write(line );
				target.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
