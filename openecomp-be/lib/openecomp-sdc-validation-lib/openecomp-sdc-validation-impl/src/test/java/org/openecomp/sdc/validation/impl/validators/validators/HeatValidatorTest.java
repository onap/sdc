package org.openecomp.sdc.validation.impl.validators.validators;

import org.openecomp.sdc.validation.impl.validators.HeatValidator;
import org.openecomp.sdc.validation.impl.validators.ValidatorBaseTest;
import org.openecomp.core.validation.types.MessageContainer;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class HeatValidatorTest extends ValidatorBaseTest {


    @Test
    public void testInvalidHeatFormat(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/invalid_heat_format/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Invalid HEAT format problem - [Cannot create property=kuku for JavaBean=Resource{type='null', properties=null, metadata=null, depends_on=null, update_policy='null', deletion_policy='null'}\n" +
                " in 'reader', line 25, column 5:\n" +
                "        kuku: kuku\n" +
                "        ^\n" +
                "Unable to find property 'kuku' on class: org.openecomp.sdc.heat.datatypes.model.Resource\n" +
                " in 'reader', line 25, column 11:\n" +
                "        kuku: kuku\n" +
                "              ^\n" +
                "]");
    }


    @Test
    public void testResourcesReferencesExistInHeat() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/resource_references_exist_in_heat/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Referenced resource - not_existing_resource not found");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "invalid get_resource syntax is in use - null , get_resource function should get the resource id of the referenced resource");
    }


    @Test
    public void testGetResourceValueIsValid(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/get_resource_value_valid/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 3);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "invalid get_resource syntax is in use - [param_1, param_2] , get_resource function should get the resource id of the referenced resource");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "invalid get_resource syntax is in use - {get_param=param_1} , get_resource function should get the resource id of the referenced resource");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(2).getMessage(), "invalid get_resource syntax is in use - null , get_resource function should get the resource id of the referenced resource");
    }

    @Test
    public void testTwoResourcesDoesNotHoldSameId() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/two_resources_does_not_hold_same_id/positive_test/input");
        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 0);
    }

    @Test
    public void negativeTestGetParamPointToExistingParameter() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/get_param_points_to_existing_parameter/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Referenced parameter - not_existing_param_1 - not found, used in resource - server_pcrf_psm_001");
    }

    @Test
    public void testGetAttrFromNested() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/get_attr_from_nested/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "get_attr attribute not found - nested_output in resource server_pcrf_psm_001");
    }

    @Test
    public void testPropertiesMatchNestedParameters() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/properties_match_nested_parameters/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Referenced parameter not found in nested file - nested-pps_v1.0.yaml, resource name - server_pcrf_pps_001, parameter name - parameter_not_existing_in_nested");
    }

    @Test
    public void testNovaPropertiesHasAssignedValue() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/nova_properties_has_assigned_value/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Missing both Image and Flavor in NOVA Server - nova_server_resource_missing_both");
    }

    @Test
    public void testNoLoopsNesting() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/no_loops_nesting/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 4);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "A resource has an invalid or unsupported type - null, Resource ID [server_pcrf_psm_002]");
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "Nested files loop - [hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml]");

        Assert.assertEquals(messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().get(0).getMessage(), "Nested files loop - [nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml]");
        Assert.assertEquals(messages.get("nested-points-to-hot-nimbus-psm.yaml").getErrorMessageList().get(1).getMessage(), "Nested files loop - [nested-points-to-hot-nimbus-psm.yaml -- nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml]");

        Assert.assertEquals(messages.get("yaml-point-to-itself.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("yaml-point-to-itself.yaml").getErrorMessageList().get(0).getMessage(), "Nested files loop - [yaml-point-to-itself.yaml -- yaml-point-to-itself.yaml]");

        Assert.assertEquals(messages.get("nested-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("nested-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Nested files loop - [nested-psm_v1.0.yaml -- nested-points-to-hot-nimbus-psm.yaml -- hot-nimbus-psm_v1.0.yaml -- nested-psm_v1.0.yaml]");
    }

    @Test
    public void testOnlyOneNovaPointsToOnePort() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/one_nova_points_to_one_port/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Resource Port oam1_int_port exceed allowed relations from NovaServer");
    }

    @Test
    public void testServerGroupsPointedByServersDefinedCorrectly() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/server_groups_defined_correctly/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 3);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Missing server group definition - BE_Affinity_2, nova_server_1");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "Missing server group definition - BE_Affinity_2, nova_server_2");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(2).getMessage(), "Referenced resource - BE_Affinity_1 not found");
    }


    @Test
    public void testPolicyIsAffinityOrAntiAffinity() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/policy_is_affinity_or_anti_affinity/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Wrong policy in server group - pcrf_server_policies_1");
    }


    @Test
    public void testEnvContentIsSubSetOfHeatParameters() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/env_content_is_subset_of_heat/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().get(0).getMessage(), "Env file hot-nimbus-pps_v1.0.env includes a parameter not in HEAT - mock_param");
    }

    @Test
    public void testDefaultValueAlignWithType() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/default_value_align_with_type/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Parameter - pcrf_pps_image_name_1 default value not align with type number");
    }


    @Test
    public void testEnvParametersMatchDefinedHeatParameterTypes() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/env_parameters_match_defined_types/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.env").getErrorMessageList().get(0).getMessage(), "Parameter env value pcrf_pps_flavor_name not align with type");

    }

    @Test
    public void testReferencedArtifactsExist() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/referenced_artifacts_exist/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Missing artifact - nimbus-ethernet");

    }


    @Test
    public void testResourcesGroupWithNested() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/resources_group_with_nested/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 3);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Referenced parameter not found in nested file - nested-from-resources-group.yaml, resource name - nested-from-resources-group.yaml, parameter name - property_not_in_nested");
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_without_resources_group], resource_def type [OS::Nova::Server]");

        Assert.assertEquals(messages.get("nested-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("nested-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "Nested files loop - [nested-pps_v1.0.yaml -- nested-from-resources-group.yaml -- hot-nimbus-pps_v1.0.yaml -- nested-pps_v1.0.yaml]");

        Assert.assertEquals(messages.get("nested-not-exist.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("nested-not-exist.yaml").getErrorMessageList().get(0).getMessage(), "Missing nested file - nested-not-exist.yaml");
    }


    @Test
    public void testResourceGroupWithInvalidType(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/resource_group_invalid_type/negative_test/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().size(), 3);
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_with_resources_group_1], resource_def type [{get_param=pcrf_vnf_id}]");
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, Resource ID [resource_with_resources_group_2], resource_def type [OS::Nova::Server]");
        Assert.assertEquals(messages.get("hot-nimbus-psm_v1.0.yaml").getErrorMessageList().get(2).getMessage(), "A resource has an invalid or unsupported type - null, Resource ID [resource_with_resources_group_3]");
    }


    @Test
    public void testNetworkPolicyAssociatedWithAttachPolicy() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/network_policy_associated_with_attach_policy/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "NetworkPolicy not in use, Resource Id [not_used_server_pcrf_policy]");
    }


    @Test
    public void testSecurityGroupsCalledByPort() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/security_group_called_by_port/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "SecurityGroup not in use, Resource Id [not_used_security_group]");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "Port not bind to any NOVA Server, Resource Id [attach_policy_resource]");
    }


    @Test
    public void testServerGroupCalledByServer() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/server_group_called_by_nova_server/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "ServerGroup not in use, Resource Id [not_used_server_group]");

    }


    @Test
    public void testSecurityGroupBaseFileNoPorts() throws IOException {
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/security_group_base_file_no_ports/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().size(), 1);
        Assert.assertEquals(messages.get("baseFile.yaml").getErrorMessageList().get(0).getMessage(), "SecurityGroup not in use, Resource Id [shared_security_group_id3]");
    }


    @Test
    public void testDependsOn(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/depends_on_points_to_existing_resource/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "a Missing resource in depend On Missing Resource ID [resource_not_exist]");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "a Missing resource in depend On Missing Resource ID [resource_3]");
    }


    @Test
    public void testSharedResourcesValidation(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/shared_resources/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(), "SecurityGroup not in use, Resource Id [not_used_security_group]");
        Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(), "Port not bind to any NOVA Server, Resource Id [attach_policy_resource]");
    }


    @Test
    public void testNoErrorWhenEmptyValueForParameterInEnv(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/env_empty_value/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 1);

        MessageContainer envMessages = messages.get("env_empty_value.env");
        Assert.assertNull(envMessages);
    }


    @Test
    public void testGetParamPseudoParameters(){
        Map<String, MessageContainer> messages = runValidation("/openecomp/org/validation/validators/heat_validator/pseudo_parameters/input");

        Assert.assertNotNull(messages);
        Assert.assertEquals(messages.size(), 0);

    }


    @Override
    public Map<String, MessageContainer> runValidation(String path) {
        HeatValidator heatValidator = new HeatValidator();
        return testValidator(heatValidator, path);
    }
}
