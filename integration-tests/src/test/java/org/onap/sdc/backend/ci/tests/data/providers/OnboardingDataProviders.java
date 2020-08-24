/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.sdc.backend.ci.tests.data.providers;

import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.backend.ci.tests.datatypes.enums.XnfTypeEnum;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;

public final class OnboardingDataProviders {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingDataProviders.class);
    private static final String VNF_FILE_PATH = FileHandling.getXnfRepositoryPath(XnfTypeEnum.VNF);

    private OnboardingDataProviders() {

    }

    @DataProvider(name = "randomVNF_List")
    private static Object[][] randomVnfList() {
        final int randomElementNumber = 3; //how many VNFs to onboard randomly
        final List<String> fileNamesFromFolder = OnboardingUtils.getVnfNamesFileListExcludeToscaParserFailure();
        final List<String> newRandomFileNamesFromFolder = getRandomElements(randomElementNumber, fileNamesFromFolder);
        if (CollectionUtils.isEmpty(newRandomFileNamesFromFolder)) {
            fail("Required number of VNF files not exists under " + VNF_FILE_PATH);
            return new Object[0][];
        }
        LOGGER.debug(String.format("There are %s zip file(s) to test", newRandomFileNamesFromFolder.size()));
        return provideData(newRandomFileNamesFromFolder, VNF_FILE_PATH);
    }

    @DataProvider(name = "VNF_List", parallel = true)
    private static Object[][] vnfList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, VNF_FILE_PATH);
    }

    @DataProvider(name = "PNF_List", parallel = true)
    private static Object[][] pnfList() {
        return provideData(OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.PNF),
            FileHandling.getXnfRepositoryPath(XnfTypeEnum.PNF));
    }

    @DataProvider(name = "Single_VNF", parallel = true)
    private static Object[][] singleVNF() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.VNF);
        final List<String> newList = new ArrayList<>();
        newList.add(fileNamesFromFolder.get(0));
        LOGGER.debug(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
        return provideData(newList, VNF_FILE_PATH);
    }

    @DataProvider(name = "softwareInformationPnf", parallel = true)
    private static Object[][] softwareInformationPnf() {
        final List<String> pnfPackageFileNameList = OnboardingUtils.getXnfNamesFileList(XnfTypeEnum.PNF);
        if (CollectionUtils.isEmpty(pnfPackageFileNameList)) {
            fail("Could not create softwareInformationPnf datasource");
        }
        final String pnfPackage = "sample-pnf-1.0.1-SNAPSHOT.csar";
        final Optional<String> softwareInformationPnfPackage = pnfPackageFileNameList.stream()
            .filter(pnfPackage::equals).findFirst();
        if (!softwareInformationPnfPackage.isPresent()) {
            fail(String.format("Could not create softwareInformationPnf datasource, the package '%s' was not found",
                pnfPackage));
        }

        final String folderPath = FileHandling.getXnfRepositoryPath(XnfTypeEnum.PNF);
        final Object[][] parametersArray = new Object[1][];
        parametersArray[0] = new Object[]{folderPath, softwareInformationPnfPackage.get(),
            Arrays.asList("5gDUv18.05.201", "5gDUv18.06.205")};
        return parametersArray;
    }

    private static Object[][] provideData(final List<String> fileNamesFromFolder, final String folderPath) {
        final Object[][] parametersArray = new Object[fileNamesFromFolder.size()][];
        int index = 0;
        for (final Object obj : fileNamesFromFolder) {
            parametersArray[index++] = new Object[]{folderPath, obj};
        }
        return parametersArray;
    }

    public static List<String> getRandomElements(final int randomElementNumber,
                                                 final List<String> fileNamesFromFolder) {
        if (fileNamesFromFolder.isEmpty() || fileNamesFromFolder.size() < randomElementNumber) {
            return Collections.emptyList();
        } else {
            final List<Integer> indexList = new ArrayList<>();
            final List<String> newRandomFileNamesFromFolder = new ArrayList<>();
            for (int i = 0; i < fileNamesFromFolder.size(); i++) {
                indexList.add(i);
            }
            Collections.shuffle(indexList);
            final Integer[] randomArray = indexList.subList(0, randomElementNumber)
                .toArray(new Integer[randomElementNumber]);
            for (final Integer randomNumber : randomArray) {
                newRandomFileNamesFromFolder.add(fileNamesFromFolder.get(randomNumber));
            }
            return newRandomFileNamesFromFolder;
        }
    }


}
