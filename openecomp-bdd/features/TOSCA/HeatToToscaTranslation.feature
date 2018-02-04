Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM

  Scenario: Full - Create and submit VSP Network Package and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item
