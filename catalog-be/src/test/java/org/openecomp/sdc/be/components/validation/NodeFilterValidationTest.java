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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

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

    @BeforeEach
    public void setup() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        MockitoAnnotations.openMocks(this);
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    public void testValidateComponentInstanceExist() {
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateComponentInstanceExist(null, INNER_SERVICE);
        assertTrue(either.isRight());
        assertEquals("Error: Internal Server Error. Please try again later.", either.right().value().getText());
        assertEquals(500, either.right().value().getStatus());

        Service service = createService("booleanIncorrect");
        either = nodeFilterValidator.validateComponentInstanceExist(service, INNER_SERVICE);
        assertTrue(either.isRight());
        assertEquals("Error: Internal Server Error. Please try again later.", either.right().value().getText());
        assertEquals(500, either.right().value().getStatus());

        List<ComponentInstance> list = new LinkedList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("uniqueId");
        list.add(instance);
        service.setComponentInstances(list);
        either = nodeFilterValidator.validateComponentInstanceExist(service, "uniqueId");
        assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectPropertyTypeProvided() {
        Service service = createService("booleanIncorrect");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);
        assertTrue(either.isRight());

        either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.CAPABILITIES);
        assertTrue(either.isRight());
    }

    @Test
    public void testValidateComponentFilter() {
        Service service = createService("booleanIncorrect");
        String property = "Prop1: {equal: {get_property: ['test','test2']}}";
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(property),
                        NodeFilterConstraintAction.ADD);
        assertTrue(either.isRight());

        property = "Prop1: {equal: {get_property: ['parentservice','Prop1']}}";
        either =
                nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(property),
                        NodeFilterConstraintAction.ADD);
        assertTrue(either.isLeft());

        String staticStr = "Prop1: {equal: 1}";
        either = nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(staticStr),
                        NodeFilterConstraintAction.ADD);
        assertTrue(either.isLeft());
        assertTrue(either.left().value());

        staticStr = "Prop1: {equal: 'true'}";
        either = nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(staticStr),
                        NodeFilterConstraintAction.ADD);
        assertTrue(either.isRight());

        staticStr = "Prop1: {greater_than: '3'}";
        either = nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(staticStr),
                NodeFilterConstraintAction.ADD);
        assertTrue(either.isRight());

        staticStr = "test: {greater_than: '3'}";
        either = nodeFilterValidator.validateComponentFilter(service, Collections.singletonList(staticStr),
                NodeFilterConstraintAction.ADD);
        assertTrue(either.isRight());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectOperatorProvidedBoolean() {
        Service service = createService("boolean");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")
                                .replace("equal", "greater_than")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectValueProvidedBoolean() {
        Service service = createService("boolean");
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "trues")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterStaticIncorrectOperatorProvidedString() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "true")
                                .replace("equal", "greater_than")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterIntegerValueSuccess() {
        Service service = createService(INTEGER_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1")),
                                NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterIntegerValueFail() {
        Service service = createService(INTEGER_TYPE);

        Mockito.when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, "param1"))
                .thenReturn(new ResponseFormat());

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1.0")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isRight());
    }

    @Test
    public void testValidateNodeFilterFloatValueSuccess() {
        Service service = createService(FLOAT_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC.replace(VALUE, "1.0")),
                        NodeFilterConstraintAction.ADD, NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidateNodeFilterFloatValueFail() {
        Service service = createService(FLOAT_TYPE);

        Mockito.when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, "param1"))
                .thenReturn(new ResponseFormat());

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                        Collections.singletonList(UI_CONSTRAINT_STATIC), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isRight());
    }

    @Test
    public void testValidateNodeFilterStringValueSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, INNER_SERVICE,
                    Collections.singletonList(UI_CONSTRAINT_STATIC), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintBrotherSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property :[component2, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentSuccess() {
        Service service = createService(STRING_TYPE);
        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property : [SELF, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintBrotherPropertyTypeMismatch() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT2_ID).get(0).setType(INTEGER_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [component2, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertyTypeMismatch() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(INTEGER_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertyNotFound() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testvalidatePropertyConstraintBrotherPropertyNotFound() {
        Service service = createService(STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                        + "  equal:  { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

        Assert.assertFalse(either.isLeft());
    }

    @Test
    public void testValidatePropertyConstraintParentPropertySchemaMismatch() {
        Service service = createService(LIST_TYPE,STRING_TYPE);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(LIST_TYPE);

        Either<Boolean, ResponseFormat> either =
                nodeFilterValidator.validateFilter(service, COMPONENT1_ID, Collections.singletonList("Prop1:\n"
                    + "  equal: { get_property : [parentservice, Prop1]}\n"), NodeFilterConstraintAction.ADD,
                    NodeFilterConstraintType.PROPERTIES);

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
