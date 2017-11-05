package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifedCompositionDynamicPortsTest extends BaseFullTranslationTest {
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testDynamicPortWithDependsOn() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dynamicPortsWithDependsOn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dynamicPortsWithDependsOn/out";

    testTranslationWithInit();
  }

  @Test
  public void testDependsOnFromNovaToNestedPort() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromNovaToNestedPort/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromNovaToNestedPort/out";

    testTranslationWithInit();
  }

  @Test
  public void testDependsOnFromPortToNested() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromPortToNested/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromPortToNested/out";

    testTranslationWithInit();
  }

  @Test
  public void testDependsOnFromVfcToNested() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromVfcToNested/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromVfcToNested/out";

    testTranslationWithInit();
  }

  @Test
  public void testDependsOnFromNestedToNested() throws IOException {
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromNestedToNested/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/dynamicPorts/dependsOnFromNestedToNested/out";

    testTranslationWithInit();
  }
}
