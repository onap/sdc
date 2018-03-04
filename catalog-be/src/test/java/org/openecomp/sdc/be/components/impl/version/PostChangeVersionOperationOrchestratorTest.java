package org.openecomp.sdc.be.components.impl.version;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PostChangeVersionOperationOrchestratorTest {

    private PostChangeVersionOperationOrchestrator testInstance;
    @Mock
    private PostChangeVersionOperation postChangeVersionOperation1;
    @Mock
    private PostChangeVersionOperation postChangeVersionOperation2;
    @Mock
    private PostChangeVersionOperation postChangeVersionOperation3;

    @Before
    public void setUp() throws Exception {
        testInstance = new PostChangeVersionOperationOrchestrator(asList(postChangeVersionOperation1, postChangeVersionOperation2, postChangeVersionOperation3));
    }

    @Test
    public void whenFirstPostOperationFails_doNotRunFollowingOperations() {
        ComponentInstance newVersion = new ComponentInstance();
        ComponentInstance prevVersion = new ComponentInstance();
        Resource container = new Resource();
        when(postChangeVersionOperation1.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(container, prevVersion, newVersion);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyZeroInteractions(postChangeVersionOperation2, postChangeVersionOperation3);
    }

    @Test
    public void whenAnyPostOperationFails_doNotRunFollowingOperations() {
        ComponentInstance newVersion = new ComponentInstance();
        ComponentInstance prevVersion = new ComponentInstance();
        Resource container = new Resource();
        when(postChangeVersionOperation1.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        when(postChangeVersionOperation2.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(container, prevVersion, newVersion);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyZeroInteractions(postChangeVersionOperation3);
    }

    @Test
    public void whenLastPostOperationFails_returnTheFailureResult() {
        ComponentInstance newVersion = new ComponentInstance();
        ComponentInstance prevVersion = new ComponentInstance();
        Resource container = new Resource();
        when(postChangeVersionOperation1.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        when(postChangeVersionOperation2.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        when(postChangeVersionOperation3.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(container, prevVersion, newVersion);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void whenAllOperationsSucceeds_returnOk() {
        ComponentInstance newVersion = new ComponentInstance();
        ComponentInstance prevVersion = new ComponentInstance();
        Resource container = new Resource();
        when(postChangeVersionOperation1.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        when(postChangeVersionOperation2.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        when(postChangeVersionOperation3.onChangeVersion(container, prevVersion, newVersion)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(container, prevVersion, newVersion);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

}