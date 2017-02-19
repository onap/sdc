package org.openecomp.sdc.uici.tests.execute.vf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.uici.tests.execute.base.SetupCDTest;
import org.openecomp.sdc.uici.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.uici.tests.utilities.OnboardUtility;
import org.openecomp.sdc.uici.tests.utilities.ResourceUIUtils;
import org.testng.annotations.Test;

import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import com.google.gson.GsonBuilder;

public class VfOnboardingTests extends SetupCDTest {

	@Test
	public void testUpdateVfCreatedOnBoarding() {
		// create vf
		ResourceReqDetails importVfResourceInUI = ResourceUIUtils.importVfFromOnBoardingModalWithoutCheckin(getUser(),
				"mock_vf");
		// update vf
		ResourceUIUtils.updateVfCsarFromOnBoarding();
	}

	@Test
	public void createVfsFromOnboarding() throws IOException {
		String folderPath = "C:\\onboardingTest\\onBoardingZips";
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		List<String> zipFileNames = Arrays.asList(listOfFiles).stream().map(file -> file.getName())
				.filter(fileName -> fileName.endsWith(".zip")).collect(Collectors.toList());
		Map<String, String> filesSuccessMap = new HashMap<>();
		for (String fileName : zipFileNames) {
			try {
				createSingleVfFromOnboarding(folderPath, fileName);
				filesSuccessMap.put(fileName, "SUCCESS");
			} catch (Exception e) {
				filesSuccessMap.put(fileName, "FAIL");
			}
		}
		Path file = Paths.get("RunResults.txt");
		String stringDataModel = new GsonBuilder().setPrettyPrinting().create().toJson(filesSuccessMap);
		Files.write(file, stringDataModel.getBytes());
	}

	private static void createSingleVfFromOnboarding(String filePath, String zipFileName) {
		String userId = UserRoleEnum.DESIGNER.getUserId();
		OnboardUtility.createVfFromOnboarding(userId, zipFileName, filePath);
		GeneralUIUtils.submitForTestingElement("Vf From Onboarding");

	}

}
