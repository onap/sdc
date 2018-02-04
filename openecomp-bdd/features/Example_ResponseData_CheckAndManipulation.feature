Feature: Example for checking response data
  Scenario: Example Checks
    # setting some data just for testing purposes
    Given Response Data:
    """
    {
      "field1" : "string field",
      "field2" : "true",
      "field3": "5",
      "field4" : [{"entry1":"a"},{"entry2":"b"},{"entry3":"c"}]
    }
    """
    # printing out for test purposes
    Then I want to print the context data

    # running the different options of checking the respone data
    Then I want to check property "field1" for value "string field"
    Then I want to check property "field2" to be "true"
    Then I want to check property "field3" for value 5
    Then I want to check property "field4" to have length 3
    Then I want to check property "field4[0].entry1" exists
    Then I want to check property "field4[0].no_exist" does not exist
