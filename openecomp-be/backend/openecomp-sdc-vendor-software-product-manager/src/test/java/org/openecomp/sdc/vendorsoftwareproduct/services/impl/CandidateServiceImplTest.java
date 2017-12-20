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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.ManifestCreator;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.CandidateServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Avrahamg
 * @since November 09, 2016
 */
public class CandidateServiceImplTest {
  @Mock
  private ManifestCreator manifestCreatorMock;
  @InjectMocks
  private CandidateServiceImpl candidateService;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldReturnOptionalPresentIfInputStreamIsNull() {
    assertTrue(candidateService.validateNonEmptyFileToUpload(null).isPresent());
  }

  // end validateNonEmptyFileToUpload tests
  // start validateNonEmptyFileToUpload tests
  @Test
  public void shouldReturnOptionalEmptyIfUploadedFileDataIsNotNull() {
    assertEquals(candidateService.validateRawZipData(new byte[]{}), Optional.empty());
  }

  @Test
  public void shouldReturnOptionalPresentIfUploadedFileDataIsNull() {
    assertTrue(candidateService.validateRawZipData(null).isPresent());
  }
  // end validateNonEmptyFileToUpload tests
  // start heatStructureTreeToFileDataStructure tests

  @Test
  public void shouldValidateManifestInZipMatchesFileDataStructureFromDB() {
    VspDetails vspDetails = new VspDetails("vspTest", null);
    vspDetails.setName("vspTest");
    vspDetails.setDescription("Test description");
    vspDetails.setVersion(new Version(0, 1));
    //vspDetails.setOnboardingMethod(VSPCommon.OnboardingMethod.HEAT.name());
    vspDetails.setOnboardingMethod("HEAT");

    FilesDataStructure structure =
        JsonUtil.json2Object(getExpectedJson(), FilesDataStructure.class);

    Optional<ManifestContent> expectedManifest = getExpectedManifestJson();
    doReturn(expectedManifest)
        .when(manifestCreatorMock).createManifest(vspDetails, structure);

    String expectedManifestJson = JsonUtil.object2Json(expectedManifest.get());
    String actualManifest = candidateService.createManifest(vspDetails, structure);
    Assert.assertEquals(actualManifest, expectedManifestJson);
  }

  @Test
  public void shouldReturnValidationErrorOnMissingfModule() {
    FilesDataStructure filesDataStructure = new FilesDataStructure();
    filesDataStructure.setArtifacts(Collections.singletonList("artifact.sh"));

    Optional<List<ErrorMessage>> validateErrors =
        candidateService.validateFileDataStructure(filesDataStructure);
    assertValidationErrorIsAsExpected(validateErrors, 1, Messages.NO_MODULES_IN_MANIFEST
        .getErrorMessage());
  }

  @Test
  public void shouldReturnValidationErrorOnVolumeEnvWithoutVolumeYaml() {
    FilesDataStructure filesDataStructure = new FilesDataStructure();
    Module module = new Module();
    module.setName("test");
    module.setYaml("base_file.yml");
    module.setVolEnv("vol_env.env");
    filesDataStructure.setModules(Collections.singletonList(module));

    Optional<List<ErrorMessage>> validateErrors =
        candidateService.validateFileDataStructure(filesDataStructure);
    assertValidationErrorIsAsExpected(validateErrors, 1, String.format(Messages
        .MODULE_IN_MANIFEST_VOL_ENV_NO_VOL.getErrorMessage(), module.getName()));
  }

  @Test
  public void shouldReturnValidationErrorOnModuleWithoutYaml() {
    FilesDataStructure filesDataStructure = new FilesDataStructure();
    Module module = new Module();
    module.setName("test");
    filesDataStructure.setModules(Collections.singletonList(module));

    Optional<List<ErrorMessage>> validateErrors =
        candidateService.validateFileDataStructure(filesDataStructure);
    assertValidationErrorIsAsExpected(validateErrors, 1, String.format(Messages
        .MODULE_IN_MANIFEST_NO_YAML.getErrorMessage(), module.getName()));
  }

