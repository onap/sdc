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
    And I want to compare the content of the entry "Artifacts/CB_BASE.yaml" in the zip "resources/downloads/VSPPackage.zip" with file "resources/yaml/CB_BASE.yaml"

    Then I want to create a VF for this Item

  Scenario: Full - Same flow for different HEAT file
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/NEW_NC_with_manifest.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"
    Then I want to create a VF for this Item

  Scenario: Test Validation Error
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/errorHeat.zip"
    And I want to process the NetworkPackage file for this VSP
    And I want to print the context data

    Then I want to check property "errors['first.env'][0].level" for value "ERROR"
    Then I want to check property "errors['first.env'][0].message" for value "ERROR: [YML1]: Invalid YAML format Problem - [empty yaml]"

  Scenario: yaml to json
    When I want to load the yaml content of the entry "CB_BASE.yaml" in the zip "resources/uploads/BASE_MUX.zip" to context
    Then I want to check property "parameters.vnf_name.description" for value "Unique name for this VF instance"