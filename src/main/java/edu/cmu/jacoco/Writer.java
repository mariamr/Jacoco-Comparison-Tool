package edu.cmu.jacoco;

import java.util.Map;


public interface Writer {
	
	public void renderHeader(String[] testSuiteTitles);
	public void renderFooter();
	
	public void renderPackageHeader(String title, String[] testSuiteTitles);
	public void renderPackageFooter();	
	
	public void renderClassHeader(String packageName, String className, boolean different);
	public void renderClassFooter();
	
	public void renderTestSuitCoverage(Coverage totalCoverage, Map<String, String> options);
	public void renderTotalCoverage(Coverage[] totalCoverage, String[] testSuiteTitles);
	
	public void renderReportEnd();
	

}
