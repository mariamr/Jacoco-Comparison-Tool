package edu.cmu.jacoco;

import java.io.File;

public interface CodeHighlighter {
	
	public void setTarget(File target, String pkg, String className);
	
	public boolean setSource(File target);
	
	public void renderHeader();
	
	public void renderCodeSuffix();
	
	public void renderCodePrefix();

	public void renderFooter();

	public void renderLine(String title, int lineNo, int aBranchStatus, int bBranchStatus, int uBranchStatus, int aLineStatus, int bLineStatus, int uLineStatus);

	public void setClassName(String name);

	public void renderTrailingLines();
}
