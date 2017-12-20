package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.core.zusammen.plugin.collaboration.TestUtils;
import org.openecomp.core.zusammen.plugin.dao.ElementStageRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElementStageStoreImplTest {
  private static final UserInfo USER = new UserInfo("user");
  private static final SessionContext context = TestUtils.createSessionContext(USER, "test");
  private static final ElementContext elementContext =
      TestUtils.createElementContext(new Id(), new Id());

  @Mock
  private ElementStageRepository elementStageRepositoryMock;
  @Spy
  private ElementStageStoreImpl elementStageStore;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(elementStageStore.getElementStageRepository(anyObject()))
        .thenReturn(elementStageRepositoryMock);
  }

  @Test
  public void testListIds() throws Exception {

  }

  @Test
  public void testGet() throws Exception {

  }

  @Test
  public void testGetConflicted() throws Exception {

  }

  @Test
  public void testHasConflicts() throws Exception {

  }

  @Test
  public void testListConflictedDescriptors() throws Exception {

  }

  @Test
  public void testCreate() throws Exception {

  }

  @Test
  public void testDelete() throws Exception {

  }

  @Test
  public void testResolveConflictWhenNotStaged() throws Exception {
    doReturn(Optional.empty())
        .when(elementStageRepositoryMock).get(anyObject(), anyObject(), anyObject());
    elementStageStore
        .resolveConflict(context, elementContext, new ElementEntity(new Id()), Resolution.YOURS);
  }

  @Test
  public void testResolveConflictWhenNotConflicted() throws Exception {
    Id elementId = new Id();
    StageEntity<ElementEntity> stagedElement =
        new StageEntity<>(new ElementEntity(elementId), new Date());
    doReturn(Optional.of(stagedElement))
        .when(elementStageRepositoryMock).get(anyObject(), anyObject(), anyObject());
    elementStageStore
        .resolveConflict(context, elementContext, new ElementEntity(elementId), Resolution.YOURS);
  }

  @Test
  public void testResolveConflictByYours() throws Exception {
    Id elementId = new Id();
    StageEntity<ElementEntity> stagedElement =
        new StageEntity<>(new ElementEntity(elementId), new Date());
    stagedElement.setAction(Action.UPDATE);
    stagedElement.setConflicted(true);

    doReturn(Optional.of(stagedElement))
        .when(elementStageRepositoryMock).get(anyObject(), anyObject(), anyObject());

    elementStageStore
        .resolveConflict(context, elementContext, new ElementEntity(elementId), Resolution.YOURS);

    verify(elementStageRepositoryMock).markAsNotConflicted(same(context),
        eq(new ElementEntityContext(USER.getUserName(), elementContext)),
        same(stagedElement.getEntity()), same(Action.IGNORE));
  }

  @Test
  public void testResolveConflictByYoursWithRelated() throws Exception {
    Id elementId = new Id();
    StageEntity<ElementEntity> stagedElement =
        new StageEntity<>(new ElementEntity(elementId), new Date());
    stagedElement.setAction(Action.UPDATE);
    stagedElement.setConflicted(true);
    ElementEntity relatedElement1 = new ElementEntity(new Id());
    ElementEntity relatedElement2 = new ElementEntity(new Id());
    ElementEntity relatedElement3 = new ElementEntity(new Id());
    Set<ElementEntity> relatedElements = new HashSet<>();
    relatedElements.add(relatedElement1);
    relatedElements.add(relatedElement2);
    relatedElements.add(relatedElement3);
    stagedElement.setConflictDependents(relatedElements);

    doReturn(Optional.of(stagedElement))
        .when(elementStageRepositoryMock).get(anyObject(), anyObject(), anyObject());

    elementStageStore
        .resolveConflict(context, elementContext, new ElementEntity(elementId), Resolution.YOURS);

    ElementEntityContext elementEntityContext =
        new ElementEntityContext(USER.getUserName(), elementContext);
    verify(elementStageRepositoryMock).markAsNotConflicted(same(context), eq(elementEntityContext),
        same(stagedElement.getEntity()), same(Action.IGNORE));
    verify(elementStageRepositoryMock).markAsNotConflicted(same(context), eq(elementEntityContext),
        same(relatedElement1), same(Action.IGNORE));
    verify(elementStageRepositoryMock).markAsNotConflicted(same(context), eq(elementEntityContext),
        same(relatedElement2), same(Action.IGNORE));
    verify(elementStageRepositoryMock).markAsNotConflicted(same(context), eq(elementEntityContext),
        same(relatedElement3), same(Action.IGNORE));
  }

  @Test
  public void testResolveConflictByTheirs() throws Exception {

  }

  @Test
  public void testResolveConflictByTheirsWithRelated() throws Exception {

  }

}