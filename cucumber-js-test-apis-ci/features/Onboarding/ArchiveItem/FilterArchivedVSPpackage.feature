Feature: Filter Archived VSP Package Details

  Background: Init
    Given I want to create a VLM

  Scenario: Active VSP Package
        When I want to create a VSP with onboarding type "NetworkPackage"
        Then I want to make sure this Item has status "Draft"

        When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
        And I want to process the NetworkPackage file for this VSP
        Then I want to commit this Item
        Then I want to submit this VSP
        Then I want to package this VSP
        Then I want to make sure this Item has status "Certified"
        Then I want to get path "/items/{item.id}/versions"
        Then I want to check property "listCount" for value 1

        Then I want to list Active VSPs packages
        Then I want to check that VSP package exits in response


  Scenario: Archived VSP Package
          When I want to create a VSP with onboarding type "NetworkPackage"
          Then I want to make sure this Item has status "Draft"

          When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
          And I want to process the NetworkPackage file for this VSP
          Then I want to commit this Item
          Then I want to submit this VSP
          Then I want to package this VSP
          Then I want to make sure this Item has status "Certified"
          Then I want to get path "/items/{item.id}/versions"
          Then I want to check property "listCount" for value 1
          Then I want to archive this item

          Then I want to list Archived VSPs packages
          Then I want to check that VSP package exits in response


