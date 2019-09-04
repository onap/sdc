/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications copyright (c) 2019 Nokia
 */

package org.onap.sdc.tosca.datatypes.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.MatcherAssert.assertThat;


public class TriggerTest {

    public static final String TRIGGER_WF_NAME_ACTION = "/mock/trigger/wfNameAction.yaml";
    public static final String TARGET_REQ = "reqA";
    public static final String ACTION_WORKFLOW_VAL = "deployment_workflow";
    public static final String POLICY_DEF_A = "policyA";
    public static final String TRIGGER_A = "triggerA";
    public static final String NODE_A = "nodeA";
    public static final String TRIGGER_OPERATION_ACTION = "/mock/trigger/operationAction.yaml";
    public static final String TARGET_CAP = "capA";
    public static final String OPERATION_ACTION_KEY = "operationAction";
    public static final String IMPLEMENTATION = "implementation";
    ToscaExtensionYamlUtil toscaExtYamlUtil = new ToscaExtensionYamlUtil();

    @Test
    public void getPolicyTriggerActionWf() throws IOException {
        String inputFile = TRIGGER_WF_NAME_ACTION;
        ServiceTemplate serviceTemplate = getServiceTemplate(inputFile);

        Map<String, PolicyDefinition> policies = serviceTemplate.getTopology_template().getPolicies();
        Trigger trigger = policyCheck(policies);
        Assert.assertEquals(TARGET_REQ, trigger.getTarget_filter().getRequirement());
        Object action = trigger.getAction();
        Assert.assertNotNull(action);
        Assert.assertEquals(true, action instanceof String);
        Assert.assertEquals(ACTION_WORKFLOW_VAL, action);
    }

    private ServiceTemplate getServiceTemplate(String inputPath) throws IOException {
        try (InputStream yamlFile = toscaExtYamlUtil.loadYamlFileIs(inputPath)) {
            return toscaExtYamlUtil.yamlToObject(yamlFile, ServiceTemplate.class);
        }
    }

    private Trigger policyCheck(Map<String, PolicyDefinition> policies) {
        Assert.assertNotNull(policies);
        PolicyDefinition policyDefinition = policies.get(POLICY_DEF_A);
        Assert.assertNotNull(policyDefinition);
        Map<String, Trigger> triggers = policyDefinition.getTriggers();
        Assert.assertNotNull(triggers);
        Trigger trigger = triggers.get(TRIGGER_A);
        Assert.assertNotNull(trigger);
        EventFilter targetFilter = trigger.getTarget_filter();
        Assert.assertNotNull(targetFilter);
        Assert.assertEquals(NODE_A, targetFilter.getNode());
        return trigger;
    }

    @Test
    public void getPolicyTriggerActionOperation() throws IOException {
        String inputFile = TRIGGER_OPERATION_ACTION;
        ServiceTemplate serviceTemplate = getServiceTemplate(inputFile);

        Map<String, PolicyDefinition> policies = serviceTemplate.getTopology_template().getPolicies();
        Trigger trigger = policyCheck(policies);
        Assert.assertEquals(TARGET_CAP, trigger.getTarget_filter().getCapability());
        Object action = trigger.getAction();
        Assert.assertNotNull(action);
        Assert.assertEquals(true, action instanceof Map);
        Object operationAction = ((Map) action).get(OPERATION_ACTION_KEY);
        Assert.assertNotNull(operationAction);
        Assert.assertEquals(true, operationAction instanceof Map);
        Assert.assertNotNull( ((Map)operationAction).get(IMPLEMENTATION));

    }
    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(Trigger.class, hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidEquals() {
        assertThat(Trigger.class, hasValidBeanEquals());
    }

    @Test
    public void shouldHaveValidHashCode() {
        assertThat(Trigger.class, hasValidBeanHashCode());
    }

}
