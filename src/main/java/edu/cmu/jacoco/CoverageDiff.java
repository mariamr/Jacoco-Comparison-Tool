package edu.cmu.jacoco;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

public class CoverageDiff {

	private final String title;

	private final File classesDirectory;
	private final File sourceDirectory;
	private final File reportDirectory;
	private File executionDataFile;

	private ExecutionDataStore executionDataStore;
	private SessionInfoStore sessionInfoStore;

	private Map<String, Map<String, ArrayList<Coverage>>> packageCoverage;
	private Coverage[] totalCoverage;
	private Writer writer;
	private CodeDirector director;

	private int numberOfTestSuites;

	final static String TOTAL_LABEL = "Total Branch Coverage";

	public CoverageDiff(final File projectDirectory, File reportDirectory, int numberOfExecFiles) {
		this.title = projectDirectory.getName();

		this.classesDirectory = new File(projectDirectory, "classes");
		this.sourceDirectory = new File(projectDirectory, "src");
		this.reportDirectory = reportDirectory;
		prepareReportDirectory();

		this.packageCoverage = new HashMap<>();
		this.numberOfTestSuites = numberOfExecFiles;
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

	public void initWriter() throws IOException {
		this.writer = new HTMLWriter(this.reportDirectory + "/index.html");
	}

	public void generateClassCoverageReport(List<IBundleCoverage> bcl) {
	    this.director.generateClassCoverageReport(bcl);
	}


	public void renderBranchCoverage(String[] testSuiteTitles, String[] packages) {

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

	@SuppressWarnings("serial")
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

	public void calculateBranchCoverage(List<IBundleCoverage> bcl) {

		Map<String, ArrayList<Coverage>> classCoverage;

		// Calculate the total branch coverage for each package and its classes
		for (IBundleCoverage bc: bcl) {
			System.out.println("calculate branch coverage " + bc.getName());
			for (IPackageCoverage p : bc.getPackages()) {
				if (packageCoverage.get(p.getName()) != null) {
					classCoverage = packageCoverage.get(p.getName());
				}
				else {
					classCoverage = new HashMap<>();
				}
				calculateBranchCoverage(p.getClasses(), classCoverage);
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


	private void calculateBranchCoverage(Collection<IClassCoverage> classes, Map<String, ArrayList<Coverage>> classCoverage) {

		Coverage coverage;
		int covered = 0;
		int total = 0;

		for (IClassCoverage c : classes) {
			coverage = calculateBranchCoverage(c);

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


	private Coverage calculateBranchCoverage(IClassCoverage c) {

		Coverage coverage = new Coverage();

		coverage.covered = c.getBranchCounter().getCoveredCount();
		coverage.total = c.getBranchCounter().getTotalCount();

		return coverage;
	}


	/*
	 * Runs Maven to merge the input exec data files
	 */
	public void mergeExecDataFiles(final String[] execDataFiles) throws IOException {

        System.out.println("merge exec files");

        ExecFileLoader execFileLoader = new ExecFileLoader();
        for (String inputFile : execDataFiles)
        {
          try (InputStream is = Files.newInputStream(Paths.get(inputFile)))
          {
            execFileLoader.load(is);
          } catch (IOException ex)
          {
            throw new RuntimeException("Error loading data from file: '" + inputFile.toString() + "'", ex);
          }
        }
        Path destFile = Paths.get("target\\jacoco.exec");
        try (OutputStream os = Files.newOutputStream(destFile))
        {
          ExecutionDataWriter executionDataWriter = new ExecutionDataWriter(os);
          execFileLoader.getSessionInfoStore().accept(executionDataWriter);
          execFileLoader.getExecutionDataStore().accept(executionDataWriter);
          executionDataWriter.flush();
        } catch (IOException ex)
        {
          throw new RuntimeException("Error writing data to file: '" + destFile.toString() + "'", ex);
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

	public List<IBundleCoverage> loadAndAnalyze(String[] execDataFiles) throws IOException {

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

	public IBundleCoverage loadAndAnalyze(File execDataFile) throws IOException {
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



