Feature: Example Rest Calls
  Scenario: Call Rest CRUD

#    Given Server host "localhost"
    Given I want to create a VLM
    Given I want to create a VSP with onboarding type "Manual"

    # do an update
    Then I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"

    # dealing with getting to the correct input data from the request
    Then I want to remove "id" from the input data
    Then I want to remove "version" from the input data
    Then I want to remove "candidateOnboardingOrigin" from the input data
    Then I want to remove "onboardingOrigin" from the input data
    Then I want to remove "onboardingMethod" from the input data
    Then I want to update the input property "description" with value "updated"
    Then I want to update for path "/vendor-software-products/{item.id}/versions/{item.versionId}" with the input data from the context

    # do a create
    Then I want to create input data
    Then I want to update the input property "name" with a random value
    Then I want to create for path "/vendor-software-products/{item.id}/versions/{item.versionId}/processes" with the input data from the context
    Then I want to copy to property "lastProcessId" from response data path "value"
    # do a delete
    Then I want to delete for path "/vendor-software-products/{item.id}/versions/{item.versionId}/processes" with the value from saved property "lastProcessId"

    When I want to set property "lastProcessId" to value "NotExisting"
    Then I want the following to fail
    When I want to delete for path "/vendor-software-products/{item.id}/versions/{item.versionId}/processes" with the value from saved property "lastProcessId"

  Scenario: Create VLM through commands
    When I want to set the input data to file "resources/json/createVLM.json"
    Then I want to update the input property "vendorName" with a random value
    Then I want to create for path "/vendor-license-models" with the input data from the context
