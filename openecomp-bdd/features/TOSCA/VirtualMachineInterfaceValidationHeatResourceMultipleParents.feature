Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM
    Given I want to set all Togglz to be "true"

  Scenario: VirtualMachineInterface Validation Flow - Multiple Parent Port Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/multiple-parents.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['nested.yml'][0].level" for value "ERROR"
    Then I want to check property "errors['nested.yml'][0].message" for value:
    """
    ERROR: [VLAN1]: More than one parent port found, there should be only one parent port for a VLAN sub-interface ID [template_Vlan_2]
    """