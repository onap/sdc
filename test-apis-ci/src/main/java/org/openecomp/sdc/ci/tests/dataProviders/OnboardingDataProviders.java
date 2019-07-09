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

package org.openecomp.sdc.ci.tests.dataProviders;

import org.openecomp.sdc.ci.tests.datatypes.enums.XnfTypeEnum;
import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnboardingDataProviders {

	protected static String filepath = FileHandling.getVnfRepositoryPath();
	
//	-----------------------dataProviders-----------------------------------------	
	@DataProvider(name = "randomVNF_List", parallel = false)
	private static final Object[][] randomVnfList() throws Exception {
		int randomElementNumber = 3; //how many VNFs to onboard randomly
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = getRandomElements(randomElementNumber, fileNamesFromFolder);
		System.out.println(String.format("There are %s zip file(s) to test", newRandomFileNamesFromFolder.size()));
		return provideData(newRandomFileNamesFromFolder, filepath);
	}
	
	@DataProvider(name = "VNF_List" , parallel = true)
	private static final Object[][] VnfList() throws Exception {
		
		List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
		
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
		return provideData(fileNamesFromFolder, filepath);
	}
	
//	-----------------------factories-----------------------------------------

	
	
	
//	-----------------------methods-----------------------------------------
	public static Object[][] provideData(List<String> fileNamesFromFolder, String filepath) {
		
		Object[][] arObject = new Object[fileNamesFromFolder.size()][];
		int index = 0;
		for (Object obj : fileNamesFromFolder) {
			arObject[index++] = new Object[] { filepath, obj };
		}
		return arObject;
	}
	
	private static List<String> getRandomElements(int randomElementNumber, List<String> fileNamesFromFolder) {
		if(fileNamesFromFolder.size() == 0 || fileNamesFromFolder.size() < randomElementNumber){
			return null;
		}else{
			List<Integer> indexList = new ArrayList<>();
			List<String> newRandomFileNamesFromFolder = new ArrayList<>(); 
			for(int i = 0; i < fileNamesFromFolder.size(); i++){
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			Integer[] randomArray = indexList.subList(0, randomElementNumber).toArray(new Integer[randomElementNumber]);
			for(int i = 0; i < randomArray.length; i++){
				newRandomFileNamesFromFolder.add(fileNamesFromFolder.get(randomArray[i]));
			}
			return newRandomFileNamesFromFolder;
		}
	}
	
	
	
	
}
