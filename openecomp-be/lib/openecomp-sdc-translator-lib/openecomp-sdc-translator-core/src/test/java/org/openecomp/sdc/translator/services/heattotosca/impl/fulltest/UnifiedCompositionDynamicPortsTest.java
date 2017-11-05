package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionDynamicPortsTest extends BaseFullTranslationTest {

  private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/dynamicPorts/";

  @Test
  public void testDynamicPortWithDependsOn() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dynamicPortsWithDependsOn");
  }

  @Test
  public void testDependsOnFromNovaToNestedPort() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dependsOnFromNovaToNestedPort");
  }

  @Test
  public void testDependsOnFromPortToNested() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dependsOnFromPortToNested");
  }

  @Test
  public void testDependsOnFromVfcToNested() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dependsOnFromVfcToNested");
  }

  @Test
  public void testDependsOnFromNestedToNested() throws IOException {
    testTranslationWithInit(BASE_DIRECTORY + "dependsOnFromNestedToNested");
  }
}
