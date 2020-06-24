package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import static org.mockito.Mockito.mock;

public abstract class IArtifactValidatorExecutorContract {

    protected abstract IArtifactValidatorExecutor createTestSubject(
        JanusGraphDao janusGraphDao,
        ToscaOperationFacade toscaOperationFacade
    );

    private IArtifactValidatorExecutor createTestSubject() {
        return createTestSubject(mock(JanusGraphDao.class), mock(ToscaOperationFacade.class));
    }

    @Test
    public void testExecuteValidations() {
        Assertions.assertThrows(NullPointerException.class, () ->
            // Initially no outputFilePath was passed to this function (hence it is set to null)
            // TODO: Fix this null and see if the argument is used by this function
            createTestSubject().executeValidations(null)
        );
    }
}

