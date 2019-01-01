Feature: Delete VSP Draft

    Background: Init
        Given I want to create a VLM

    Scenario: Delete VSP with only draft
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

        Then I want the following to fail with error message "VSP has been certified and cannot be deleted."
        Then I want to delete this VSP

        Then I want to get path "/items/{item.id}/versions"
        Then I want to check property "listCount" for value 1

