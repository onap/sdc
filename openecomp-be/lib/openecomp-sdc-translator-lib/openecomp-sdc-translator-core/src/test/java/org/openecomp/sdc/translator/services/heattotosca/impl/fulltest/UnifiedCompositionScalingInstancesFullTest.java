package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;


import java.io.IOException;

public class UnifiedCompositionScalingInstancesFullTest extends BaseFullTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testComputesSameTypeWithOnePortEach() throws IOException {
    //1. Scenario #1 - Compute type 1 – 2 nova, each one with 1 port
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/oneComputeTypeOnePort/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/oneComputeTypeOnePort/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesTypesTwoComputesEachWithOnePort() throws IOException {
    //2. Scenario #2 - 2 compute types, each type has 2 computes with one port per compute
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/twoComputeTypesOnePort/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/twoComputeTypesOnePort/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesTypesTwoComputesEachWithOnePortWithGetAttr() throws IOException {
    //3.	Scenario #2 + between the 2 nova which has diff types there is getAttr
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/twoComputeTypesOnePortWithGetAttr/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/twoComputeTypesOnePortWithGetAttr/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortOneGroupEach() throws IOException {
    //4.	Scenario #1 + group – NovaServerGroup, connected to both nova
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/oneComputeTypeOnePortOneGroup/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances/oneComputeTypeOnePortOneGroup/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndNodesConnectedIn() throws IOException {
    //5.	Scenario #1 + node connected In – security Rule point to both port + “resource” with
    // dependency (depends_on) to both computes
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortNodeConnectedIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortNodeConnectedIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndNodesConnectedOut() throws IOException {
    //6.	Scenario#1  + node connected Out – both port connected to same network + each compute
    // connected to same volume
    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortNodeConnectedOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortNodeConnectedOut/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndGetAttrIn() throws IOException {
    /*
      7.	Scenario#1 + node get attr in – network which include
        a.	Property with getAttr from Compute1
        b.	Property with getAttr from Compute2
        c.	Property with getAttr from port
    */

    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndGetAttrOut() throws IOException {
    /*
      8. Scenario#1 + node get attr out + network
        a.	Compute1 with property1, that include getAttr from network
        b.	Compute2 with property2, that include getAttr from network
        c.	Port with property, that include getAttr from network
    */

    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrOut/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrOut/out";

    testTranslationWithInit();
  }


  @Test
  public void testComputesSameTypeWithOnePortEachAndOutputParamGetAttrIn() throws IOException {
    /*
      9.	Scenario#1 + output parameter get attr in –  3 output parameters
        a.	Output param with getAttr from Compute1
        b.	Output param with getAttr from Compute2
        c.	Output param with getAttr from Port
    */

    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortOutputParamGetAttrIn/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortOutputParamGetAttrIn/out";

    testTranslationWithInit();
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndGetAttrOutBetweenConsolidationEntities() throws
      IOException {
    /*
      10. Scenario#1 + node get attr out
        a.	Compute1 with property1, that include getAttr from port1
        b.	Compute2 with property2, that include getAttr from port2
        c.	Port1 with property, that include getAttr from compute1
        d.	Port2 with property, that include getAttr from compute2
    */

    inputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrOutComputePort/in";
    outputFilesPath =
        "/mock/services/heattotosca/fulltest/scalingInstances" +
            "/oneComputeTypeOnePortGetAttrOutComputePort/out";

    testTranslationWithInit();
  }

}
