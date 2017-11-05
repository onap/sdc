package org.openecomp.sdc.translator.services.heattotosca.impl.fulltest;

import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseFullTranslationTest;

import java.io.IOException;

public class UnifiedCompositionScalingInstancesFullTest extends BaseFullTranslationTest {

  private static final String BASE_DIRECTORY = "/mock/services/heattotosca/fulltest/scalingInstances/";

  @Test
  public void testComputesSameTypeWithOnePortEach() throws IOException {
    //1. Scenario #1 - Compute type 1 – 2 nova, each one with 1 port
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePort");
  }

  @Test
  public void testComputesTypesTwoComputesEachWithOnePort() throws IOException {
    //2. Scenario #2 - 2 compute types, each type has 2 computes with one port per compute
    testTranslationWithInit(BASE_DIRECTORY + "twoComputeTypesOnePort");
  }

  @Test
  public void testComputesTypesTwoComputesEachWithOnePortWithGetAttr() throws IOException {
    //3.	Scenario #2 + between the 2 nova which has diff types there is getAttr
    testTranslationWithInit(BASE_DIRECTORY + "twoComputeTypesOnePortWithGetAttr");
  }

  @Test
  public void testComputesSameTypeWithOnePortOneGroupEach() throws IOException {
    //4.	Scenario #1 + group – NovaServerGroup, connected to both nova
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortOneGroup");
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndNodesConnectedIn() throws IOException {
    //5.	Scenario #1 + node connected In – security Rule point to both port + “resource” with
    // dependency (depends_on) to both computes
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortNodeConnectedIn");
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndNodesConnectedOut() throws IOException {
    //6.	Scenario#1  + node connected Out – both port connected to same network + each compute
    // connected to same volume
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortNodeConnectedOut");
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndGetAttrIn() throws IOException {
    /*
      7.	Scenario#1 + node get attr in – network which include
        a.	Property with getAttr from Compute1
        b.	Property with getAttr from Compute2
        c.	Property with getAttr from port
    */
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortGetAttrIn");
  }

  @Test
  public void testComputesSameTypeWithOnePortEachAndGetAttrOut() throws IOException {
    /*
      8. Scenario#1 + node get attr out + network
        a.	Compute1 with property1, that include getAttr from network
        b.	Compute2 with property2, that include getAttr from network
        c.	Port with property, that include getAttr from network
    */
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortGetAttrOut");
  }


  @Test
  public void testComputesSameTypeWithOnePortEachAndOutputParamGetAttrIn() throws IOException {
    /*
      9.	Scenario#1 + output parameter get attr in –  3 output parameters
        a.	Output param with getAttr from Compute1
        b.	Output param with getAttr from Compute2
        c.	Output param with getAttr from Port
    */
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortOutputParamGetAttrIn");
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
    testTranslationWithInit(BASE_DIRECTORY + "oneComputeTypeOnePortGetAttrOutComputePort");
  }

}
