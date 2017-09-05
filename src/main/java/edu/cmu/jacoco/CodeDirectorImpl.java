package edu.cmu.jacoco;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;


public class CodeDirectorImpl implements CodeDirector{
	
	private CodeHighlighter writer;
	private FileWriter fileWriter;
	private File sourceDirectory;
	private File outputDirectory;
	
	public CodeDirectorImpl(File sourceDirectory, File reportDirectory, HTMLHighlighter htmlHighlighter) {
		this.writer = htmlHighlighter;
		
		this.sourceDirectory = sourceDirectory;

		this.outputDirectory = reportDirectory;
		try {
			this.fileWriter = new FileWriter(new File("summary.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generateClassCoverageReport(List<IBundleCoverage> bcl) {
		if (bcl == null) 
			return;
		
		if (bcl.size() == 3) {
			generateClassCoverageReport(bcl.get(0), bcl.get(1), bcl.get(2));
			
		}
		else if (bcl.size() == 2) {
			
		}
		else if (bcl.size() == 1) {
			
		}
		
	}
	
	private void generateClassCoverageReport(IBundleCoverage bca, IBundleCoverage bcb, IBundleCoverage bcu) {
		
		Iterator<IPackageCoverage> bcai = bca.getPackages().iterator();
		Iterator<IPackageCoverage> bcbi = bcb.getPackages().iterator();
		Iterator<IPackageCoverage> bcui = bcu.getPackages().iterator();
		
		Collection<ISourceFileCoverage> bcap, bcbp, bcup;
		IPackageCoverage p;
		
		while (bcai.hasNext() && bcbi.hasNext() && bcui.hasNext()) {
			p = bcai.next();
			bcap = p.getSourceFiles();
			bcbp = bcbi.next().getSourceFiles();
			bcup = bcui.next().getSourceFiles();
			/*
			bcap = p.getClasses();
			bcbp = bcbi.next().getClasses();
			bcup = bcui.next().getClasses();
			*/
			generateClassCoverageReport(p.getName(), bcap, bcbp, bcup);
				
		}
	}


	private void generateClassCoverageReport(String pkg, Collection<ISourceFileCoverage> bcac, Collection<ISourceFileCoverage> bcbc, Collection<ISourceFileCoverage> bcuc) {

		Iterator<ISourceFileCoverage> bcai = bcac.iterator();
		Iterator<ISourceFileCoverage> bcbi = bcbc.iterator();
		Iterator<ISourceFileCoverage> bcui = bcuc.iterator();
		
		while (bcai.hasNext() && bcbi.hasNext() && bcui.hasNext()) {			
			generateClassCoverageReport(pkg, bcai.next(), bcbi.next(), bcui.next());				
		}
		
	}


	private void generateClassCoverageReport(String pkg, ISourceFileCoverage bca, ISourceFileCoverage bcb, ISourceFileCoverage bcu) {
		
		int lastLine = Math.max(bca.getLastLine(), bcb.getLastLine());
		String className = pkg + "/" + bca.getName();
		String toolTip;
		
		writer.setClassName(className.replaceAll("/", "."));
				
		if (!writer.setSource(new File(this.sourceDirectory, className.replaceFirst("\\$[a-zA-Z_0-9]+", "")))) {
			return;
        }
		writer.setTarget(this.outputDirectory, pkg, className.concat(".html"));		
		
		writer.renderHeader();
		writer.renderCodePrefix();
		int coverInb = 0;
		int validLine = bcu.getLineCounter().getTotalCount();;
		for (int counter = 1; counter <= lastLine; counter++) {
			// If this is not a branch initialize the tool tip with the line coverage statistics
			if (bca.getLine(counter).getBranchCounter().getStatus() == ICounter.EMPTY) {
				toolTip = String.format("%s|%s|%s - Total: %s", 
										  bca.getLine(counter).getInstructionCounter().getCoveredCount(),
										  bcb.getLine(counter).getInstructionCounter().getCoveredCount(),
										  bcu.getLine(counter).getInstructionCounter().getCoveredCount(),
										  bcu.getLine(counter).getInstructionCounter().getTotalCount());

			} else {
				toolTip = String.format("%s|%s|%s - Total: %s", 
						  bca.getLine(counter).getBranchCounter().getCoveredCount(),
						  bcb.getLine(counter).getBranchCounter().getCoveredCount(),
						  bcu.getLine(counter).getBranchCounter().getCoveredCount(),
						  bcu.getLine(counter).getBranchCounter().getTotalCount());
				
			}
				
			coverInb += writer.renderLine(toolTip,
							  counter,
							  bca.getLine(counter).getBranchCounter().getStatus(),
							  bcb.getLine(counter).getBranchCounter().getStatus(),
							  bcu.getLine(counter).getBranchCounter().getStatus(),
							  bca.getLine(counter).getStatus(),
							  bcb.getLine(counter).getStatus(),
							  bcu.getLine(counter).getStatus());
		}
		try{
			fileWriter.append(pkg.replace("/", ".") + "," + className.replace(pkg + "/", "").replace("/", ".") + "," + coverInb + "," + validLine);
			fileWriter.append("\n");
			fileWriter.flush();
		} catch (Exception e){
			e.printStackTrace();
		}

		writer.renderTrailingLines();
		
		writer.renderCodeSuffix();
		writer.renderFooter();		
	}




}
