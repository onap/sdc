Feature: Interface Lifecycle Types

Scenario: Test Interface Lifecycle Types

  When I want to get interface lifecycle types
  Then I want to check property "tosca.interfaces.node.lifecycle.standard" exists
  Then I want to check property "tosca.interfaces.nfv.vnf.lifecycle.nfv" exists

