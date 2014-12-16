package edu.cmu.jacoco;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//import edu.cmu.jacoco.CoverageDiff.Coverage;

public class HTMLWriter implements Writer {
	
	File file;
	BufferedWriter bw;
	
	public HTMLWriter(String output) throws IOException {
		file = new File(output);
		
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		bw = new BufferedWriter(fw);
	}

	@Override
	public void renderHeader(String[] testSuiteTitles) {
		try {
			bw.write("<table width='100%' border='1' cellspacing='0'>");
			bw.write("<tr>");
			
			String s = String.format("<td>%-50s </td> <td>%20s</td><td> %20s</td><td> %20s</td><td>%-50s </td> <td>%20s</td><td> %20s</td>", 
					  "", testSuiteTitles[0], testSuiteTitles[0] + "%", testSuiteTitles[1], testSuiteTitles[1] + "%", "Union Coverage", "Union Coverage %");
			bw.write(s);
			
			bw.write("</tr>");
		} catch (IOException e) {
			
			e.printStackTrace();
		}		

	}
	
	@Override
	public void renderFooter() {
		try {
			bw.write("</table>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void renderTotalCoverage(Coverage[] totalCoverage, String[] testSuiteTitles) {		
		
		renderHeader(testSuiteTitles);
		
		renderClassHeader("", CoverageDiff.TOTAL_LABEL, false);
		
		for (Coverage c : totalCoverage) {
			renderTestSuitCoverage(c, new HashMap<String, String>() {{put("bgcolor", "C3FAF9");}});
		}
		
		renderClassFooter();	
		
		renderFooter();
	}

	@Override
	public void renderPackageHeader(String title, String[] testSuiteTitles) {
		String s;
		
		try {
			s = String.format("<p> Package: %s </p>", title);
			bw.write(s);
			
			bw.write("<table width='100%' border='1' cellspacing='0'>");
			bw.write("<tr>");
	
			s = String.format("<td>%-50s </td> <td>%20s</td><td> %20s</td><td> %20s</td><td>%-50s </td> <td>%20s</td><td> %20s</td>", 
					 		  "Class", testSuiteTitles[0], testSuiteTitles[0] + "%", testSuiteTitles[1], testSuiteTitles[1] + "%", "Union Coverage", "Union Coverage %");
			bw.write(s);
			bw.write("</tr>");
		
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void renderPackageFooter() {

		try {
			bw.write("</table>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void renderClassHeader(String packageName, String title, boolean different) {
				
		try {
			title = title.replace(packageName, "").replace(".", "");
			String path = packageName.concat("/" + title + ".java.html");
			
			if (different) {
				bw.write("<tr bgcolor='#F5F507'>");
			}
			else {
				bw.write("<tr>");
			}
			
			bw.write(String.format("<td width='300px'><a href='%100s'> %-50s</a></td>", path, title));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void renderClassFooter() {
		try {
			bw.write("</tr>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void renderTestSuitCoverage(Coverage c, Map<String, String> options) {
		
		String s = String.format("<td> %-5d of %-5d </td> <td bgcolor='#%s'> %-7.0f </td> ",
								c.covered,
								c.total,
								options.get("bgcolor"),
								c.total > 0 ? c.covered * 100 / c.total : 0.0);
		try {
			bw.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void renderReportEnd() {
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
