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
        if (isStale(moduleLocation+File.separator+"target"+File.separator+"test-classes", moduleLocation+File.separator+"src"+File.separator+"test"+File.separator+"java")){
            Assert.fail("****** Please remove 'target' directory manually under path " + moduleLocation);
        }
    }

    private boolean isStale(String compiledCodeLocation, String javaSourceLocation){
        File compiledFiles = new File(compiledCodeLocation);
        File[] list = compiledFiles.listFiles((dir, file) -> file.endsWith(CLASS_EXT) && file.indexOf('$')==-1);
        if (list==null || list.length==0){
            return false;
        }
        File candidate = Collections.min(Arrays.asList(list), (file1, file2)->file1.lastModified()>=file2.lastModified()?1:-1);
        String sourceFilePath = javaSourceLocation + candidate.getAbsolutePath().replace(compiledCodeLocation, "").replace(CLASS_EXT,JAVA_EXT);
        return !Paths.get(sourceFilePath).toFile().exists();
    }
}
