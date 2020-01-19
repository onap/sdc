/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.asdctool.impl.internal.tool;

import org.openecomp.sdc.asdctool.utils.ConsoleWriter;
import org.openecomp.sdc.asdctool.utils.ReportWriter;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

import java.io.IOException;
import java.util.Map;

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
