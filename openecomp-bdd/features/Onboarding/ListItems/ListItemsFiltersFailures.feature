Feature: List items with incorrect filter values

  Scenario: Filter by incorrect item type - negative
    When I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to get path "/items?itemType=bsp"

  Scenario: Filter by incorrect permission - negative
    When I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to get path "/items?permission=Contributer"

  Scenario: Filter by incorrect version status - negative
    When I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to get path "/items?versionStatus=Draftt"

  Scenario: Filter by incorrect item status - negative
    When I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to get path "/items?itemStatus=active"

  Scenario: Filter by incorrect onboarding method - negative
    When I want the following to fail with error code "GENERAL_ERROR_REST_ID"
    Then I want to get path "/items?onboardingMethod=heat"

    Then I want to print the context data

