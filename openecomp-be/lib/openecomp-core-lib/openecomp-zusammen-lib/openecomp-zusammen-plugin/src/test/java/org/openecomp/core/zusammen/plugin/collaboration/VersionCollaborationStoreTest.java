package org.openecomp.core.zusammen.plugin.collaboration;

public class VersionCollaborationStoreTest {/*
  private static final String TENANT = "test";
  private static final String USER = "ItemStateStoreTest_user";
  private static final SessionContext context =
      TestUtils.createSessionContext(new UserInfo(USER), TENANT);

  @Mock
  private VersionDao versionDaoMock;
  @Mock
  private ElementStore elementCollaborationStore;
  @Spy
  @InjectMocks
  private VersionStore versionCollaborationStore;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(versionCollaborationStore.getVersionDao(anyObject())).thenReturn(versionDaoMock);
  }
*//*
  @Test
  public void testListPrivateItemVersions() throws Exception {
    testListItemVersions(Space.PRIVATE, USER);
  }

  @Test
  public void testListPublicItemVersions() throws Exception {
    testListItemVersions(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE);
  }

  @Test
  public void testIsPrivateItemVersionExist() throws Exception {
    testIsItemVersionExist(Space.PRIVATE, USER);
  }

  @Test
  public void testIsPublicItemVersionExist() throws Exception {
    testIsItemVersionExist(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE);
  }

  @Test
  public void testIsItemVersionExistWhenNot() throws Exception {
    Id itemId = new Id();
    Id versionId = new Id();
    doReturn(Optional.empty()).when(versionDaoMock).get(context, USER, itemId, versionId);

    boolean itemExist =
        versionCollaborationStore.isItemVersionExist(context, Space.PRIVATE, itemId, versionId);
    Assert.assertFalse(itemExist);
  }

  @Test
  public void testGetPrivateItemVersion() throws Exception {
    testGetItemVersion(Space.PRIVATE, USER);
  }

  @Test
  public void testGetPublicItemVersion() throws Exception {
    testGetItemVersion(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE);
  }


  @Test
  public void testGetNonExistingItemVersion() throws Exception {
    Id itemId = new Id();
    Id versionId = new Id();
    doReturn(Optional.empty()).when(versionDaoMock).get(context, USER, itemId, versionId);

    ItemVersion itemVersion =
        versionCollaborationStore.getItemVersion(context, Space.PRIVATE, itemId, versionId);
    Assert.assertNull(itemVersion);
  }*//*

  @Test
  public void testCreatePrivateItemVersion() throws Exception {
    testCreateItemVersion(Space.PRIVATE, USER, null);
  }

  @Test
  public void testCreatePrivateItemVersionBasedOn() throws Exception {
    testCreateItemVersion(Space.PRIVATE, USER, new Id());
  }

  @Test
  public void testCreatePublicItemVersion() throws Exception {
    testCreateItemVersion(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE, null);
  }

  @Test
  public void testCreatePublicItemVersionBasedOn() throws Exception {
    testCreateItemVersion(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE, new Id());
  }

  @Test
  public void testUpdatePrivateItemVersion() throws Exception {
    testUpdateItemVersion(Space.PRIVATE, USER);
  }

  @Test
  public void testUpdatePublicItemVersion() throws Exception {
    testUpdateItemVersion(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE);
  }

  @Test
  public void testDeletePrivateItemVersion() throws Exception {
    testDeleteItemVersion(Space.PRIVATE, USER);
  }

  @Test
  public void testDeletePublicItemVersion() throws Exception {
    testDeleteItemVersion(Space.PUBLIC, ZusammenPluginConstants.PUBLIC_SPACE);
  }

  @Test
  public void testPublishItemVersionWhenNotDirty() throws Exception {
    Id itemId = new Id();
    ItemVersion version = TestUtils.createItemVersion(new Id(), null, "v1", false);
    doReturn(Optional.of(version)).when(versionDaoMock).get(context, USER, itemId, version.getId());

    versionCollaborationStore.publishItemVersion(context, itemId, version.getId(), "message");

  }
*//*
  private void testIsItemVersionExist(Space space, String spaceName) {
    Id itemId = new Id();
    ItemVersion retrievedVersion = TestUtils.createItemVersion(new Id(), null, "v1");
    doReturn(Optional.of(retrievedVersion)).when(versionDaoMock)
        .get(context, spaceName, itemId, retrievedVersion.getId());

    boolean itemExist =
        versionCollaborationStore
            .isItemVersionExist(context, space, itemId, retrievedVersion.getId());
    Assert.assertTrue(itemExist);
  }

  private void testGetItemVersion(Space space, String spaceName) throws Exception {
    Id itemId = new Id();
    ItemVersion retrievedVersion = TestUtils.createItemVersion(new Id(), null, "v1");
    doReturn(Optional.of(retrievedVersion)).when(versionDaoMock)
        .get(context, spaceName, itemId, retrievedVersion.getId());

    ItemVersion itemVersion =
        versionCollaborationStore.getItemVersion(context, space, itemId, retrievedVersion.getId());
    Assert.assertEquals(itemVersion, retrievedVersion);
  }

  private void testListItemVersions(Space space, String spaceName) {
    Id itemId = new Id();
    ItemVersion v1 = TestUtils.createItemVersion(new Id(), null, "v1");
    ItemVersion v2 = TestUtils.createItemVersion(new Id(), v1.getId(), "v2");
    ItemVersion v3 = TestUtils.createItemVersion(new Id(), v2.getId(), "v3");
    List<ItemVersion> retrievedVersions = Arrays.asList(v1, v2, v3);
    doReturn(retrievedVersions).when(versionDaoMock).list(context, spaceName, itemId);

    Collection<ItemVersion> itemVersions =
        versionCollaborationStore.listItemVersions(context, space, itemId);
    Assert.assertEquals(itemVersions, retrievedVersions);
  }*//*

  private void testCreateItemVersion(Space space, String spaceName, Id baseId) {
    Id itemId = new Id();
    ItemVersion v1 = TestUtils.createItemVersion(new Id(), baseId, "v1", false);
    List<ElementEntity> baseVersionElements = mockVersionElements(spaceName, itemId, baseId);

    ArgumentCaptor<ItemVersion> versionCaptor = ArgumentCaptor.forClass(ItemVersion.class);

    Date creationTime = new Date();
    versionCollaborationStore
        .createItemVersion(context, space, itemId, baseId, v1.getId(), v1.getData(), creationTime);

    verify(versionDaoMock).create(eq(context), eq(spaceName), eq(itemId), versionCaptor.capture());
    //baseId, v1.getId(),v1.getData(), creationTime);

    ItemVersion capturedVersion = versionCaptor.getValue();
    Assert.assertEquals(baseId, capturedVersion.getBaseId());
    Assert.assertEquals(v1.getId(), capturedVersion.getId());
    Assert.assertEquals(v1.getData(), capturedVersion.getData());
    Assert.assertEquals(creationTime, capturedVersion.getCreationTime());
*//*    verify(versionDaoMock)
        .create(anyObject(), anyObject(), anyObject(), anyObject(), anyObject(), anyObject(),
            anyObject());*//*

*//*    if (baseId != null) {
      baseVersionElements.forEach(element ->
          verify(elementCollaborationStore).create(eq(context),
              eq(new ElementEntityContext(spaceName, itemId, v1.getId())),
              eq(element)));
    } else {
      verifyZeroInteractions(elementCollaborationStore);
    }*//*
  }

  private void testUpdateItemVersion(Space space, String spaceName) {
    Id itemId = new Id();
    ItemVersion retrievedVersion = TestUtils.createItemVersion(new Id(), null, "v1", false);
    doReturn(Optional.of(retrievedVersion)).when(versionDaoMock)
        .get(context, spaceName, itemId, retrievedVersion.getId());

    ItemVersionData updatedData = new ItemVersionData();
    updatedData.setInfo(TestUtils.createInfo("v1 updated"));
    updatedData.setRelations(
        Arrays.asList(new Relation(), new Relation(), new Relation(), new Relation()));
    versionCollaborationStore.updateItemVersion(
        context, space, itemId, retrievedVersion.getId(), updatedData, new Date());

    *//*verify(versionDaoMock)
        .update(context, spaceName, itemId, retrievedVersion.getId(), updatedData, modificationTime);*//*
    verify(versionDaoMock)
        .update(anyObject(), anyObject(), anyObject(), anyObject());

  }

  private void testDeleteItemVersion(Space space, String spaceName) {
    Id itemId = new Id();
    Id versionId = new Id();

    List<ElementEntity> versionElements = mockVersionElements(spaceName, itemId, versionId);
    versionCollaborationStore.deleteItemVersion(context, space, itemId, versionId);

*//*    versionElements.forEach(element ->
        verify(elementCollaborationStore).delete(eq(context),
            eq(new ElementEntityContext(spaceName, itemId, versionId)),
            eq(element)));*//*
    verify(versionDaoMock).delete(context, spaceName, itemId, versionId);
  }

  private List<ElementEntity> mockVersionElements(String spaceName, Id itemId, Id versionId) {
    ElementEntity elm1 = new ElementEntity(new Id());
    ElementEntity elm2 = new ElementEntity(new Id());
    List<ElementEntity> baseVersionElements = Arrays.asList(elm1, elm2);
*//*    doReturn(baseVersionElements).when(elementCollaborationStore)
        .list(eq(context), eq(new ElementEntityContext(spaceName, itemId, versionId)));*//*
    return baseVersionElements;
  }*/
}