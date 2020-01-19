/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeFilterValidationTest {

    private static final String UI_CONSTRAINT_STATIC = "Prop1: {equal: 'value'}";
    private static final String INNER_SERVICE = "innerService";
    private static final String PROPERTY_NAME = "Prop1";
    private static final String VALUE = "value";
    private static final String FLOAT_TYPE = "float";
    private static final String STRING_TYPE = "string";
    private static final String LIST_TYPE = "list";
    private static final String COMPONENT1_ID = "component1";
    private static final String INTEGER_TYPE = "integer";
    private static final String PARENTSERVICE_ID = "parentservice";
    private static final String COMPONENT2_ID = "component2";
    private ComponentsUtils componentsUtils;

    @InjectMocks
    private NodeFilterValidator nodeFilterValidator;

    @Before
    public void setup() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectPropertyTypeProvided() {
        Service service = createService("booleanIncorrect");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectOperatorProvidedBoolean() {
        Service service = createService("boolean");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")
                                .replace("equal", "greater_than")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectValueProvidedBoolean() {
        Service service = createService("boolean");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "trues")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectOperatorProvidedString() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")
                                .replace("equal", "greater_than")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterIntegerValueSuccess() {
        Service service = createService(INTEGER_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1")),
                                NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterIntegerValueFail() {
        Service service = createService(INTEGER_TYPE);

        Mockito.when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, "param1"))
                .thenReturn(new ResponseFormat());

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1.0")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isRight());
    }

    @Test
    public void testValidateNodeFilterFloatValueSuccess() {
        Service service = createService(FLOAT_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1.0")),
                        NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterFloatValueFail() {
        Service service = createService(FLOAT_TYPE);

        Mockito.when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, "param1"))
                .thenReturn(new ResponseFormat());

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC), NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isRight());
    }

    @Test
    public void testValidateNodeFilterStringValueSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC), NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintBrotherSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property :[component2, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintBrotherPropertyTypeMismatch() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT2_ID).get(0).setType(INTEGER_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [component2, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertyTypeMismatch() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(INTEGER_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertyNotFound() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testvalidatePropertyConstraintBrotherPropertyNotFound() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertySchemaMismatch() {
        Service service = createService(LIST_TYPE,STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(LIST_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateNodeFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                                                                                                                 + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD);

        Assert.assertFalse(either.isLeft());
    }

    private Service createService(String type) {
        return createService(type, null);
    }

    private Service createService(String type, String schemaType) {
        Service service = new Service();
        service.setName(PARENTSERVICE_ID);

        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(PROPERTY_NAME);
        propertyDefinition.setType(type);
        if (schemaType != null){
            SchemaDefinition schemaDefinition = new SchemaDefinition();
            PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
            schemaProperty.setType(schemaType);
            schemaDefinition.setProperty(schemaProperty);
            propertyDefinition.setSchema(schemaDefinition);
        }
        service.setProperties(Collections.singletonList(propertyDefinition));

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT1_ID);
        componentInstance.setName(COMPONENT1_ID);

        ComponentInstance componentInstance2 = new ComponentInstance();
        componentInstance2.setUniqueId(COMPONENT2_ID);
        componentInstance2.setName(COMPONENT2_ID);

        service.setComponentInstances(Arrays.asList(componentInstance, componentInstance2));

        ComponentInstanceProperty componentInstanceProperty  = new ComponentInstanceProperty();
        componentInstanceProperty.setName(PROPERTY_NAME);
        componentInstanceProperty.setType(type);

        ComponentInstanceProperty componentInstanceProperty2  = new ComponentInstanceProperty();
        componentInstanceProperty2.setName(PROPERTY_NAME);
        componentInstanceProperty2.setType(type);

        Map<String, List<ComponentInstanceProperty>> componentInstancePropertyMap = new HashMap<>();
        componentInstancePropertyMap.put(componentInstance.getUniqueId(),
                Collections.singletonList(componentInstanceProperty));
        componentInstancePropertyMap.put(componentInstance2.getUniqueId(),
                Collections.singletonList(componentInstanceProperty2));
        componentInstancePropertyMap.put(INNER_SERVICE, Collections.singletonList(componentInstanceProperty));

        service.setComponentInstancesProperties(componentInstancePropertyMap);

        return service;
    }

}
