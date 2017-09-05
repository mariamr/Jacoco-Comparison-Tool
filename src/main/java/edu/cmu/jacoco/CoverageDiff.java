package edu.cmu.jacoco;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

public class CoverageDiff {
	
	private final String title;

	private File projectDirectory;
	private final File classesDirectory;
	private final File sourceDirectory;
	private final File reportDirectory;
	private File executionDataFile;
	private static CommandLine line;

	private ExecutionDataStore executionDataStore;
	private SessionInfoStore sessionInfoStore;

	private Map<String, Map<String, ArrayList<Coverage>>> packageCoverage;
	private Coverage[] totalCoverage;
	private Writer writer;
	private CodeDirector director;
	
	private static int numberOfTestSuites;
	
	final static String TOTAL_LABEL = "Total Line Coverage";
	private final static String ALL_PACKAGES = "all";

	public CoverageDiff(final File projectDirectory, File reportDirectory/*, int numberOfTestSuites*/) {
		this.title = projectDirectory.getName();
		
		this.projectDirectory = projectDirectory;
		this.classesDirectory = new File(projectDirectory, "classes");
		this.sourceDirectory = new File(projectDirectory, "src");
		this.reportDirectory = reportDirectory;
		prepareReportDirectory();
		
		this.packageCoverage = new HashMap<>();		
		numberOfTestSuites = getOptionValues("exec", ",").length;
		this.totalCoverage = new Coverage[numberOfTestSuites + 1];

		this.director = new CodeDirectorImpl(sourceDirectory, this.reportDirectory, new HTMLHighlighter());
	}
	
