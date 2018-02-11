Feature: Tosca Validation Flow

  Background: Init
    Given I want to create a VLM

  Scenario: Full - Create and submit VSP Network Package and Create VF
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to make sure this Item has status "Certified"

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"

    Then I want to create a VF for this Item

  Scenario: Full - Input parameter annotation validation.
    When I want to create a VSP with onboarding type "NetworkPackage"

    Then I want to upload a NetworkPackage for this VSP from path "resources/uploads/inputsForNestedHeat.zip"
    And I want to process the NetworkPackage file for this VSP

    Then I want to commit this Item
    And I want to submit this VSP
    And I want to package this VSP

    Then I want to get the package for this Item to path "resources/downloads/VSPPackage.zip"
    When I want to load the yaml content of the entry "Definitions/MainServiceTemplate.yaml" in the zip "resources/downloads/VSPPackage.zip" to context

    Then I want to check property "topology_template.inputs.pcm_flavor_name.annotations.source.type" for value "org.openecomp.annotations.Source"
    Then I want to check property "topology_template.inputs.pcm_flavor_name.annotations.source.properties.vf_module_label" to have length 2
    Then I want to check property "topology_template.inputs.pcm_flavor_name.annotations.source.properties.vf_module_label[0]" for value "main-heat2"
    Then I want to check property "topology_template.inputs.pcm_flavor_name.annotations.source.properties.vf_module_label[1]" for value "main-heat1"
    Then I want to check property "topology_template.inputs.pcm_flavor_name.annotations.source.properties.param_name" for value "pcm_flavor_name"


    Then I want to check property "topology_template.inputs.sm_server_names.annotations.source.type" for value "org.openecomp.annotations.Source"
    Then I want to check property "topology_template.inputs.sm_server_names.annotations.source.properties.vf_module_label" to have length 1
    Then I want to check property "topology_template.inputs.sm_server_names.annotations.source.properties.vf_module_label[0]" for value "main-heat1"
    Then I want to check property "topology_template.inputs.sm_server_names.annotations.source.properties.param_name" for value "sm_server_names"

    Then I want to check property "topology_template.inputs.dummy_net_netmask_1.annotations.source.type" for value "org.openecomp.annotations.Source"
    Then I want to check property "topology_template.inputs.dummy_net_netmask_1.annotations.source.properties.vf_module_label" to have length 1
    Then I want to check property "topology_template.inputs.dummy_net_netmask_1.annotations.source.properties.vf_module_label[0]" for value "main-heat2"
    Then I want to check property "topology_template.inputs.dummy_net_netmask_1.annotations.source.properties.param_name" for value "dummy_net_netmask_1"

    When I want to load the yaml content of the entry "Definitions/nested-pcm_v0.1ServiceTemplate.yaml" in the zip "resources/downloads/VSPPackage.zip" to context
    Then I want to check property "topology_template.inputs.port_pcm_port_0_network_role.annotations" does not exist
    Then I want to check property "topology_template.inputs.availabilityzone_name.annotations" does not exist
    Then I want to check property "topology_template.inputs.pcm_server_name.annotations" does not exist

    When I want to load the yaml content of the entry "Definitions/nested-pcm_v0.2ServiceTemplate.yaml" in the zip "resources/downloads/VSPPackage.zip" to context
    Then I want to check property "topology_template.inputs.port_pcm_port_13_mac_requirements.annotations" does not exist
    Then I want to check property "topology_template.inputs.availabilityzone_name.annotations" does not exist
    Then I want to check property "topology_template.inputs.pcm_server_name.annotations" does not exist

    When I want to load the yaml content of the entry "Definitions/Nested_computeServiceTemplate.yaml" in the zip "resources/downloads/VSPPackage.zip" to context
    Then I want to check property "topology_template.inputs.compute_compute_user_data_format.annotations" does not exist
    Then I want to check property "topology_template.inputs.vm_image_name.annotations" does not exist
    Then I want to check property "topology_template.inputs.compute_compute_name.annotations" does not exist

    Then I want to create a VF for this Item