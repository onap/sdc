package org.openecomp.sdc.vendorsoftwareproduct.services;

public class QuestionnaireValidatorTest {
/*
    public static QuestionnaireValidator validator = new QuestionnaireValidator();

    @Test(expectedExceptions = CoreException.class)
    public void testAddSubEntityBeforeRoot_negative() {
        validator.addSubEntity(createComponent("componentId2"), CompositionEntityType.vsp);
    }

    @Test(dependsOnMethods = "testAddSubEntityBeforeRoot_negative")
    public void testAddRootEntity() {
        validator.addRootEntity(createVspQuestionnaireEntity());
    }

    @Test(dependsOnMethods = "testAddRootEntity", expectedExceptions = CoreException.class)
    public void testAddRootEntityWhenAlreadyExist_negative() {
        validator.addRootEntity(createVspQuestionnaireEntity());
    }

    @Test(dependsOnMethods = "testAddRootEntity")
    public void testAddSubEntity() {
        validator.addSubEntity(createComponent("componentId1"), CompositionEntityType.vsp);
    }

    @Test(dependsOnMethods = "testAddRootEntity", expectedExceptions = CoreException.class)
    public void testAddSubEntityWithNonExistingParentType() {
        validator.addSubEntity(new ComponentEntity("vspId1", new Version(0, 1), "componentId2"), CompositionEntityType.nic);
    }

    @Test(dependsOnMethods = "testAddSubEntity")
    public void testAddSubEntities() throws Exception {
        Collection<CompositionEntity> nics = new ArrayList<>();
        nics.add(createNic("nicId1", "componentId1"));
        nics.add(createNic("nicId2", "componentId1"));
        nics.add(createNic("nicId3", "componentId1"));

        validator.addSubEntities(nics, CompositionEntityType.component);
    }


    @Test(dependsOnMethods = "testAddSubEntities")
    public void testValidate() throws Exception {
        QuestionnaireValidationResult validationResult = validator.validate();
        Assert.assertTrue(validationResult.isValid());
    }

    private static VspQuestionnaireEntity createVspQuestionnaireEntity() {
        VspQuestionnaireEntity vspQuestionnaireEntity = new VspQuestionnaireEntity();
        vspQuestionnaireEntity.setId("vspId1");
        vspQuestionnaireEntity.setVersion(new Version(0, 1));
        vspQuestionnaireEntity.setQuestionnaireData("{\n" +
                "  \"name\": \"bla bla\"\n" +
                "}");
        return vspQuestionnaireEntity;
    }

    private static ComponentEntity createComponent(String componentId) {
        ComponentEntity component = new ComponentEntity("vspId1", new Version(0, 1), componentId);
        component.setQuestionnaireData("{\n" +
                "  \"name\": \"bla bla\"\n" +
                "}");
        return component;
    }

    private static CompositionEntity createNic(String nicId, String componentId) {
        NicEntity nic = new NicEntity("vspId1", new Version(0, 1), componentId, nicId);
        nic.setQuestionnaireData("{\n" +
                "  \"name\": \"bla bla\"\n" +
                "}");
        return nic;
    }*/
}