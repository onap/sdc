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
    private static String outputFullFilePath;
    private static String outputFilePath;

    public static String getOutputFullFilePath() {
        return outputFullFilePath;
    }
    public static String getOutputFilePath() {
        return outputFilePath;
    }

    public static void setOutputFullFilePath(String outputPath) {
    	ValidationConfigManager.outputFilePath = outputPath;
        ValidationConfigManager.outputFullFilePath = outputPath+ "/reportOutput.txt";
    }

    

    public static String getCsvReportFilePath() {
        return csvReportFilePath;
    }

    public static void setCsvReportFilePath(String outputPath) {
        ValidationConfigManager.csvReportFilePath = outputPath +"/csvSummary_"+System.currentTimeMillis()+".csv";
    }

    private static String csvReportFilePath = "summary.csv";

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
