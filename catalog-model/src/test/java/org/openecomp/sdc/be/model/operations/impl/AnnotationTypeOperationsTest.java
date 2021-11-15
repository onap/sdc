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

package org.openecomp.sdc.be.model.operations.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = "classpath:application-context-test.xml")
public class AnnotationTypeOperationsTest extends ModelTestBase {

    static final String TYPE = "org.openecomp.annotations.source";
    static final String NEW_TYPE = "org.openecomp.annotations.Source";
    static final String DESCRIPTION = "description";
    static final String NEW_DESCRIPTION = "new description";

    @Resource
    private JanusGraphGenericDao janusGraphGenericDao;

    @Resource
    private CommonTypeOperations commonTypeOperations;

    @Resource
    private AnnotationTypeOperations annotationTypeOperations;

    private PropertyDefinition prop1, prop2;
    private AnnotationTypeDefinition initialAnnotationDefinition;

    @BeforeAll
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @BeforeEach
    public void initTestData() {
        removeGraphVertices(janusGraphGenericDao.getGraph());
        prop1 = createSimpleProperty("val1", "prop1", "string");
    }

    @AfterEach
    public void tearDown() {
        janusGraphGenericDao.rollback();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddType() {
        prepareInitialType();
        AnnotationTypeDefinition result = annotationTypeOperations.addType(initialAnnotationDefinition);
        assertThat(result.getUniqueId()).isNotEmpty();
        assertThat(result)
            .isEqualToComparingOnlyGivenFields(initialAnnotationDefinition, "description", "type");
        assertThat(result.getProperties())
            .usingElementComparatorOnFields("defaultValue", "name", "type")
            .containsExactlyInAnyOrder(prop1);
        assertThat(result.isHighestVersion()).isTrue();
    }

    @Test
    public void testGetLatestType_TypeDoesntExist_shouldReturnNull() {
        AnnotationTypeDefinition latestType = annotationTypeOperations.getLatestType(TYPE);
        assertThat(latestType).isNull();
    }

    @Test
    public void testGetLatestType_TypeExists_shouldReturnIt() {
        addAnnotationType();
        AnnotationTypeDefinition latestType = annotationTypeOperations.getLatestType(TYPE);
        assertThat(latestType.getType()).isEqualTo(TYPE);
    }

    public void addAnnotationType() {
        prepareInitialType();
        annotationTypeOperations.addType(initialAnnotationDefinition);
        janusGraphGenericDao.commit();
    }

    @Test
    public void compareTypes_same_shouldReturnTrue() {
        AnnotationTypeDefinition type1 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        AnnotationTypeDefinition type2 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        assertThat(annotationTypeOperations.isSameType(type1, type2)).isTrue();
    }

    @Test
    public void compareTypes_sameExceptVersions_shouldReturnTrue() {
        AnnotationTypeDefinition type1 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        AnnotationTypeDefinition type2 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        type1.setVersion("1");
        type2.setVersion("2");
        assertThat(annotationTypeOperations.isSameType(type1, type2)).isTrue();
    }

    @Test
    public void compareTypes_differentType_shouldReturnFalse() {
        AnnotationTypeDefinition type1 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        AnnotationTypeDefinition type2 = buildAnnotationDefinition(DESCRIPTION, NEW_TYPE, prop1);
        assertThat(annotationTypeOperations.isSameType(type1, type2)).isFalse();
    }

    @Test
    public void compareTypes_differentDescription_shouldReturnFalse() {
        AnnotationTypeDefinition type1 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        AnnotationTypeDefinition type2 = buildAnnotationDefinition(NEW_DESCRIPTION, TYPE, prop1);
        assertThat(annotationTypeOperations.isSameType(type1, type2)).isFalse();
    }

    @Test
    public void compareTypes_differentProperty_shouldReturnFalse() {
        AnnotationTypeDefinition type1 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop1);
        prop2 = createSimpleProperty("val2", "prop2", "string");
        AnnotationTypeDefinition type2 = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        assertThat(annotationTypeOperations.isSameType(type1, type2)).isFalse();
    }

    @Test
    public void testUpdateType_propertyAdded_shouldSucceed() {
        addAnnotationType();
        prop2 = createSimpleProperty("val2", "prop2", "string");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(NEW_DESCRIPTION, TYPE, prop1, prop2);
        AnnotationTypeDefinition updatedType = annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
        assertThat(updatedType.getDescription()).isEqualTo(NEW_DESCRIPTION);
        assertThat(updatedType.getProperties())
            .usingElementComparatorOnFields("defaultValue", "name", "type")
            .containsExactlyInAnyOrder(prop1, prop2);
    }

    @Test
    public void testUpdateType_propertyDefaultValueModification_shouldSucceed() {
        addAnnotationType();
        prop2 = createSimpleProperty("val3", "prop1", "string");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        AnnotationTypeDefinition updatedType = annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
        assertThat(updatedType.getProperties())
            .usingElementComparatorOnFields("defaultValue", "name", "type")
            .containsExactlyInAnyOrder(prop2);
    }

    @Test
    public void testUpdateType_propertyDescriptionModification_shouldSucceed() {
        addAnnotationType();
        prop2 = createSimpleProperty("val1", "prop1", "string");
        prop2.setDescription("bla");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        AnnotationTypeDefinition updatedType = annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
        assertThat(updatedType.getProperties())
            .usingElementComparatorOnFields("defaultValue", "name", "type", "description")
            .containsExactlyInAnyOrder(prop2);
    }

    @Test
    public void testUpdateType_propertyTypeModification_shouldFail() {
        Assertions.assertThrows(StorageException.class,()->{
        addAnnotationType();
        prop2 = createSimpleProperty("val1", "prop1", "int");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
        });
    }

    @Test
    public void testUpdateType_propertyRemoved_shouldFail() {
        Assertions.assertThrows(StorageException.class,()->{
            addAnnotationType();
            prop2 = createSimpleProperty("val1", "prop2", "int");
            AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
            annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
        });
    }

    private void prepareInitialType() {
        initialAnnotationDefinition = buildAnnotationDefinition(DESCRIPTION,
            TYPE,
            prop1);
        initialAnnotationDefinition.setVersion(TypeUtils.getFirstCertifiedVersionVersion());
    }

    private AnnotationTypeDefinition buildAnnotationDefinition(String description, String type, PropertyDefinition... properties) {
        AnnotationTypeDefinition annotationTypeDefinition = new AnnotationTypeDefinition();
        annotationTypeDefinition.setDescription(description);
        annotationTypeDefinition.setType(type);
        annotationTypeDefinition.setHighestVersion(true);
        annotationTypeDefinition.setProperties(asList(properties));
        return annotationTypeDefinition;
    }

}
