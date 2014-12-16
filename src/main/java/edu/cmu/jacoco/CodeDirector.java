package edu.cmu.jacoco;

import java.util.List;

import org.jacoco.core.analysis.IBundleCoverage;

public interface CodeDirector {
	

	public void generateClassCoverageReport(List<IBundleCoverage> bcl);
	

}
