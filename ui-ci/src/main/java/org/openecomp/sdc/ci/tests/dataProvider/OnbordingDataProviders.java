package org.openecomp.sdc.ci.tests.dataProvider;

import org.openecomp.sdc.ci.tests.execute.sanity.ToscaValidationTest;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utils.general.OnboardingUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnbordingDataProviders {

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
		
		List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileList();

		System.out.println(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
		return provideData(fileNamesFromFolder, filepath);
	}

	@DataProvider(name = "updateList")
	private static final Object[][] updateList() throws Exception {

		Object[][] objectArr = new Object[2][];

		Object[][] filteredArObject = null;

		objectArr[0] = new Object[]{ "1-2016-20-visbc3vf-(VOIP)_v2.1.zip", "2-2016-20-visbc3vf-(VOIP)_v2.1_RenameResourceToShay.zip" };
		objectArr[1] = new Object[]{ "1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip", "2-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0_Added2TestParameters.zip" };

		filteredArObject = OnboardingUtils.filterObjectArrWithExcludedVnfs(objectArr);

		return filteredArObject;


	}

	//	-----------------------factories-----------------------------------------
	@Factory(dataProvider = "VNF_List")
	public Object[] OnbordingDataProviders(String filepath, String vnfFile){
		return new Object[] { new ToscaValidationTest(filepath, vnfFile)};
	}
	
	
	
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


//	private static String[][] arrangeFilesVersionPairs(List<String> filesArr) {
//		String[][] filesArrangeByPairs = null;
//
//		List<String> versionOneFiles= null;
//		List<String> versionTowFiles= null;
//
//		for ( String fileName : filesArr )
//		{
//			if(fileName.startsWith("1-"))
//			{
//				versionOneFiles.add(fileName);
//			}
//			else if(fileName.startsWith("2-"))
//			{
//				versionTowFiles.add(fileName);
//			}
//		}
//
//		Collections.sort(versionOneFiles);
//		Collections.sort(versionTowFiles);
//
//		for (int i=0 ; i<versionOneFiles.size() ; i++ )
//		{
//			for (int j=0 ; j<versionTowFiles.size() ; j++ )
//			{
//
//			}
//		}
//
//		return filesArrangeByPairs;
//	}

}
