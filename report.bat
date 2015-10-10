@echo off

rem parse report.conf
FOR /F "tokens=1,2 delims==" %%G IN (report.conf) DO (set %%G=%%H)

rem removing double qoutes
set source=%source:"=%
set reports=%reports:"=%
set execfile[0]=%execfile[0]:"=%
set execfile[1]=%execfile[1]:"=%
set packages=%packages:"=%
set titles=%titles:"=%

IF NOT "%packages%"=="" SET package=--package %packages%
IF NOT "%titles%"=="" SET title=--title %titles%


echo 'mvn clean compile exec:java -Dexec.mainClass="edu.cmu.jacoco.cli.JacocoComparisonTool" -Dexec.args="--source %source% --report %reports% --exec %execfile[0]%,%execfile[1]% %package% %title%"'
mvn clean compile exec:java -Dexec.mainClass="edu.cmu.jacoco.cli.JacocoComparisonTool" -Dexec.args="--source %source% --report %reports% --exec %execfile[0]%,%execfile[1]% %package% %title%"


