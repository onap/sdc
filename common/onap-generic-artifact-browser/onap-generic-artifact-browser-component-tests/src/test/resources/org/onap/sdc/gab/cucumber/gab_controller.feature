Feature: GABService usage
  GABService can find a keywords inside the yaml file

  Scenario: Ask for two-results keyword providing path to yaml
    Given yaml document "yaml/faultRegistration.yml" of type "PATH"
    And header to search "event.action[1]"
    When I ask service for results
    Then Service should find 2 results

  Scenario: Ask for single-results keyword providing yaml content
    Given yaml document "event: {presence: required, action: [ any, any, alarm003, Clear ], structure: {}}" of type "CONTENT"
    And header to search "event.action[1]"
    When I ask service for results
    Then Service should find 1 results