package org.openecomp.sdc.asdctool.impl.internal.tool;

import java.io.IOException;
import java.util.Map;

import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.openecomp.sdc.asdctool.utils.ReportWriter;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

public abstract class CommonInternalTool {
    protected ReportWriter reportWriter;
    private String reportType;
    
    CommonInternalTool(String reportType){
        this.reportType = reportType;
    }
    protected ReportWriter getReportWriter() throws IOException{
        if ( reportWriter == null ){
            reportWriter = new ReportWriter(reportType); 
        }
        return reportWriter;
    }
    public void closeAll() {
        try {
            getReportWriter().close();
        } catch (IOException e) {
            ConsoleWriter.dataLine("\nFailed to close report file.");
       }
    }
    protected void printComponentInfo(Map<GraphPropertyEnum, Object> metadataProperties) {
        ConsoleWriter.dataLine("component from type", metadataProperties.get(GraphPropertyEnum.COMPONENT_TYPE));
        ConsoleWriter.dataLine("component name", metadataProperties.get(GraphPropertyEnum.NAME));
        ConsoleWriter.dataLine("component version", metadataProperties.get(GraphPropertyEnum.VERSION));
        ConsoleWriter.dataLine("component state", metadataProperties.get(GraphPropertyEnum.STATE));
        ConsoleWriter.dataLine("component is highest", metadataProperties.get(GraphPropertyEnum.IS_HIGHEST_VERSION));
        ConsoleWriter.dataLine("component is archived", metadataProperties.get(GraphPropertyEnum.IS_ARCHIVED));
    }
}
