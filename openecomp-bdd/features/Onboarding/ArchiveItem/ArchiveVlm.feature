Feature: Archive and Restore VLM

    Scenario: Archive VLM with Draft
        When I want to create a VLM
        Then I want to create input data
        Then I want to update the input property "name" with a random value
        Then I want to update the input property "type" with value "Universal"
        Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
        Then I want to commit this Item

        Then I want to archive this item
        Then I want to list Archived VLMs
        Then I want to check that item exits in response

    Scenario: Archive Already Archived VLM - Negative
        When I want to create a VLM
        Then I want to create input data
        Then I want to update the input property "name" with a random value
        Then I want to update the input property "type" with value "Universal"
        Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
        Then I want to submit this VLM

        Then I want to archive this item
        Then I want to list Archived VLMs
        Then I want to check that item exits in response
        Then I want the following to fail with error message "Archive item failed, item {item.id} is already Archived"
        Then I want to archive this item


    Scenario: Archive Certified VLM
            When I want to create a VLM
            Then I want to create input data
            Then I want to update the input property "name" with a random value
            Then I want to update the input property "type" with value "Universal"
            Then I want to create for path "/vendor-license-models/{item.id}/versions/{item.versionId}/license-key-groups" with the input data from the context
            Then I want to submit this VLM

            Then I want to archive this item
            Then I want to list Archived VLMs
            Then I want to check that item exits in response
            Then I want to list Active VLMs
            Then I want to check that item does not exits in response
