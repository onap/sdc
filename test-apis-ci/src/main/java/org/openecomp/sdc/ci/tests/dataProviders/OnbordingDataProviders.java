package org.openecomp.sdc.ci.tests.dataProviders;

import org.openecomp.sdc.ci.tests.utils.general.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertFalse;

public class OnbordingDataProviders {

	protected static String filepath = FileHandling.getVnfRepositoryPath();
	
//	-----------------------dataProviders-----------------------------------------	
	@DataProvider(name = "randomVNF_List", parallel = false)
	private static final Object[][] randomVnfList() throws Exception {
		int randomElementNumber = 3; //how many VNFs to onboard randomly
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
		List<String> newRandomFileNamesFromFolder = getRandomElements(randomElementNumber, fileNamesFromFolder);
		if(newRandomFileNamesFromFolder == null){
			assertFalse(true,"Requered number of VNF files not exists under " + filepath);
		}
		System.out.println(String.format("There are %s zip file(s) to test", newRandomFileNamesFromFolder.size()));
		return provideData(newRandomFileNamesFromFolder, filepath);
	}
	
	@DataProvider(name = "VNF_List" , parallel = true)
	private static final Object[][] VnfList() throws Exception {
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
		return provideData(fileNamesFromFolder, filepath);
	}

	@DataProvider(name = "Single_VNF" , parallel = true)
	private static final Object[][] SingleVNF() throws Exception {
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();
		List<String> newList = new ArrayList<>();
		newList.add(fileNamesFromFolder.get(0));
		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
		return provideData(newList, filepath);
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
	
	public static List<String> getRandomElements(int randomElementNumber, List<String> fileNamesFromFolder) {
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
