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

    public static void setOutputFilePath(String outputPath) {
        ValidationConfigManager.outputFilePath = outputPath+ "/reportOutput.txt";
    }

    private static String outputFilePath;

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
