Feature: Collaboration Example File

  Background: Init
    Given I want to create a VLM
    Scenario: Testing permissions for contributors and Owners
      Then I want to check user "mb033001" has no permissions on this Item

      When I want to add user "mb0001" as a contributor to this Item
      Then I want to get the permissions for this Item
      Then I want to check property "listCount" for value 2
      Then I want to check user "cs0008" has role "owner" on this Item
      Then I want to check user "mb0001" has role "contributor" on this Item

      When I want to set the user to "aaaa"
      Then I want the following to fail
      When I want to get the permissions for this Item

      When I want to set the user to "mb0001"
      Then I want the following to fail
      When I want to change the owner to user "mb0001" on this Item

      When I want to set the user to "cs0008"
      When I want to change the owner to user "mb0001" on this Item
      Then I want to get the permissions for this Item
      Then I want to check user "cs0008" has role "contributor" on this Item
      Then I want to check user "mb0001" has role "owner" on this Item