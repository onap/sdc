Feature: Archive and Restore VSP

    Background: Init
        Given I want to create a VLM

    Scenario: Archive VSP with Draft
            When I want to create a VSP with onboarding type "NetworkPackage"
            When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
            And I want to process the NetworkPackage file for this VSP

            Then I want to commit this Item
            Then I want to get path "/items/{item.id}/versions"
            Then I want to check property "listCount" for value 1
            Then I want to make sure this Item has status "Draft"

            Then I want to archive this item
            Then I want to list Archived VSPs
            Then I want to check that item exits in response
            Then I want to list Active VSPs
            Then I want to check that item does not exits in response

    Scenario: Archive Already Archived VSP - Negative
            When I want to create a VSP with onboarding type "NetworkPackage"
            When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
            And I want to process the NetworkPackage file for this VSP
            Then I want to commit this Item

            Then I want to archive this item
            Then I want to list Archived VSPs
            Then I want to check that item exits in response
            Then I want to list Active VSPs
            Then I want to check that item does not exits in response

            Then I want the following to fail with error message "Archive item failed, item {item.id} is already Archived"
            Then I want to archive this item


    Scenario: Archive Certified VSP
            When I want to create a VSP with onboarding type "NetworkPackage"
            Then I want to make sure this Item has status "Draft"
            When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
            And I want to process the NetworkPackage file for this VSP

            Then I want to commit this Item
            Then I want to get path "/items/{item.id}/versions"
            Then I want to check property "listCount" for value 1
            Then I want to make sure this Item has status "Draft"

            When I want to submit this VSP
            Then I want to make sure this Item has status "Certified"
            Then I want to get path "/items/{item.id}/versions"
            Then I want to check property "listCount" for value 1

            Then I want to archive this item
            Then I want to list Archived VSPs
            Then I want to check that item exits in response
            Then I want to list Active VSPs
            Then I want to check that item does not exits in response