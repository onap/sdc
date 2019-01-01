Feature: VLM Example File
  Scenario: VLM Defaults

    When I want to create a VLM
    Then I want to make sure this Item has status "Draft"
    When I want to submit this VLM
    Then I want to make sure this Item has status "Certified"
    Then I want to create a new version for this Item

  Scenario: Testing revisions with VLM
    When I want to create a VLM

    When I want to get path "/items/{item.id}/versions/{item.versionId}/revisions"
    Then I want to check property "listCount" for value 1

    # example creating input data
    Then I want to create input data
    Then I want to update the input property "name" with a random value
    Then I want to update the input property "type" with value "Universal"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
    Then I want to copy to property "lastProcessId" from response data path "value"
    Then I want to commit this Item

    Then I want to get path "/items/{item.id}/versions/{item.versionId}/revisions"
    Then I want to check property "listCount" for value 2
    Then I want to copy to property "setRevision" from response data path "results[1].id"

    When I want to revert this Item to the revision with the value from saved property "setRevision"
    Then I want to get path "/items/{item.id}/versions/{item.versionId}/revisions"
    Then I want to check property "listCount" for value 2

    When I want to get path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups"
    Then I want to check property "listCount" for value 0

    Then I want to create input data
    Then I want to update the input property "name" with a random value
    Then I want to update the input property "type" with value "Universal"
    Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
    Then I want to copy to property "newLKG" from response data path "value"
    Then I want to delete for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the value from saved property "newLKG"

    When I want to set property "NotExisting" to value "NotExisting"
    Then I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to revert this Item to the revision with the value from saved property "NotExisting"
    Then I want to revert this Item to the revision with the value from saved property "setRevision"

