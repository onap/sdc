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

import java.io.File;

import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.rest.AutomationUtils;
import org.testng.ITestContext;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentXReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {
	
	private static final String VERSIONS_INFO_FILE_NAME = "versions.info";
	private static ExtentReports extent;
	private static ExtentHtmlReporter htmlReporter;
	private static ExtentXReporter extentxReporter;
	private static final String icon = "$(document).ready(function() {" +"\n"+
                    "$('.brand-logo').html('').prepend(\"<span><img src='data:image/png;base64,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' style='display: block; margin-left: auto; margin-right: auto; margin-top: 7px; width: 89px;'/></span>\").width(\"120px\").css(\"float\",\"left\").css(\"padding-left\",\"0\");$('.report-name').css(\"font-weight\",\"bold\");"+"\n"+
//                    "$('.logo-content' ).remove();"+"\n"+
//                    "$('#slide-out li:first-child').on('click', function(){ $('#charts-row').hide() }) ; $('#slide-out li:last-child').on('click', function(){ $('#charts-row').show() });"+"\n"+
//                    "$('.charts div:nth-child(2)').remove();"+"\n"+
					"})";

	public enum suiteNameXml {
		
		TESTNG_FAILED_XML_NAME("testng-failed.xml");
		
		suiteNameXml(String value) {
		        this.value = value;
		    }
		
		private String value;
		
	    public String getValue() {
	        return value;
	    }
		
	}
    
    public synchronized static ExtentReports setReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception {
    	String dbIp = Utils.getConfig().getReportDBhost();
    	int dbPort = Utils.getConfig().getReportDBport();

    	if (extent == null) {
        	extentxReporter = new ExtentXReporter(dbIp, dbPort);
    		extent = new ExtentReports();
    		initAndSetExtentHtmlReporter(filePath, htmlFile, isAppend);
			
    		if(extentxReporter.config().getReportObjectId() != null){
				setExtentXReporter(isAppend);
			}else{
				extentxReporter.stop();
			}
        }
        return extent;
    }
    
    public synchronized static void setExtentXReporter(Boolean isAppend){
    	extentxReporter.setAppendExisting(isAppend);
		extent.attachReporter(extentxReporter);
    }
    
    public synchronized static void initAndSetExtentHtmlReporter(String filePath, String htmlFile, Boolean isAppend) throws Exception{
    	htmlReporter = new ExtentHtmlReporter(filePath + htmlFile);
    	setConfiguration(htmlReporter);
		htmlReporter.setAppendExisting(isAppend);
		extent.attachReporter(htmlReporter);
    }

    public synchronized static ExtentReports getReporter() {
        return extent;
    }

	public static void initReporter(String filepath, String htmlFile, ITestContext context) throws Exception {
		
		String onboardVersion = AutomationUtils.getOnboardVersion();
		String osVersion = AutomationUtils.getOSVersion();
		Config config = Utils.getConfig();
		String envData = config.getUrl();
		String suiteName = getSuiteName(context);
		
		if(suiteName.equals(suiteNameXml.TESTNG_FAILED_XML_NAME.getValue())){
			if (config.getUseBrowserMobProxy())
			    setTrafficCaptue(config);
			
			setReporter(filepath, htmlFile, true);
			String suiteNameFromVersionInfoFile = FileHandling.getKeyByValueFromPropertyFormatFile(filepath + VERSIONS_INFO_FILE_NAME, "suiteName");
			reporterDataDefinition(onboardVersion, osVersion, envData, suiteNameFromVersionInfoFile);
		}else{
			FileHandling.deleteDirectory(ComponentBaseTest.getReportFolder());
			FileHandling.createDirectory(filepath);
			setReporter(filepath, htmlFile, false);
			reporterDataDefinition(onboardVersion, osVersion, envData, suiteName);
			AutomationUtils.createVersionsInfoFile(filepath + VERSIONS_INFO_FILE_NAME, onboardVersion, osVersion, envData, suiteName);
		}
		
	}

	public static void reporterDataDefinition(String onboardVersion, String osVersion, String envData, String suiteNameFromVersionInfoFile) throws Exception {
		extent.setSystemInfo("Onboard Version", onboardVersion);
		extent.setSystemInfo("OS Version", osVersion);
//		extent.setSystemInfo("Host Name Address", RestCDUtils.getExecutionHostAddress());
		extent.setSystemInfo("ExecutedOn", envData);
		extent.setSystemInfo("SuiteName", suiteNameFromVersionInfoFile);
	}

	public static  String getSuiteName(ITestContext context) {
		String suitePath = context.getSuite().getXmlSuite().getFileName();
		if(suitePath != null){
			File file = new File(suitePath);
			String suiteName = file.getName();
			return suiteName;
		}
		return null;
	}
	
	public synchronized static ExtentHtmlReporter setConfiguration(ExtentHtmlReporter htmlReporter) throws Exception {
		
    	htmlReporter.config().setTheme(Theme.STANDARD);
    	htmlReporter.config().setEncoding("UTF-8");
    	htmlReporter.config().setProtocol(Protocol.HTTPS);
    	htmlReporter.config().setDocumentTitle("SDC Automation Report");
    	htmlReporter.config().setChartVisibilityOnOpen(true);
//    	htmlReporter.config().setReportName(AutomationUtils.getATTVersion());
    	htmlReporter.config().setReportName("SDC Automation Report");
    	htmlReporter.config().setChartVisibilityOnOpen(false);
    	htmlReporter.config().setJS(icon);
    	return htmlReporter;
    }
	
	public static void closeReporter(){
		extent.flush();
	}
	
	public static void setTrafficCaptue(Config config) {
		boolean mobProxyStatus = config.getUseBrowserMobProxy();
		if (mobProxyStatus){
			config.setCaptureTraffic(true);;
		}
	}	
}

