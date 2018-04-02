#Feature: ActivitySpec Flow
#
#Given Default prefix "activity_spec"
#
#  Scenario: Test Invalid Status Transition for Activity Spec
#    #Create Activity Spec
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with a random value
#    When I want to create an ActivitySpec
#    Then I want to check property "id" exists
#    And I want to check property "versionId" exists
#
#    #Get Activity Spec and verify status
#    When I want to get the ActivitySpec for the current item
#    Then I want to check property "status" for value "Draft"
#
#    #Deprecate "Draft" activity status and verify error message
#    Then I want the following to fail with error message "Activity Spec is in an invalid state"
#    When I want to call action "DEPRECATE" on this ActivitySpec item
#
#    #Delete "Draft" activity spec and verify error code
#    Then I want the following to fail with error message "Activity Spec is in an invalid state"
#    When I want to call action "DELETE" on this ActivitySpec item
#
#    #Certify activity spec
#    When I want to call action "CERTIFY" on this ActivitySpec item
#    #Certify "certified" activity spec and verify error code
#    Then I want the following to fail with error message "Activity Spec is in an invalid state"
#    When I want to call action "CERTIFY" on this ActivitySpec item