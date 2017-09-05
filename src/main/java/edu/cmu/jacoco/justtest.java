package edu.cmu.jacoco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by yutian.song on 8/9/17.
 */
public class justtest {
    public static void main(String[] args) throws IOException {
        int lineinb = 0;
        try(BufferedReader br = new BufferedReader(new FileReader("/Users/yutian.song/git-enterprise/Jacoco-Comparison-Tool/summary.txt"))) {
            for(String line; (line = br.readLine()) != null; ) {
                lineinb += Integer.parseInt(line.split(":")[1]);
            }
        }
        System.out.print("total line not in test are:" + lineinb);
    }
}
