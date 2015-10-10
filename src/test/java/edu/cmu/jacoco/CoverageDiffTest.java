package edu.cmu.jacoco;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CoverageDiffTest {

    @Test
    public void testPrepareReportDirectory() throws Exception {
        CoverageDiff diff = new CoverageDiff(new File("src/main/java"), new File("target/classes"),
                new File("target/jacoco-test"), 2);
        diff.prepareReportDirectory();
        List<String> originalFileNames = new ArrayList<String>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get("src/main/resources/htmlresources"));
        for (Path path : directoryStream) {
            originalFileNames.add(path.getFileName().toString());
        }
        List<String> afterPreparationFileNames = new ArrayList<String>();
        DirectoryStream<Path> afterPreparationDirectoryStream = Files
                .newDirectoryStream(Paths.get("target/jacoco-test/.resources"));
        for (Path path : afterPreparationDirectoryStream) {
            afterPreparationFileNames.add(path.getFileName().toString());
        }
        assertThat(afterPreparationFileNames, containsInAnyOrder(originalFileNames.toArray()));
    }

}
