/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.ci.tests.api;

import org.openecomp.sdc.ci.tests.utils.rest.AutomationUtils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {

    private static ExtentReports extent;
    
    public synchronized static ExtentReports getReporter(String filePath) {
        if (extent == null) {
        	// initialize the HtmlReporter
    		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(filePath);
    		
    		// initialize ExtentReports and attach the HtmlReporter
    		extent = new ExtentReports();

    		// attach all reporters
    		extent.attachReporter(htmlReporter);

            
        }
        
        return extent;
    }
    
    public synchronized static ExtentHtmlReporter setConfiguration(ExtentHtmlReporter htmlReporter) {
    	htmlReporter.config().setTheme(Theme.STANDARD);
    	
    	htmlReporter.config().setEncoding("UTF-8");
    	
    	htmlReporter.config().setProtocol(Protocol.HTTPS);
    	
    	htmlReporter.config().setDocumentTitle("ASDC Automation Report");
    	
    	htmlReporter.config().setChartVisibilityOnOpen(true);
    	
    	htmlReporter.config().setReportName(AutomationUtils.getOSVersion());
    	
    	return htmlReporter;
    }
    
    public synchronized static ExtentReports getReporter() {
        return extent;
    }
}

