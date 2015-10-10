package edu.cmu.jacoco.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jacoco.core.analysis.IBundleCoverage;

import edu.cmu.jacoco.CoverageDiff;

public class JacocoComparisonTool {

    private static CommandLine line;

    private JacocoComparisonTool() {
	// private default constructor to use only static methods
    }

    public static void main(final String[] args) throws IOException {
	if (!extractArguments(args))
	    return;

	final String[] execDataFiles = getOptionValues("exec", ",");
	final CoverageDiff s = new CoverageDiff(new File(getOptionValue("source")), new File(getOptionValue("report")),
		execDataFiles.length);

	final List<IBundleCoverage> bcl = s.loadAndAnalyze(execDataFiles);

	s.mergeExecDataFiles(execDataFiles);
	final IBundleCoverage bundleCoverage = s.loadAndAnalyze(new File("./target/jacoco.exec"));

	bcl.add(bundleCoverage);

	s.calculateBranchCoverage(bcl);

	s.initWriter();

	final String[] testSuiteTitles = wrapTitles(getOptionValues("title", ","), execDataFiles.length);
	s.renderBranchCoverage(testSuiteTitles, getOptionValues("package", ","));

	s.generateClassCoverageReport(bcl);
    }

    @SuppressWarnings("static-access")
    private static boolean extractArguments(final String[] args) {
	final CommandLineParser parser = new BasicParser();

	final Options options = new Options();
	boolean valid = true;

	options.addOption(OptionBuilder.withLongOpt("source")
		.withDescription("The directory containing the SOURCE files").hasArg().create());
	options.addOption(OptionBuilder.withLongOpt("report")
		.withDescription("The directory that the generated REPORTs will be written to").hasArg().create());
	options.addOption(OptionBuilder.withLongOpt("package")
		.withDescription("The packages that the reports will be genrated for").hasArg().create());

	options.addOption(OptionBuilder.withLongOpt("exec").withDescription("The name of the Jacoco execution files")
		.hasArg().create());

	options.addOption(OptionBuilder.withLongOpt("title")
		.withDescription("The title of the test suites in the coverage report").hasArg().create());

	try {
	    // parse the command line arguments
	    line = parser.parse(options, args);

	    if (!line.hasOption("source")) {
		System.out.println("You need to specify the source directory");
		valid = false;
	    }

	    if (!line.hasOption("report")) {
		System.out.println("You need to specify the report directory");
		valid = false;
	    }

	    if (!line.hasOption("exec")) {
		System.out.println("You need to specify the name of the exec files.");
		valid = false;
	    }

	} catch (ParseException exp) {
	    System.out.println("Unexpected exception:" + exp.getMessage());
	    valid = false;
	}

	return valid;
    }

    private static String getOptionValue(final String option) {
	if (line.hasOption(option)) {
	    return line.getOptionValue(option);
	} else {
	    return new String();
	}
    }

    private static String[] getOptionValues(final String option, final String separator) {
	if (line.hasOption(option)) {
	    return line.getOptionValue(option).split(separator);
	} else {
	    return new String[0];
	}
    }

    private static String[] wrapTitles(final String[] optionValues, final int numberOfExecFiles) {

	int givenTitles = optionValues.length;

	if (givenTitles == numberOfExecFiles)
	    return optionValues;

	final String[] wrapped = new String[numberOfExecFiles];

	System.arraycopy(optionValues, 0, wrapped, 0, givenTitles);

	for (int counter = givenTitles; counter < numberOfExecFiles; counter++) {
	    wrapped[counter] = "Test Suite " + (counter + 1);
	}

	return wrapped;

    }

}
