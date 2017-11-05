package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifedCompositionDynamicPortsTest extends BaseFullTranslationTest {
  private static final String baseDirectory = "/mock/services/heattotosca/fulltest/dynamicPorts";
  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testDynamicPortWithDependsOn() throws IOException {
    testTranslationWithInit(
        baseDirectory + "/dynamicPortsWithDependsOn/in",
        baseDirectory + "/dynamicPortsWithDependsOn/out"
    );
  }

  @Test
  public void testDependsOnFromNovaToNestedPort() throws IOException {
    testTranslationWithInit(
        baseDirectory + "/dependsOnFromNovaToNestedPort/in",
        baseDirectory + "/dependsOnFromNovaToNestedPort/out"
    );
  }

  @Test
  public void testDependsOnFromPortToNested() throws IOException {
    testTranslationWithInit(
        baseDirectory + "/dependsOnFromPortToNested/in",
        baseDirectory + "/dependsOnFromPortToNested/out"
    );
  }

  @Test
  public void testDependsOnFromVfcToNested() throws IOException {
    testTranslationWithInit(
        baseDirectory + "/dependsOnFromVfcToNested/in",
        baseDirectory + "/dependsOnFromVfcToNested/out"
    );
  }

  @Test
  public void testDependsOnFromNestedToNested() throws IOException {
    testTranslationWithInit(
        baseDirectory + "/dependsOnFromNestedToNested/in",
        baseDirectory + "/dependsOnFromNestedToNested/out"
    );
  }
}
