Feature: VSP Example File

  Background: Init
    Given I want to create a VLM

  Scenario: Create and submit VSP Network Package
    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"
    When I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"
    Then I want to compare the content of the entry "Artifacts/CB_BASE.yaml" in the zip "resources/downloads/VSPPackage.zip" with file "resources/yaml/CB_BASE.yaml"
    When I want to load the yaml content of the entry "Artifacts/CB_BASE.yaml" in the zip "resources/downloads/VSPPackage.zip" to context
    Then I want to check property "parameters.vnf_name.description" for value "Unique name for this VF instance"
    When I want to load the json content of the entry "Artifacts/MANIFEST.json" in the zip "resources/downloads/VSPPackage.zip" to context
    Then I want to check property "description" for value "for testing"
  Scenario: Create VSP Manual
    When I want to create a VSP with onboarding type "Manual"
    Then I want to make sure this Item has status "Draft"

    When I want to create a VSP with onboarding type "Manual"
    Then I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"

    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components"
    Then I want to check property "listCount" for value 0

    When I want to add a component
    Then I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components"
    Then I want to check property "listCount" for value 1

    Then I want the following to fail with error code "VSP_VFC_COUNT_EXCEED"
    When I want to add a component

    Then I want to commit this Item
    Then I want the following to fail
    When I want to submit this VSP


  Scenario: VSP Questionnaire Examples
    Given I want to create a VSP with onboarding type "Manual"

    When I want to get the questionnaire for this item

    When I want to add a component
    Then I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components"
    Then I want to check property "listCount" for value 1

    Then I want to get the questionnaire for this item
    And I want to update this questionnaire with value "15" for property "general/storageDataReplication/storageReplicationSize"
    And I want to update this questionnaire

    When I want to get the questionnaire for this item
    Then I want to check this questionnaire has value "15" for property "general/storageDataReplication/storageReplicationSize"