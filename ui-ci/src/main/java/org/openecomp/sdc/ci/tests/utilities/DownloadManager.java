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

package org.openecomp.sdc.ci.tests.utilities;

import java.io.File;
import java.util.List;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;
import org.openecomp.sdc.ci.tests.execute.setup.AttFtpClient;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openqa.selenium.WebElement;

import com.aventstack.extentreports.Status;

public class DownloadManager {
	
	
	public File fetchDownloadedFile(){
		
		File retrieveLastModifiedFileFromFTP = null;
				
		if (DriverFactory.getConfig().isRemoteTesting()){
			
				
			try {
				
				AttFtpClient instance = AttFtpClient.getInstance();			   	  
				AttFtpClient.getInstance().retrieveListOfFile();
			   	retrieveLastModifiedFileFromFTP = instance.retrieveLastModifiedFileFromFTP();
								
			} catch (Exception e) {
				System.out.println("could not retriev file");
			}
			
			return retrieveLastModifiedFileFromFTP;
			
		}
		
		
		return retrieveLastModifiedFileFromFTP;
		
	}

	
	/**
	 * this method download csar file from VSP repository to default browser download directory  
	 * @param vspName
	 * @throws Exception
	 */
	public static void downloadCsarByNameFromVSPRepository(String vspName, Boolean isDelete) throws Exception{
		
		if(isDelete){
			FileHandling.cleanCurrentDownloadDir();
		}
		HomePage.showVspRepository();
		boolean vspFound = HomePage.searchForVSP(vspName);
		if (vspFound){
			ExtentTestActions.log(Status.INFO, String.format("Going to downloading VSP %s", vspName));
			List<WebElement> elemenetsFromTable = HomePage.getElemenetsFromTable();
			elemenetsFromTable.get(1).click();
			GeneralUIUtils.waitForLoader();
            GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.DOWNLOAD_CSAR.getValue());
			ExtentTestActions.log(Status.INFO, "Succeeded to downloaded CSAR file named " + vspName + " into folder " + SetupCDTest.getWindowTest().getDownloadDirectory());
            GeneralUIUtils.getElementsByCSS("div[class^='w-sdc-modal-close']").forEach(e -> e.click());
            GeneralUIUtils.ultimateWait();
		}
	}



	/*public static void downloadCsarByNameFromVSPRepository(String vspName, String vspId, Boolean isDelete) throws Exception{

		if(isDelete){
			FileHandling.cleanCurrentDownloadDir();
		}
		HomePage.showVspRepository();
		boolean vspFound = HomePage.searchForVSP(vspName);
		if (vspFound){
			ExtentTestActions.log(Status.INFO, String.format("Going to downloading VSP %s", vspName));
			List<WebElement> elemenetsFromTable = HomePage.getElemenetsFromTable();
//			GeneralUIUtils.ultimateWait();
//			WebDriverWait wait = new WebDriverWait(GeneralUIUtils.getDriver(), 5);
//			WebElement findElement = wait.until(ExpectedConditions.visibilityOf(elemenetsFromTable.get(1)));
			elemenetsFromTable.get(1).click();
//			findElement.click();
			GeneralUIUtils.waitForLoader();
			GeneralUIUtils.clickOnElementByTestId(DataTestIdEnum.ImportVfRepository.DOWNLOAD_CSAR.getValue());
    		ExtentTestActions.log(Status.INFO, "Succeeded to downloaded CSAR file named " + vspId + " into folder " + SetupCDTest.getWindowTest().getDownloadDirectory());
			GeneralUIUtils.getElementsByCSS("div[class^='w-sdc-modal-close']").forEach(e -> e.click());
			GeneralUIUtils.ultimateWait();
		}
	}*/


	public static void downloadCsarByNameFromVSPRepository(String vspName, String vspId) throws Exception{
		downloadCsarByNameFromVSPRepository(vspName, true);
	}
	
//	AttFtpClient instance = AttFtpClient.getInstance();
//	
//	 String server = "localhost";
//     int port = 2121;
//     String user = "admin";
//     String pass = "admin";
//     AttFtpClient.getInstance().init(server, port, user, pass);
//     
//     try {
//   	  AttFtpClient.getInstance().retrieveListOfFile();
//   	  
//   	  File retrieveLastModifiedFileFromFTP = instance.retrieveLastModifiedFileFromFTP();
//   	  String content = new String(Files.readAllBytes(Paths.get(retrieveLastModifiedFileFromFTP.getPath())), StandardCharsets.UTF_8);
////   	  instance.deleteFilesFromFTPserver();
//   	  System.out.println(content);
//   	  readFile(retrieveLastModifiedFileFromFTP);
//		
//	} finally {
//		instance.terminateClient();
//	}
	

}
