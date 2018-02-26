Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM
    Given I want to set all Togglz to be "true"

  Scenario: Test Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/all-violations.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['main.yml'][4].level" for value "ERROR"
    Then I want to check property "errors['main.yml'][4].message" for value:
    """
  ERROR: [VlAN_G1]: VLAN Resource will not be translated as the VLAN Sub-interface [test_Vlan1] is not modeled as resource group
    """


    Then I want to check property "errors['main.yml'][5].level" for value "ERROR"
    Then I want to check property "errors['main.yml'][5].message" for value:
    """
    ERROR: [VlAN_G2]: There should not be any Compute Server Node, Port, Parent Port in nested file [main.yml]
"""

    Then I want to check property "errors['main.yml'][6].level" for value "WARNING"
    Then I want to check property "errors['main.yml'][6].message" for value:
    """
  WARNING: [VlAN_G3]: Network role associated with VLAN Sub-interface id[test_Vlan1] is not following the naming convention
    """