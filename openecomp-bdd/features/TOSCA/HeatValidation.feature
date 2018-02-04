Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM

  Scenario: Test Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/errorHeat.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['first.env'][0].level" for value "ERROR"
    Then I want to check property "errors['first.env'][0].message" for value "ERROR: [YML1]: Invalid YAML format Problem - [empty yaml]"