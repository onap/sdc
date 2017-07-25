package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.apache.commons.lang.text.StrBuilder;
import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by chaya on 7/5/2017.
 */
public class ReportManager {

    private static List<ValidationTaskResult> taskResults;
    private static String reportOutputFilePath;
    private static Map<String, Set<String>> failedVerticesPerTask = new HashMap<>();

    public ReportManager() {
        try {
            taskResults = new ArrayList<>();
            // open file for first time
            reportOutputFilePath = ValidationConfigManager.getOutputFilePath();
            StrBuilder sb = new StrBuilder();
            sb.appendln("-----------------------Validation Tool Results:-------------------------");
            Files.write(Paths.get(reportOutputFilePath), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addFailedVertex (String taskName, String vertexId) {
        Set<String> failedVertices = failedVerticesPerTask.get(taskName);
        if (failedVertices == null) {
            failedVertices = new HashSet<>();
        }
        failedVertices.add(vertexId);
        failedVerticesPerTask.put(taskName, failedVertices);
    }

    public static void reportValidationTaskStatus(GraphVertex vertexScanned, String taskName, String taskResultMessage, boolean success) {
        taskResults.add(new ValidationTaskResult(vertexScanned, taskName, taskResultMessage, success));
        printValidationTaskStatus(vertexScanned, taskName, success);
    }

    private static void printValidationTaskStatus(GraphVertex vertexScanned, String taskName, boolean success) {
        String successStatus = success ? "success" : "failed";
        String line = "-----------------------Vertex: "+vertexScanned.getUniqueId()+", Task " + taskName + " " +successStatus+"-----------------------";
        StrBuilder sb = new StrBuilder();
        writeReportLineToFile(sb.appendNewLine().toString());
        sb.appendln(line);
        sb.appendNewLine();
        writeReportLineToFile(line);
    }

    public static void reportValidationTaskSummary(TopologyTemplateValidationTask task, int numOfFailedComponents, int numOfSuccessComponents) {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine();
        sb.appendln("-----------------------Task " + task.getTaskName() + " Validation Summary-----------------------");
        sb.appendln("Num of failed components: "+ numOfFailedComponents);
        sb.appendln("Num of success components: "+ numOfSuccessComponents);
        sb.appendln("Total components scanned: " + numOfFailedComponents+numOfSuccessComponents);
        writeReportLineToFile(sb.toString());
    }

    public static void writeReportLineToFile(String message) {
        try {
            Files.write(Paths.get(reportOutputFilePath), new StrBuilder().appendNewLine().toString().getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(reportOutputFilePath), message.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reportValidatorTypeSummary(String validatorName, Set<String> failedTasksNames, Set<String> successTasksNames){
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine().appendNewLine();
        sb.appendln("-----------------------ValidatorExecuter " + validatorName + " Validation Summary-----------------------");
        sb.appendln("Failed tasks: "+ failedTasksNames);
        sb.appendln("Success tasks: "+ successTasksNames);
        writeReportLineToFile(sb.toString());
    }

    public static void reportStartValidatorRun(String validatorName, int componenentsNum) {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine().appendNewLine();
        sb.appendln("------ValidatorExecuter " + validatorName + " Validation Started, on "+componenentsNum+" components---------");
        writeReportLineToFile(sb.toString());
    }

    public static void reportStartTaskRun(GraphVertex vertex, String taskName){
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine().appendNewLine();
        sb.appendln("-----------------------Vertex: "+vertex.getUniqueId()+", Task " + taskName + " Started-----------------------");
        writeReportLineToFile(sb.toString());
    }

    public static void reportEndOfToolRun() {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine().appendNewLine();
        sb.appendln("-----------------------------------Validator Tool Summary-----------------------------------");
        failedVerticesPerTask.forEach((taskName, failedVertices) -> {
            sb.append("Task: " + taskName);
            sb.appendNewLine();
            sb.append("FailedVertices: " + failedVertices);
            sb.appendNewLine();
        });
        writeReportLineToFile(sb.toString());
    }
}
