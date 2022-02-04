/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  Copyright (C) 2021 Nokia. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PackageTypeEnum;
import org.onap.sdc.backend.ci.tests.utils.general.FileHandling;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;

public final class OnboardingDataProviders {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingDataProviders.class);
    private static final String VNF_FILE_PATH = FileHandling.getPackageRepositoryPath(PackageTypeEnum.VNF);

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
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.VNF);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, VNF_FILE_PATH);
    }

    @DataProvider(name = "PNF_List", parallel = true)
    private static Object[][] pnfList() {
        return provideData(OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.PNF),
            FileHandling.getPackageRepositoryPath(PackageTypeEnum.PNF));
    }

    @DataProvider(name = "ASD_List", parallel = true)
    private static Object[][] asdList() {
        return provideData(OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.ASD),
                FileHandling.getPackageRepositoryPath(PackageTypeEnum.ASD));
    }

    @DataProvider(name = "CNF_List", parallel = true)
    private static Object[][] cnfList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.CNF);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, FileHandling.getPackageRepositoryPath(PackageTypeEnum.CNF));
    }

    @DataProvider(name = "Invalid_CNF_List", parallel = true)
    private static Object[][] invalidCnfList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getInvalidXnfNamesFileList(PackageTypeEnum.CNF);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, FileHandling.getPackageRepositoryPath(PackageTypeEnum.CNF) + File.separator + OnboardingUtils.INVALID_XNFS_SUBPATH);
    }

    @DataProvider(name = "CNF_Helm_Validator_List", parallel = true)
    private static Object[][] cnfForHelmValidatorList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.CNF_HELM);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, FileHandling.getPackageRepositoryPath(PackageTypeEnum.CNF_HELM));
    }

    @DataProvider(name = "CNF_With_Warning_Helm_Validator_List", parallel = true)
    private static Object[][] cnfWithWarningForHelmValidatorList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesWithWarningsFileList(PackageTypeEnum.CNF_HELM);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, FileHandling.getPackageRepositoryPath(PackageTypeEnum.CNF_HELM) + File.separator + OnboardingUtils.WITH_WARNINGS_XNFS_SUBPATH);
    }

    @DataProvider(name = "Invalid_CNF_Helm_Validator_List", parallel = true)
    private static Object[][] invalidCnfForHelmValidatorList() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getInvalidXnfNamesFileList(PackageTypeEnum.CNF_HELM);
        LOGGER.debug(String.format("There are %s package file(s) to test", fileNamesFromFolder.size()));
        return provideData(fileNamesFromFolder, FileHandling.getPackageRepositoryPath(PackageTypeEnum.CNF_HELM) + File.separator + OnboardingUtils.INVALID_XNFS_SUBPATH);
    }

    @DataProvider(name = "Single_VNF", parallel = true)
    private static Object[][] singleVNF() {
        final List<String> fileNamesFromFolder = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.VNF);
        final List<String> newList = new ArrayList<>();
        newList.add(fileNamesFromFolder.get(0));
        LOGGER.debug(String.format("There are %s zip file(s) to test", fileNamesFromFolder.size()));
        return provideData(newList, VNF_FILE_PATH);
    }

    @DataProvider(name = "softwareInformationPnf", parallel = true)
    private static Object[][] softwareInformationPnf() {
        final List<String> pnfPackageFileNameList = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.PNF);
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

        final String folderPath = FileHandling.getPackageRepositoryPath(PackageTypeEnum.PNF);
        final Object[][] parametersArray = new Object[1][];
        parametersArray[0] = new Object[]{folderPath, softwareInformationPnfPackage.get(),
            Arrays.asList("5gDUv18.05.201", "5gDUv18.06.205")};
        return parametersArray;
    }

    @DataProvider(name = "etsiVnfCnfOnboardPackages")
    private static Object[][] etsiVnf() {
        final List<String> vnfPackageFileNameList = OnboardingUtils.getXnfNamesFileList(PackageTypeEnum.ETSI);
        if (CollectionUtils.isEmpty(vnfPackageFileNameList)) {
            fail("Could not create etsiSingleVnfCnf datasource");
        }
        final String etsiVnfPackageName = "ETSI-VNF-SAMPLE.csar";
        final String etsiCnfPackageName = "ETSI-CNF-SAMPLE.csar";
        final List<String> etsiPackages = vnfPackageFileNameList.stream()
            .filter(packageName -> packageName.equals(etsiVnfPackageName) || packageName.equals(etsiCnfPackageName))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(etsiPackages) || etsiPackages.size() < 2) {
            fail(String.format("Could not create etsiSingleVnfCnf datasource, one of the package '%s' was not found",
                etsiPackages));
        }

        final String folderPath = FileHandling.getPackageRepositoryPath(PackageTypeEnum.ETSI);
        final Object[][] parametersArray = new Object[2][];
        parametersArray[0] = new Object[]{folderPath, etsiPackages.get(0)};
        parametersArray[1] = new Object[]{folderPath, etsiPackages.get(1)};
        return parametersArray;
    }

    @DataProvider(name = "vfcList")
    private static Object[][] vfcList() {
        final List<String> vfcFileNameList = OnboardingUtils.getVfcFilenameList();
        if (CollectionUtils.isEmpty(vfcFileNameList)) {
            fail("Could not create vfcList datasource");
        }
        final String vfc1 = "1-VFC-NetworkFunction.yaml";
        final String vfc2 = "2-VFC-NetworkService.yaml";
        final List<String> vfcFiles = vfcFileNameList.stream()
            .filter(filename -> filename.equals(vfc1) || filename.equals(vfc2))
            .collect(Collectors.toList());
        Collections.sort(vfcFiles);
        if (CollectionUtils.isEmpty(vfcFiles) || vfcFiles.size() < 2) {
            fail(String.format("Could not create vfcList datasource, one of the vfc file '%s' was not found", vfcFiles));
        }

        final String folderPath = FileHandling.getPackageRepositoryPath(PackageTypeEnum.VFC);
        final Object[][] parametersArray = new Object[2][];
        parametersArray[0] = new Object[]{folderPath, vfcFiles.get(0)};
        parametersArray[1] = new Object[]{folderPath, vfcFiles.get(1)};
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
