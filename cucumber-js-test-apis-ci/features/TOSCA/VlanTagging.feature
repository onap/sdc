Feature: Vlan Tagging - Full Flow tests

  Background: Init
    Given I want to create a VLM
    Given I want to set all Togglz to be "true"

  Scenario: Pattern 1A Full - Create and submit VSP Network Package containing one compute, one
  port and one subinterface connected to that port. Both port and subinterface are connected to
  different internal networks.
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1a/subInterfaceGetAttrInOut.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1A Full - Create and submit VSP Network Package containing one compute, one
  port and one regular nested resource subinterface connected to that port. Only port is connected
  to an internal network.
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1a/regularNestedSubinterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1A Negative - Create and submit VSP Network Package containing one compute, one
  port and multiple subinterface resource groups which are not bound to parent port. Only port is
  connected to an internal network.
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1a/negativeNotBoundToParentPort.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1B Full - Create and submit VSP Network Package containing Sub Interface and different compute type and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1b/diffCompute_SubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1B Full - Create and submit VSP Network Package containing Sub Interface and same Compute type and different Port type and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1b/diffPort_SubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1B Full - Create and submit VSP Network Package containing Sub Interface and same Compute type same Port type and different Sub interface file and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1b/diffSubInterfaceFile.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1B Full - Create and submit VSP Network Package containing Sub Interface and same Compute type different Port type and same Sub interface file and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1b/regularNestedSubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1C1 Full - Create and submit VSP Network Package containing two computes of different type, four ports of two types
  each and four subinterface resource groups of two types each and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1c1/multiplePortsMultipleVlans.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1C1 Full - Create and submit VSP Network Package containing two computes, two ports of same type
  connected to network and two subinterface resource represented through a regular nested resource and not using a
  resource group not connected to network and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1c1/regularNestedSubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1C2 Full - Create and submit VSP Network Package containing two computes of the same type,
  each one has one port with one sub interface (same type) resource group have a different count and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1c2/differentResourceGroupCount.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 1C2 Full - Create and submit VSP Network Package containing two computes of the same type,
  each one has one port with one sub interface each (same type, different network connectivity) and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern1c2/differentNetwork.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 4 Full - Create and submit VSP Network Package containing one computes, one
  port represented through a nested resource having one subinterface nested resource and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern4/Pattern_4_SinglePort_SingleSubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 4 Full - Create and submit VSP Network Package containing one computes, two
  ports represented through a nested resource both port connected to different nested subinterface
  resource and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern4/Pattern_4_MultiplePort_MultipleSubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 4 Full - Create and submit VSP Network Package containing one computes, one
  port represented through a regular nested resource and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern4/regularNestedSubInterface.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 5 Full - Create and submit VSP Network Package containing one nested
  component which is having two VFCs of different type both connected with two port and one port
  is connected with sub interface resource and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern5/Pattern_5_NestedResourceWithMultipleComputeAndPort.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Pattern 5 Full - Create and submit VSP Network Package containing different
  subinterface connectivity scenario and Create VF

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/pattern5/Pattern_5ComplexVSPWithDiffSubInfConn.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: VFC Instance Group per Network Role - Create and submit VSP Network Package of pattern 1A heat

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/vfcinstancegroup/groupPattern1aHeat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: VFC Instance Group per Network Role - Create and submit VSP Network Package of pattern 1B heat

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/vfcinstancegroup/groupPattern1bHeat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: VFC Instance Group per Network Role - Create and submit VSP Network Package of pattern 1C1 heat

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/vfcinstancegroup/groupPattern1c1Heat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: VFC Instance Group per Network Role - Create and submit VSP Network Package of pattern 1C2 heat

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/vfcinstancegroup/groupPattern1c2Heat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: VFC Instance Group per Network Role - Create and submit VSP Network Package of pattern 4 heat

    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/vlantagging/vfcinstancegroup/groupPattern4Heat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item