	private void prepareReportDirectory() {		

		try {
			if (!reportDirectory.exists()) {
				Files.createDirectories(Paths.get(reportDirectory.getPath()));
			}
			
			FileUtils.copyDirectory(new File(".resources"), new File(reportDirectory, ".resources"));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	
	public static void main(final String[] args) throws IOException {
		

		//if (!validateArguments(args)) return;
		
		if (!extractArguments(args)) return;

		CoverageDiff s = new CoverageDiff(new File(getOptionValue("source")), 
										  new File(getOptionValue("report"))/*, 
										   args.length - EXEC_DATA_INDEX*/);
	    IBundleCoverage bundleCoverage;
	    	    
	    // Analyze the individual test suits coverage
	    
	    String[] execDataFiles = getOptionValues("exec", ",");
		List<IBundleCoverage> bcl = s.loadAndAnalyze(execDataFiles);
	    
		// Merge the execution files and analyze the coverage
		s.mergeExecDataFiles();
		bundleCoverage = s.loadAndAnalyze(new File("./target/jacoco.exec"));		
		bcl.add(bundleCoverage);
		
		s.calculateLineCoverage(bcl);
		
		s.setWriter(new HTMLWriter(s.reportDirectory + "/index.html"));
		
		String[] testSuiteTitles = wrapTitles(getOptionValues("title", ","));
		s.renderBranchCoverage(testSuiteTitles, getOptionValues("package", ","));	
		
		s.director.generateClassCoverageReport(bcl);
		
	}
	

	private static String[] wrapTitles(String[] optionValues) {
		
		int givenTitles = optionValues.length;
		
		if (givenTitles == numberOfTestSuites) return optionValues;
		
		String[] wrapped = new String[numberOfTestSuites];
		
		System.arraycopy(optionValues, 0, wrapped, 0, givenTitles);

		for (int counter = givenTitles; counter < numberOfTestSuites; counter++) {
			wrapped[counter] = "Test Suite " + (counter + 1);
		}
		
		return wrapped;
		
	}

	private static boolean extractArguments(String[] args) {
		CommandLineParser parser = new BasicParser();

		Options options = new Options();
		boolean valid = true;
		
		options.addOption( OptionBuilder.withLongOpt( "source" )
		        .withDescription( "The directory containing the SOURCE files" )
		        .hasArg()
		        .create() );
		options.addOption( OptionBuilder.withLongOpt( "report" )
                .withDescription( "The directory that the generated REPORTs will be written to" )
                .hasArg()
                .create() );
		options.addOption( OptionBuilder.withLongOpt( "package" )
                .withDescription( "The packages that the reports will be genrated for" )
                .hasArg()
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "exec" )
                .withDescription( "The name of the Jacoco execution files" )
                .hasArg()
                .create() );

		options.addOption( OptionBuilder.withLongOpt( "title" )
                .withDescription( "The title of the test suites in the coverage report" )
                .hasArg()
                .create() );

		
		try {
		    // parse the command line arguments
			line = parser.parse( options, args );
		    
		    if( !line.hasOption( "source" )) {
		        System.out.println("You need to specify the source directory");
		        valid = false;
		    }
		    
		    if( !line.hasOption("report")) {
		        System.out.println( "You need to specify the report directory");
		        valid = false;
		    }
		    
		    if( !line.hasOption( "exec" ) ) {
		        System.out.println("You need to specify the name of the exec files.");
		        valid = false; 
		    }

		    
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		    valid = false;
		}
		
		return valid;
	}
	
	private static String getOptionValue(String option) {
		if (line.hasOption(option)) {
			return line.getOptionValue(option);
		}
		else {
			return new String();
		}
	}

	private static String[] getOptionValues(String option, String separator) {
		if (line.hasOption(option)) {
			return line.getOptionValue(option).split(separator);
		}
		else {
			return new String[0];
		}
	}

	private void renderBranchCoverage(String[] testSuiteTitles, String[] packages) {
		
		// Render the total coverage
		String packageName;
		boolean all = packages.length == 0;
		writer.renderTotalCoverage(totalCoverage, testSuiteTitles);
		
		for (Map.Entry<String, Map<String, ArrayList<Coverage>>> entry : packageCoverage.entrySet()) {
		    // Render the package level coverage by passing the package name, and the list of its classes
			packageName = entry.getKey().replaceAll("/", ".");
			
			if (all || Arrays.asList(packages).contains(packageName))				
				renderPackageBranchCoverage(packageName, entry.getValue(), testSuiteTitles);
		}
		
		writer.renderReportEnd();
	}

	private void renderPackageBranchCoverage(String packageName, Map<String, ArrayList<Coverage>> classes, String[] testSuiteTitles) {

		String className;
		writer.renderPackageHeader(packageName, testSuiteTitles);
				
		for (Map.Entry<String, ArrayList<Coverage>> entry : classes.entrySet()) {
			className = entry.getKey().replaceAll("/", ".");
			// Do not display the class coverage if the class name is "Total Coverage"	
			if (!className.equals(TOTAL_LABEL)) {				
				renderClassBranchCoverage(packageName, className, entry.getValue(), new HashMap<String, String>() {{put("bgcolor", "F7E4E4");}});
			}
		}
		
		// Render the package total branch coverage
		renderClassBranchCoverage("", TOTAL_LABEL, classes.get(TOTAL_LABEL), new HashMap<String, String>() {{put("bgcolor", "C3FAF9");}});
		
		writer.renderPackageFooter();
	}


	private void renderClassBranchCoverage(String packageName, String className, ArrayList<Coverage> coverageList, HashMap<String, String> options) {
		
		boolean different = isDifferent(className, coverageList);
		writer.renderClassHeader(packageName, className, different);
		
		for (Coverage c : coverageList) {
			writer.renderTestSuitCoverage(c, options);
		}
		
		writer.renderClassFooter();	
	}

	private void calculateLineCoverage(List<IBundleCoverage> bcl) {
		
		Map<String, ArrayList<Coverage>> classCoverage;
		
		// Calculate the total branch coverage for each package and its classes
		for (IBundleCoverage bc: bcl) {
			System.out.println("calculate line coverage " + bc.getName());
			for (IPackageCoverage p : bc.getPackages()) {	
				if (packageCoverage.get(p.getName()) != null) {
					classCoverage = packageCoverage.get(p.getName());
				}
				else {
					classCoverage = new HashMap<>();
				}
				calculateLineCoverage(p.getSourceFiles(), classCoverage);
				packageCoverage.put(p.getName(), classCoverage);		 
			 }
			
		}
		
		// Calculate the total branch coverage for each test suite
		
		for (Map.Entry<String, Map<String, ArrayList<Coverage>>> entry : packageCoverage.entrySet()) {
			classCoverage = entry.getValue();
			int counter = 0;
			
			for (Coverage c : classCoverage.get(TOTAL_LABEL)) {
				if (totalCoverage[counter] ==  null) {
					totalCoverage[counter] = new Coverage();
				}
				totalCoverage[counter].covered += c.covered;
				totalCoverage[counter++].total += c.total;
			}
		}
	}


	private void calculateLineCoverage(Collection<ISourceFileCoverage> classes, Map<String, ArrayList<Coverage>> classCoverage) {
	
		Coverage coverage;
		int covered = 0;
		int total = 0;
		
		for (ISourceFileCoverage c : classes) {
			coverage = calculateLineCoverage(c);
			
			if (classCoverage.get(c.getName()) == null) {
				classCoverage.put(c.getName(), new ArrayList<Coverage>());
			}
			classCoverage.get(c.getName()).add(coverage);
			
			covered += coverage.covered;
			total += coverage.total;
	
		}
	
		// Calculate the package coverage
		if (classCoverage.get(TOTAL_LABEL) == null) {
			classCoverage.put(TOTAL_LABEL, new ArrayList<Coverage>());
		}
		
		classCoverage.get(TOTAL_LABEL).add(new Coverage(covered, total));
			
	}


	private Coverage calculateLineCoverage(ISourceFileCoverage c) {
		
		Coverage coverage = new Coverage();
		
		coverage.covered = c.getLineCounter().getCoveredCount();
		coverage.total = c.getLineCounter().getTotalCount();
		
		return coverage;
	}


	/*
	 * Runs Maven to merge the input exec data files
	 */
	private void mergeExecDataFiles() throws IOException {
		
		System.out.println("merge exec files");
		Process p = Runtime.getRuntime().exec("mvn jacoco:merge");
	    
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
	private IBundleCoverage analyzeStructure() throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore,
				coverageBuilder);

		analyzer.analyzeAll(classesDirectory);

		return coverageBuilder.getBundle(title);
	}
	
