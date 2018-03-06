Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM
    Given I want to set all Togglz to be "true"

  Scenario:VirtualMachineInterface Validation Flow - Guideline Validation issues present
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/all-violations.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['nested.yml'][3].level" for value "WARNING"
    Then I want to check property "errors['nested.yml'][3].message" for value:
    """
 WARNING: [VlANG1]: VLAN Resource will not be translated as the VLAN Sub-interface [template_Vlan_2] is not modeled as resource group
    """


    Then I want to check property "errors['nested.yml'][1].level" for value "ERROR"
    Then I want to check property "errors['nested.yml'][1].message" for value:
    """
    ERROR: [VlANG2]: There should not be any Compute Server Node, Port, Parent Port in nested file [nested.yml]
"""

    Then I want to check property "errors['main.yml'][6].level" for value "WARNING"
    Then I want to check property "errors['main.yml'][6].message" for value:
    """
  WARNING: [VlANG3]: Network role associated with VLAN Sub-interface id[test_Vlan1] is not following the naming convention
    """