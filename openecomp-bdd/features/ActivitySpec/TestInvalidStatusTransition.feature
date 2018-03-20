#Feature: ActivitySpec Flow
#
#Given Default prefix "activity_spec"
#
#  Scenario: Test Invalid Status Transition for Activity Spec
#    #Create Activity Spec
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with a random value
#    When I want to create a ActivitySpec
#    Then I want to check property "id" exists
#    And I want to check property "versionId" exists
#
#    #Get Activity Spec and verify status
#    When I want to get ActivitySpec
#    Then I want to check property "status" for value "Draft"
#
#    #Deprecate "Draft" activity status and verify error code
#    Then I want the following to fail with error code "STATUS_NOT_CERTIFIED"
#    When I want to "Deprecate" ActivitySpec
#
#    #Delete "Draft" activity spec and verify error code
#    Then I want the following to fail with error code "STATUS_NOT_DEPRECATED"
#    When I want to "Delete" ActivitySpec
#
#    #Certify activity spec
#    When I want to "Certify" ActivitySpec
#    #Certify "certified" activity spec and verify error code
#    Then I want the following to fail with error code "STATUS_NOT_DRAFT"
#    When I want to "Certify" ActivitySpec
