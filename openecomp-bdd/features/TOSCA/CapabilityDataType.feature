Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM

  Scenario: Test Capability type in service template
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/zipWithExternalPort.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

   Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"
   And I want to compare the content of the entry "Definitions/Nested_FSB1ServiceTemplate.yaml" in the zip "resources/downloads/VSPPackage.zip" with file "resources/yaml/Nested_FSB1ServiceTemplate.yaml"

    Then I want to create a VF for this Item

    #And I want to load the yaml content of the entry "Definitions/Nested_FSB1ServiceTemplate
    # .yaml" in the zip "resources/downloads/VSPPackage.zip" to context
    #And I want to check property "topology_template.node_templates.FSB1_FSB1_OAM.capabilities"
    # for value { port_mirroring: { properties: { connection_point: [Object] } } }