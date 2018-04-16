Feature: Unique value

  Scenario: List unique types
    When I want to get path "/unique-types"
    Then I want to check property "listCount" for value 2
    And I want to check property "results[0]" for value "VspName"
    And I want to check property "results[1]" for value "VlmName"

  Scenario: Get unique value - non existing type (negative)
    Given I want the following to fail with error code "UNIQUE_TYPE_NOT_FOUND"
    When I want to get path "/unique-types/nonExistingType/values/someValue"

  Scenario: Get unique value - non existing VLM name
    When I want to get path "/unique-types/VlmName/values/nonExistingName"
    Then I want to check property "occupied" to be false

  Scenario: Get unique value - non existing VSP name
    When I want to get path "/unique-types/VspName/values/nonExistingName"
    Then I want to check property "occupied" to be false

  Scenario: Get unique value - existing VLM name
    Given I want to create a VLM

    When I want to get path "/unique-types/VlmName/values/{vlm.name}"
    Then I want to check property "occupied" to be true

  Scenario: Get unique value - existing VSP name
    Given I want to create a VLM
    And I want to create a VSP with onboarding type "NetworkPackage"
    And I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"

    When I want to get path "/unique-types/VspName/values/{responseData.name}"
    Then I want to check property "occupied" to be true