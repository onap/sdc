Feature: Delete VLM

Scenario: Delete VLM with only draft
    When I want to create a VLM
    Then I want to create input data
    Then I want to update the input property "name" with a random value
    Then I want to update the input property "type" with value "Universal"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
    Then I want to copy to property "lastProcessId" from response data path "value"
    Then I want to commit this Item

    Then I want to get path "/items/{item.id}/versions"
    Then I want to check property "listCount" for value 1
    Then I want to make sure this Item has status "Draft"

    Then I want to delete this VLM

   Then I want the following to fail with error code "ENTITY_NOT_FOUND"
   Then I want to get path "/items/{item.id}/versions"





