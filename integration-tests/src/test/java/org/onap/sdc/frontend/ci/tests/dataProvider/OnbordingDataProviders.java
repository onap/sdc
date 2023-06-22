/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Nokia. All rights reserved.
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

package org.onap.sdc.frontend.ci.tests.dataProvider;

import org.onap.sdc.backend.ci.tests.datatypes.enums.PackageTypeEnum;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtils;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OnbordingDataProviders {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnbordingDataProviders.class);

    private static final String VSP_VGW_CSAR = "vsp-vgw.csar";
    private static final int NUMBER_OF_RANDOMLY_ONBOARD_VNF = 3;
    protected static String filepath = FileHandling.getVnfRepositoryPath();

    //	-----------------------dataProviders-----------------------------------------
    @DataProvider(name = "randomVNF_List", parallel = false)
    private static Object[][] randomVnfList() throws Exception {
        int randomElementNumber = NUMBER_OF_RANDOMLY_ONBOARD_VNF; //how many VNFs to onboard randomly
        List<String> fileNamesFromFolder = new OnboardingUtils().getVnfNamesFileListExcludeToscaParserFailure();
        List<String> newRandomFileNamesFromFolder = getRandomElements(randomElementNumber, fileNamesFromFolder);
        return provideData(newRandomFileNamesFromFolder, filepath);
    }

    @DataProvider(name = "VNF_List", parallel = true)
    private static Object[][] VnfList() throws Exception {
        List<String> fileNamesFromFolder = new OnboardingUtils().getXnfNamesFileList(PackageTypeEnum.VNF);
        return provideData(fileNamesFromFolder, filepath);
    }

    @DataProvider(name = "CNF_List", parallel = true)
    private static Object[][] cnfList() {
        List<String> fileNamesFromFolder = new OnboardingUtils().getXnfNamesFileList(PackageTypeEnum.CNF);
        return provideData(fileNamesFromFolder, FileHandling.getCnfRepositoryPath());
    }

    @DataProvider(name = "CNF_Helm_Validator_List", parallel = true)
    private static Object[][] cnfForHelmValidatorList() {
        List<String> fileNamesFromFolder = new OnboardingUtils().getXnfNamesFileList(PackageTypeEnum.CNF_HELM);
        return provideData(fileNamesFromFolder, FileHandling.getCnfForHelmValidatorRepositoryPath());
    }

    @DataProvider(name = "CNF_With_Warning_Helm_Validator_List", parallel = true)
    private static Object[][] cnfWithWarningForHelmValidatorList() {
        List<String> fileNamesFromFolder = new OnboardingUtils().getXnfNamesWithWarningsFileList(PackageTypeEnum.CNF_HELM);
        Object[][] objects = provideData(fileNamesFromFolder,
            FileHandling.getCnfWithWarningForHelmValidatorRepositoryPath());
        return objects;
    }

    @DataProvider(name = "Invalid_CNF_Helm_Validator_List", parallel = true)
    private static Object[][] invalidCnfForHelmValidatorList() {
        List<String> fileNamesFromFolder = new OnboardingUtils().getInvalidXnfNamesFileList(PackageTypeEnum.CNF_HELM);
        return provideData(fileNamesFromFolder, FileHandling.getInvalidCnfForHelmValidatorRepositoryPath());
    }

    @DataProvider(name = "Invalid_CNF_List", parallel = true)
    private static Object[][] invalidCnfList() {
        List<String> fileNamesFromFolder = new OnboardingUtils().getInvalidXnfNamesFileList(PackageTypeEnum.CNF);
        return provideData(fileNamesFromFolder, FileHandling.getCnfRepositoryPath());
    }

    @DataProvider(name = "updateList")
    private static Object[][] updateList() throws Exception {

        Object[][] objectArr = new Object[2][];

        Object[][] filteredArObject = null;

        objectArr[0] = new Object[] {"1-2016-20-visbc3vf-(VOIP)_v2.1.zip",
                "2-2016-20-visbc3vf-(VOIP)_v2.1_RenameResourceToShay.zip"};
        objectArr[1] = new Object[] {"1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip",
                "2-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0_Added2TestParameters.zip"};

        filteredArObject = new OnboardingUtils().filterObjectArrWithExcludedVnfs(objectArr);

        return filteredArObject;


    }

    @DataProvider(name = "Single_Vsp_Test_Csar", parallel = true)
    private static Object[][] singleVspTestCsar() throws Exception {

        List<String> fileNamesFromFolder = new OnboardingUtils().getXnfNamesFileList(PackageTypeEnum.VNF);
        if (!fileNamesFromFolder.contains(VSP_VGW_CSAR)) {
            Assert.fail("Vsp Test file is not exits in the path");
        }
        return provideData(Arrays.asList(VSP_VGW_CSAR), filepath);
    }

    //	-----------------------methods-----------------------------------------
    static Object[][] provideData(List<String> fileNamesFromFolder, String filepath) {
        LOGGER.info(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
        Object[][] arObject = new Object[fileNamesFromFolder.size()][];
        int index = 0;
        for (Object obj : fileNamesFromFolder) {
            arObject[index++] = new Object[] {filepath, obj};
        }
        return arObject;
    }

    static List<String> getRandomElements(int randomElementNumber, List<String> fileNamesFromFolder) {
        if (fileNamesFromFolder.size() == 0 || fileNamesFromFolder.size() < randomElementNumber) {
            return null;
        } else {
            List<Integer> indexList = new ArrayList<>();
            List<String> newRandomFileNamesFromFolder = new ArrayList<>();
            for (int i = 0; i < fileNamesFromFolder.size(); i++) {
                indexList.add(i);
            }
            Collections.shuffle(indexList);
            Integer[] randomArray = indexList.subList(0, randomElementNumber).toArray(new Integer[randomElementNumber]);
            for (Integer integer : randomArray) {
                newRandomFileNamesFromFolder.add(fileNamesFromFolder.get(integer));
            }
            return newRandomFileNamesFromFolder;
        }
    }

}
