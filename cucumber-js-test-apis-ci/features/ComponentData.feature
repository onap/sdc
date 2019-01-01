Feature: Component - Test Component Composition and Questionnaire Data

  Background: Init
    Given I want to create a VLM

  Scenario: Test Component Composition and Questionnaire Data After Same Heat Reupload
    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/vMME_Ericsson_small_v2.zip"
    And I want to process the NetworkPackage file for this VSP

    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components"
    Then I want to copy to property "componentId" from response data path "results[0].id"
    Then I want to check property "listCount" for value 4
    Then I want to check property "results[0].id" exists

    #Verify composition data for first component
    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components/{componentId}"
    Then I want to check property "data.name" exists
    Then I want to check property "data.displayName" exists
    Then I want to copy to property "firstCompDisplayName" from response data path "data.displayName"
    #Ensure composition data does not have vfcCode and nfcFunction since they are moved to questionnaire
    Then I want to check property "data.vfcCode" does not exist
    Then I want to check property "data.nfcFunction" does not exist

    When I want to get the questionnaire for this component
    #Ensure questionnaire data has nfcNamingCode in "general" and populated with value of component displayName
    Then I want to check value of "general.nfcNamingCode" in the questionnaire data with value of property "firstCompDisplayName"

    #Update questionnaire nfcNamingCode and nfcFunction in "general"
    And I want to update this questionnaire with value "general/nfcNamingCode" for property "test_update_naming_code"
    And I want to update this questionnaire with value "general/nfcFunction" for property "test_function"
    And I want to update this questionnaire

    #Retrive questionnaire and verify nfcNamingCode and nfcFunction in "general" has updated value
    When I want to get the questionnaire for this component
    Then I want to check this questionnaire has value "general/nfcNamingCode" for property "test_update_naming_code"
    Then I want to check this questionnaire has value "general/nfcFunction" for property "test_function"

    #Reupload the same Heat
    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/vMME_Ericsson_small_v2.zip"
    And I want to process the NetworkPackage file for this VSP

    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}/components"
    #Find component id for which nfcNamingCode and nfcFunction were set in previous HEAT based on component display name
    Then I want to set componentId for component name in property "firstCompDisplayName"

    #Retrive questionnaire and verify nfcNamingCode and nfcFunction in "general" has retained values that were before heat upload
    When I want to get the questionnaire for this component
    Then I want to check this questionnaire has value "general/nfcNamingCode" for property "test_update_naming_code"
    Then I want to check this questionnaire has value "general/nfcFunction" for property "test_function"