#Feature: ActivitySpec Flow - Test Create Activity Spec
#
#Given Default prefix "activity_spec"
#
#  # SDC-6350
#  Scenario: Test Create Activity Spec functionality
#    #Create ActivitySpec
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with a random value
#    When I want to create an ActivitySpec
#    #Check id and versionId returned in response
#    Then I want to check property "id" exists
#    And I want to check property "versionId" exists
#
#    #List ActivitySpec
#    And I want to list ActivitySpecs with status "Draft"
#    And I want to check property "listCount" exists
#
#    #Get ActivitySpec and verify its status
#    And I want to get the ActivitySpec for the current item
#    And I want to check property "status" for value "Draft"
#
#    #Certify and Get ActivitySpec and verify its status
#    And I want to call action "CERTIFY" on this ActivitySpec item
#    And I want to get the ActivitySpec for the current item
#    And I want to check property "status" for value "Certified"
#
#    #Deprecate and Get ActivitySpec and verify its status
#    And I want to call action "DEPRECATE" on this ActivitySpec item
#    And I want to get the ActivitySpec for the current item
#    And I want to check property "status" for value "Deprecated"
#
#    #Delete and Get ActivitySpec and verify its status
#    And I want to call action "DELETE" on this ActivitySpec item
#    And I want to get the ActivitySpec for the current item
#    And I want to check property "status" for value "Deleted"
#
#    #Pass Invalid Id to Get and verify error code
#    Then I want to set property "item.id" to value "invalidId"
#    Then I want the following to fail with error code "ACTIVITYSPEC_NOT_FOUND"
#    And I want to get the ActivitySpec for the current item
#
#  # SDC-6353
#  Scenario: Test Create Activity Spec With Duplicate Name
#    #Create ActivitySpec with name "test"
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with value "test"
#    When I want to create an ActivitySpec
#    Then I want to check property "id" exists
#    And I want to check property "versionId" exists
#
#    #Again Create ActivitySpec with name "test" and verify error code
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with value "test"
#    Then I want the following to fail with error code "UNIQUE_VALUE_VIOLATION"
#    When I want to create an ActivitySpec
#
#  # SDC-6354
#  Scenario: Test Create Activity Spec With Invalid Name Format
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with value "test!@"
#    Then I want the following to fail with error code "FIELD_VALIDATION_ERROR_ERR_ID"
#    When I want to create an ActivitySpec
#
#  # SDC-6355
#  Scenario: Test Create Activity Spec With Null/Blank Name
#    When I want to set the input data to file "resources/json/createActivitySpec.json"
#    Then I want to update the input property "name" with value ""
#    Then I want the following to fail with error code "FIELD_VALIDATION_ERROR_ERR_ID"
#    When I want to create an ActivitySpec