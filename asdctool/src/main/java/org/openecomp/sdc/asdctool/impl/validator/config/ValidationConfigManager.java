package org.openecomp.sdc.asdctool.impl.validator.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by chaya on 7/4/2017.
 */
public class ValidationConfigManager {

    private static Properties prop = new Properties();

    public static String getOutputFilePath() {
        return outputFilePath;
    }

    public static void setOutputFilePath(String outputFilePath) {
        ValidationConfigManager.outputFilePath = outputFilePath;
    }

    private static String outputFilePath;

    public static Properties setValidationConfiguration(String path){
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return prop;
    }

    public static Properties getValidationConfiguration() {
        return prop;
    }
}
