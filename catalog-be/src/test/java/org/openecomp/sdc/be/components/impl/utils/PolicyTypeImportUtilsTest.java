package org.openecomp.sdc.be.components.impl.utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.PolicyTypeBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class PolicyTypeImportUtilsTest {

    private static final String UNIQUE_ID_EXSISTS = "uniqueId";

    @Test
    public void isPolicyTypesEquals_whenBothTypesAreNull_returnTrue() {
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(null, null)).isTrue();
    }

    @Test
    public void isPolicyTypesEquals_whenOneTypeIsNull_returnFalse() {
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(new PolicyTypeDefinition(), null)).isFalse();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(null, new PolicyTypeDefinition())).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenTypesIsSameObject_returnTrue() {
        PolicyTypeDefinition policyType = new PolicyTypeDefinition();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(policyType, policyType)).isTrue();
    }

    @Test
    public void isPolicyTypesEquals_allFieldsEquals_returnTrue() {
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(createPolicyTypeWithAllFields(), createPolicyTypeWithAllFields())).isTrue();
    }

    @Test
    public void isPolicyTypeEquals_whenTypesAreDifferentInANonCompareFields_returnTrue() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setOwnerId("ownerIdNew");
        type2.setModificationTime(System.currentTimeMillis());
        type2.setCreationTime(System.currentTimeMillis());
        type2.setUniqueId("uniqueIdNew");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isTrue();
    }

    @Test
    public void isPolicyTypesEquals_whenTypeIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setType("newType");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void whenNameIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setName("newName");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void whenIconIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setIcon("newIcon");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenDescriptionIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setDescription("newDescription");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenTargetsAreDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setTargets(new ArrayList<>());
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenDerivedFromIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setDerivedFrom("newDerivedFrom");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenVersionIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setVersion("2.0");
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypesEquals_whenMetadataIsDifferent_returnFalse() {
        PolicyTypeDefinition type1 = createPolicyTypeWithAllFields();
        PolicyTypeDefinition type2 = createPolicyTypeWithAllFields();
        type2.setMetadata(ImmutableMap.of("newKey", "newVal"));
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void whenBothPropertiesListNull_returnTrue() {
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals( new PolicyTypeDefinition(),  new PolicyTypeDefinition())).isTrue();
    }

    @Test
    public void whenOnePropertiesListIsNullAndSecondOneIsEmpty_returnTrue() {
        PolicyTypeDefinition noProperties = new PolicyTypeDefinition();
        PolicyTypeDefinition emptyProperties = new PolicyTypeBuilder().setProperties(Collections.emptyList()).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(noProperties, emptyProperties)).isTrue();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(emptyProperties, noProperties)).isTrue();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesListNotOfSameSize_returnFalse() {
        PolicyTypeDefinition noProperties = new PolicyTypeDefinition();
        PolicyTypeDefinition emptyProperties = new PolicyTypeBuilder().setProperties(Collections.emptyList()).build();
        PolicyTypeDefinition oneProp = new PolicyTypeBuilder().setProperties(Collections.singletonList(createPropertyDefinitionWithAllFields("prop1"))).build();
        PolicyTypeDefinition twoProps = new PolicyTypeBuilder().setProperties(Arrays.asList(createPropertyDefinitionWithAllFields("prop1"),
                                                                                            createPropertyDefinitionWithAllFields("prop2")))
                                                                .build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(noProperties, oneProp)).isFalse();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(emptyProperties, oneProp)).isFalse();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(twoProps, oneProp)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesSamePropertiesList_returnTrue() {
        List<PropertyDefinition> propList = Collections.singletonList(createPropertyDefinitionWithAllFields("prop1"));
        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(propList).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(propList).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isTrue();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesListFieldsEquals_returnTrue() {
        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(createPropertyDefinitionWithAllFields("prop1"))).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(createPropertyDefinitionWithAllFields("prop1"))).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isTrue();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesListDifferentInANonComparedFields_returnTrue() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffNonComparedFields = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffNonComparedFields.setOwnerId("newOwner");
        prop1DiffNonComparedFields.setValue("newVal");
        prop1DiffNonComparedFields.setConstraints(null);
        prop1DiffNonComparedFields.setUniqueId("newId");
        prop1DiffNonComparedFields.setHidden(true);

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffNonComparedFields)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isTrue();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesNotOfSameName_returnFalse() {
        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(createPropertyDefinitionWithAllFields("prop1"))).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(createPropertyDefinitionWithAllFields("prop2"))).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesNotOFSameType_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1TypeInteger = createPropertyDefinitionWithAllFields("prop1");
        prop1TypeInteger.setType("integer");

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1TypeInteger)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesNotOfSameDefaultVal_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffDefault = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffDefault.setDefaultValue("newDefVal");

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffDefault)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesNotOfSameSchema_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffSchema = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffSchema.setSchema(null);

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffSchema)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesIsPasswordFieldNotSame_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffIsPassword = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffIsPassword.setPassword(!prop1.isPassword());

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffIsPassword)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesIsRequiredFieldNotSame_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffIsRequired = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffIsRequired.setRequired(!prop1.isRequired());

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffIsRequired)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    @Test
    public void isPolicyTypeEquals_whenPropertiesNotSameDescription_returnFalse() {
        PropertyDefinition prop1 = createPropertyDefinitionWithAllFields("prop1");
        PropertyDefinition prop1DiffDescription = createPropertyDefinitionWithAllFields("prop1");
        prop1DiffDescription.setDescription("newDescription");

        PolicyTypeDefinition type1 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1)).build();
        PolicyTypeDefinition type2 = new PolicyTypeBuilder().setProperties(Collections.singletonList(prop1DiffDescription)).build();
        assertThat(PolicyTypeImportUtils.isPolicyTypesEquals(type1, type2)).isFalse();
    }

    private PolicyTypeDefinition createPolicyTypeWithAllFields() {
        return new PolicyTypeBuilder()
                .setType("type1")
                .setDerivedFrom("derivedFrom")
                .setVersion("1.0")
                .setDescription("description")
                .setUniqueId("id1")
                .setHighestVersion(true)
                .setModificationTime(System.currentTimeMillis())
                .setCreationTime(System.currentTimeMillis())
                .setTargets(getTargets())
                .setOwner("owner")
                .setName("name")
                .setIcon("icon")
                .setMetadata(ImmutableMap.of("key1", "val1", "key2", "val2"))
                .build();
    }

    private PropertyDefinition createPropertyDefinitionWithAllFields(String name) {
        return new PropertyDataDefinitionBuilder()
                .setConstraints(Arrays.asList(new GreaterThanConstraint("abc"), new MinLengthConstraint(5)))
                .setUniqueId("uid")
                .setDefaultValue("val1")
                .setType("string")
                .setValue("val1")
                .setName(name)
                .setSchemaType("string")
                .setOwnerId("owner")
                .setStatus("status")
                .setDescription("description")
                .setIsPassword(false)
                .setIsRequired(false)
                .build();
    }

    private List<String> getTargets() {

        return Collections.singletonList(UNIQUE_ID_EXSISTS);
    }
}