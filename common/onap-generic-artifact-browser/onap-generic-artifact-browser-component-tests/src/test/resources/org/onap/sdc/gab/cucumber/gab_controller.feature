Feature: GABService usage
  GABService can find a keywords inside the yaml file

  Scenario: Ask for two-results keyword providing path to yaml
    Given new GAB service
    Given yaml document path "yaml/faultRegistration.yml"
    Given header to search "event.action[1]"
    When I ask service for results providing file
    Then Service should find 2 results

  Scenario: Ask for single-results keyword providing yaml content
    Given new GAB service
    Given yaml document content "event: {presence: required, action: [ any, any, alarm003, Clear ], structure: {}}"
    Given header to search "event.action[1]"
    When I ask service for results providing content
    Then Service should find 1 results