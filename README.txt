This repository extends the test coverage report of Jacoco by creating a comparison report. The comparison report shows the differences and similarities 
between the test coverage of two different sets of test cases.

#############################################
# Coverage report generation tool instructions
############################################

To generate the coverage report 

1. Put your configuration in the report.conf file in the project root folder.
   source :    The absolute path to the directory which has the compiled source, and the intermediate execution data files of your source code
   reports:    The absolute path to the directory where the generated report files will be copied to
   execfiles:  The path to your execution data files ex. jacoco1.exec
 
 
  Below are the optional configurations. 
  
    packages:   The comma separated name of packages to be included in the report.
	titles: 	The comma separated title of test suites, to be used in the coverage report.    

2. Put your compiled source in the "bin" directory inside your source folder


4. Run the report generation script by issuing the below command 
   ./report.sh

5. The generated HTML reports, will be saved inside the reports directory
