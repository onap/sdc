Feature: Enable all toggls

Scenario: Enable/Disable all toggls
    When I want to list Togglz
    Then I want to set all Togglz to be "true"
    Then I want to list Togglz
    Then I want to set all Togglz to be "false"
    Then I want to list Togglz