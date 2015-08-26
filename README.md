# Jacoco Test Coverage Comparison Tool

This repository extends the test coverage report of [Jacoco] by creating a comparison report. The comparison report shows the differences and similarities
between the test coverage of two different sets of test cases.

## Instructions
To genrate the coverage report follow the bellow steps:
- Run your test cases using Jacoco to generate the coverage report for each individual test suite. This will generate some intermediate results in a .exec file. Currently the project supports only two sets of test suites.
- Create a folder as your source folder.
- Create a "bin" directory under your source folder, and put your compiled source (jar or class files) inside that "bin" directory.
- Put your exec files under the source folder.
- Edit the configuration in the report.conf file inside the project root folder.
    - *source*: The absolute path to your source folder.
    - *reports*: The absolute path to the directory where the generated report files will be copied to
    - *execfile[0]*: The path to your first intermediate data file ex. jacoco1.exec
    - *execfile[1]*: The path to your second intermediate data file ex. jacoco2.exec
    - *packages* [optional] : The comma separated name of packages to be included in the report.
    - *titles* [optional] : The comma separated title of test suites, to be used in the coverage report.
- Run the report generation script by running ./report.sh
- The generated HTML reports, will be saved inside the reports directory

[Jacoco]:http://www.eclemma.org/jacoco/
