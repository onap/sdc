package org.openecomp.sdc.uici.scripts;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.uici.tests.datatypes.CleanTypeEnum;
import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.OnboardUtility;

import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import com.google.gson.GsonBuilder;

/**
 * This Class functions to load mass zip files to vfs through onboarding.<br>
 * It uses both BE & UI APIs
 * 
 * @author mshitrit
 *
 */
public class CreateVfsFromOnboarding extends SetupCDTest {
	public static void main(String[] args) {
		CreateVfsFromOnboarding manager = new CreateVfsFromOnboarding();

		FunctionalInterfaces.swallowException(() -> manager.setEnvParameters(CleanTypeEnum.NONE.name()));
		// String folderPath = args[0];
		String folderPath = "C:\\onboardingTest\\onBoardingZips";
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		List<String> zipFileNames = Arrays.asList(listOfFiles).stream().map(file -> file.getName())
				.filter(fileName -> fileName.endsWith(".zip")).collect(Collectors.toList());
		Map<String, String> filesSuccessMap = new HashMap<>();
		for (String fileName : zipFileNames) {
			try {
				// Before
				manager.beforeState(null);
				manager.setBrowserBeforeTest();
				createSingleVfFromOnboarding(folderPath, fileName);
				filesSuccessMap.put(fileName, "SUCCESS");

			} catch (Exception e) {
				filesSuccessMap.put(fileName, "FAIL");
			} finally {
				FunctionalInterfaces.swallowException(() -> manager.afterState(null));
				manager.quitAfterTest();
			}
		}
		Path file = Paths.get("RunResults.txt");
		String stringDataModel = new GsonBuilder().setPrettyPrinting().create().toJson(filesSuccessMap);
		FunctionalInterfaces.swallowException(() -> Files.write(file, stringDataModel.getBytes()));
	}

	private static void createSingleVfFromOnboarding(String filePath, String zipFileName) {
		String userId = UserRoleEnum.DESIGNER.getUserId();
		OnboardUtility.createVfFromOnboarding(userId, zipFileName, filePath);
		GeneralUIUtils.submitForTestingElement("Vf From Onboarding");

	}
}