  private void assertValidationErrorIsAsExpected(Optional<List<ErrorMessage>> validateErrors,
                                                 int errorListSize, String expectedErrorMessage) {
    if (validateErrors.isPresent()) {
      List<ErrorMessage> errorMessages = validateErrors.get();
      Assert.assertEquals(errorMessages.size(), errorListSize);
      Assert.assertEquals(errorMessages.get(0).getMessage(), expectedErrorMessage);
    }
  }

  private String getExpectedJson() {
    return "{\n" +
        "  \"modules\": [\n" +
        "    {\n" +
        "      \"isBase\": false,\n" +
        "      \"yaml\": \"file2.yaml\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"isBase\": true,\n" +
        "      \"yaml\": \"file1.yaml\",\n" +
        "      \"vol\": \"file1_vol.yaml\",\n" +
        "      \"volEnv\": \"file1.env\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"unassigned\": [\n" +
        "    \"file3.yml\"\n" +
        "  ],\n" +
        "  \"artifacts\": [\n" +
        "    \"file2.sh\"\n" +
        "  ],\n" +
        "  \"nested\": []\n" +
        "}";
  }

  private Optional<ManifestContent> getExpectedManifestJson() {
    ManifestContent mock = new ManifestContent();
    mock.setDescription("Test description");
    mock.setName("vspTest");
    mock.setVersion("0.1");


    List<FileData> mockFileData = new ArrayList<>();
    FileData fileData = createFileData("file2.yaml", false, FileData.Type.HEAT, null);
    mockFileData.add(fileData);
    fileData = createFileData("file1.yaml", true, FileData.Type.HEAT, null);
    mockFileData.add(fileData);
    fileData = createFileData("file1_vol.yaml", null, FileData.Type.HEAT_VOL, fileData);
    fileData = createFileData("file1.env", null, FileData.Type.HEAT_ENV,
        mockFileData.get(1).getData().get(0));
    mockFileData.add(createFileData("file2.sh", null, FileData.Type.OTHER, null));
    mockFileData.add(createFileData("file3.yml", null, FileData.Type.OTHER, null));
    mock.setData(mockFileData);
    return Optional.of(mock);
  }

  private FileData createFileData(String fileName, Boolean isBase, FileData.Type fileType,
                                  FileData fileDataToAddTo) {
    FileData fileData = new FileData();
    fileData.setFile(fileName);
    if (isBase != null) {
      fileData.setBase(isBase);
    }
    fileData.setType(fileType);
    addFileDataToList(fileDataToAddTo, fileData);
    return fileData;
  }

  private void addFileDataToList(FileData fileDataToAddTo, FileData fileData) {
    if (fileDataToAddTo != null) {
      List<FileData> list = fileDataToAddTo.getData();
      if (CollectionUtils.isEmpty(list)) {
        list = new ArrayList<>();
      }
      list.add(fileData);
      fileDataToAddTo.setData(list);

    }
  }

  private HeatStructureTree createHeatWithEnvAndVolIncludeVolEnv() {
    HeatStructureTree heat1 = createBasicHeatTree("file1.yaml", true, FileData.Type.HEAT);
    addEnvToHeat(heat1, "file1.env");
    HeatStructureTree heat1Vol =
        createBasicHeatTree("file1_vol.yaml", false, FileData.Type.HEAT_VOL);
    addEnvToHeat(heat1Vol, "file1_vol.env");
    heat1.addVolumeFileToVolumeList(heat1Vol);
    return heat1;
  }

  private void addEnvToHeat(HeatStructureTree toAddHeat, String envFileName) {
    HeatStructureTree heat1Env = createBasicHeatTree(envFileName, false, FileData.Type.HEAT_ENV);
    toAddHeat.setEnv(heat1Env);
  }

  private HeatStructureTree createBasicHeatTree(String fileName, boolean isBase,
                                                FileData.Type type) {
    HeatStructureTree heat = new HeatStructureTree();
    heat.setFileName(fileName);
    heat.setBase(isBase);
    heat.setType(type);
    return heat;
  }

}
