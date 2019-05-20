package org.openecomp.sdc.be.model.operations.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
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

    @BeforeClass
    public static void setupBeforeClass() {
        ModelTestBase.init();
    }

    @Before
    public void initTestData() {
        removeGraphVertices(janusGraphGenericDao.getGraph());
        prop1 = createSimpleProperty("val1", "prop1", "string");
    }

    @After
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

    @Test(expected = StorageException.class)
    public void testUpdateType_propertyTypeModification_shouldFail() {
        addAnnotationType();
        prop2 = createSimpleProperty("val1", "prop1", "int");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
    }

    @Test(expected = StorageException.class)
    public void testUpdateType_propertyRemoved_shouldFail() {
        addAnnotationType();
        prop2 = createSimpleProperty("val1", "prop2", "int");
        AnnotationTypeDefinition advancedDefinition = buildAnnotationDefinition(DESCRIPTION, TYPE, prop2);
        annotationTypeOperations.updateType(initialAnnotationDefinition, advancedDefinition);
    }

    private void prepareInitialType() {
        initialAnnotationDefinition = buildAnnotationDefinition(DESCRIPTION,
                TYPE,
                prop1);
        initialAnnotationDefinition.setVersion(TypeUtils.FIRST_CERTIFIED_VERSION_VERSION);
    }

    private AnnotationTypeDefinition buildAnnotationDefinition(String description, String type, PropertyDefinition ... properties) {
        AnnotationTypeDefinition annotationTypeDefinition = new AnnotationTypeDefinition();
        annotationTypeDefinition.setDescription(description);
        annotationTypeDefinition.setType(type);
        annotationTypeDefinition.setHighestVersion(true);
        annotationTypeDefinition.setProperties(asList(properties));
        return annotationTypeDefinition;
    }

}