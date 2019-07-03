package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;

public class ReportManagerHelper {

    private ReportManagerHelper() {
    }

    public static List<String> getReportOutputFileAsList() {
        return readFileAsList(ValidationConfigManager.getOutputFullFilePath());
    }

    public static List<String> getReportCsvFileAsList() {
        return readFileAsList(ValidationConfigManager.getCsvReportFilePath());
    }

    public static void cleanReports() {
        cleanFile(ValidationConfigManager.getCsvReportFilePath());
        cleanFile(ValidationConfigManager.getOutputFullFilePath());
    }

    private static List<String> readFileAsList(String filePath) {
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(filePath));
            return br.lines().collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    private static void cleanFile(String filePath) {
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException ignored) {

        }
    }
}
