Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM
    Given I want to set all Togglz to be "true"

  Scenario: VirtualMachineInterface Validation Flow - Missing Refs Element Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/refs_missing.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['nested.yml'][0].level" for value "WARNING"
    Then I want to check property "errors['nested.yml'][0].message" for value:
    """
    WARNING: [VLAN2]: Parent port property virtual_machine_interface_refs is missing in VLAN Resource ID [template_Vlan_2]
    """

  Scenario: VirtualMachineInterface Validation Flow - Missing Tag Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/tag_missing.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['nested.yml'][0].level" for value "WARNING"
    Then I want to check property "errors['nested.yml'][0].message" for value:
    """
    WARNING: [VLAN2]: VLAN Tag property virtual_machine_interface_properties_sub_interface_vlan_tag is missing in VLAN Resource ID [template_Vlan_2]
    """