	private void loadExecutionData() throws IOException {
		
		final FileInputStream fis = new FileInputStream(executionDataFile);
		final ExecutionDataReader executionDataReader = new ExecutionDataReader(
				fis);
		executionDataStore = new ExecutionDataStore();
		sessionInfoStore = new SessionInfoStore();

		executionDataReader.setExecutionDataVisitor(executionDataStore);
		executionDataReader.setSessionInfoVisitor(sessionInfoStore);

		while (executionDataReader.read()) {
		}

		fis.close();
	}
	
	private List<IBundleCoverage> loadAndAnalyze(String[] execDataFiles) throws IOException {
		
		List<IBundleCoverage> bcl = new ArrayList<>();
		IBundleCoverage bundleCoverage;
	    File source, dest;
	    
	    Files.createDirectories(Paths.get("./target/jacoco-execs"));
	    
	    for (int i = 0; i < execDataFiles.length; i++) {		
			
	    	//Copy the execution data files locally to prepare them for merge
	    	source = new File(execDataFiles[i]);
	    	dest = new File("./target/jacoco-execs/" + "part_" + i + ".exec"); 			
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			//Load and analyze the execution data
			bundleCoverage = loadAndAnalyze(source);	
			
			bcl.add(bundleCoverage);
		}
	    
	    return bcl;
	}
	
	private IBundleCoverage loadAndAnalyze(File execDataFile) throws IOException {
		System.out.println("load and analyze: " + execDataFile.getPath());
		executionDataFile = execDataFile;
		loadExecutionData();
		return analyzeStructure();		
		
	}


	 int getNumberOfTestSuites() {
		return numberOfTestSuites;
	}
	 
	private boolean isDifferent(String className, ArrayList<Coverage> coverage) {

		int prev = 0;
		boolean different = false;
		
		if (className.equals(TOTAL_LABEL)) return false;
		
		if (coverage != null && coverage.size() > 0) {
			prev = coverage.get(0).covered;
		}
		
		for (Coverage c : coverage) {
			if (c.covered != prev) {
				different = true;
				break;
			}
			
		} 
		
		return different;
	}


}

class Coverage {
    
	int covered;
    int total;
    
    public Coverage() {
    	covered = 0;
    	total = 0;
    }
	
	public Coverage(int covered, int total) {
		this.covered = covered;
		this.total = total;
	}
}



