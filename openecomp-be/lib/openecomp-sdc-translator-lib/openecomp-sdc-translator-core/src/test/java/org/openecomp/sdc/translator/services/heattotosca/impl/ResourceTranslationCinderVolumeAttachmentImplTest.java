package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ResourceTranslationCinderVolumeAttachmentImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateAllResourcesInOneFile() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/vol_att/volume_and_attach_one_file/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_att/volume_and_attach_one_file/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testVolFileIsNestedInMainHeatFile() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/vol_att/volume_file_nested_in_main_file_in_manifest/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_att/volume_file_nested_in_main_file_in_manifest/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testVolFileAsDataOfNested() throws Exception {
    inputFilesPath = "/mock/services/heattotosca/vol_att/nested_with_inner_vol/inputfiles";
    outputFilesPath = "/mock/services/heattotosca/vol_att/nested_with_inner_vol/out";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testVolFileIsParallelToMainHeatFile() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/vol_att/volume_file_parallel_to_main_file/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/vol_att/volume_file_parallel_to_main_file/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }
}