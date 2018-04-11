package org.openecomp.onboarding.build.test;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class StaleCodeDetectionTest {

    private static final String JAVA_EXT = ".java";
    private static final String CLASS_EXT = ".class";

    @Test
    public void checkIfStale(){

        String moduleLocation = System.getProperty("basedir");
        String compiledTestLocation = moduleLocation+File.separator+"target"+File.separator+"test-classes";
        String javaSourceLocation =  moduleLocation+File.separator+"src"+File.separator+"test"+File.separator+"java";

        File compiledFiles = new File(compiledTestLocation);
        File[] list = compiledFiles.listFiles((dir, file) -> file.endsWith(CLASS_EXT));
        if (list==null || list.length==0){
            return;
        }
        File candidate = Collections.min(Arrays.asList(list), (file1, file2)->file1.lastModified()>=file2.lastModified()?1:-1);
        String sourceFilePath = javaSourceLocation + candidate.getAbsolutePath().replace(compiledTestLocation, "").replace(CLASS_EXT,JAVA_EXT);
        if (!Files.exists(Paths.get(sourceFilePath))){
            Assert.fail("****** Please remove 'target' directory manually under path " + moduleLocation);
        }
    }
}
