package edu.cmu.jacoco;

public enum CoverageType {
	
	COVERED_BY_A("ca"),
	COVERED_BY_B("cb"),
	COVERED_BY_AB("cab"),
	PARTIALLY_COVERED_BY_A("pca"),
	PARTIALLY_COVERED_BY_B("pcb"),
	PARTIALLY_COVERED_BY_AB("pcab");	
	
	private final String abreviation;
	
	CoverageType(String abreviation) {
		this.abreviation = abreviation;
	}

	@Override
	public String toString() {
		return abreviation;
	}
	
	
}
