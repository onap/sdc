package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata.TestConstants;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;

public class FunctionTranslationGetAttrImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testTranslateGetAtt() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUC/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUC/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_GET_ATTR_FOR_MORE_THAN_ONE_ATTR_IN_ATTR_LIST);
  }

  @Test
  public void testTranslateGetAttUnsupportedResource() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUnsupportedResource/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUnsupportedResource/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_IGNORE_GET_ATTR_FROM_OUTPUT);
  }

  @Test
  public void testTranslateGetAttUnsupportedAttr() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUnsupportedAttr/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrUnsupportedAttr/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_GET_ATTR_FOR_NOT_SUPPORTED_ATTR_IN_ATTR_LIST);
  }

  @Test
  public void testTranslateGetAttNestedAttr() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttrNestedAtt/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttrNestedAtt/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateGetAttDynamicParam() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttrDynamicParam/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttrDynamicParam/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_OUTPUT_GET_ATTR);
  }

  @Test
  public void testTranslateGetAttOnlyResourceName() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrOnlyResourceName/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/getAttr/getAttrOnlyResourceName/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_GET_ATTR_FOR_ONLY_RESOURCE_NAME);
  }

  @Test
  public void testTranslateGetAttNonePortOrCompute() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/buildconsolidationdata/getattribute/noneToPortOrCompute/inputs";
    outputFilesPath =
        "/mock/services/heattotosca/buildconsolidationdata/getattribute/noneToPortOrCompute/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
    validateGetAttribute(TestConstants.TEST_GET_ATTR_FOR_NONE_TO_PORT_OR_COMPUTE);
  }

  @Test
  public void testTranslateDynamicGetAttrWithEmptyMapDefaultValue() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/getAttrDynamicParamEmptyMap/inputs";
    outputFilesPath =
        "/mock/services/heattotosca/getAttrDynamicParamEmptyMap/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

}