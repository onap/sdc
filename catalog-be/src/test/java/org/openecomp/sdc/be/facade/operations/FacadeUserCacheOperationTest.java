package org.openecomp.sdc.be.facade.operations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.user.UserMessage;
import org.openecomp.sdc.be.user.UserOperationEnum;

@RunWith(MockitoJUnitRunner.class)
public class FacadeUserCacheOperationTest {
    @Mock
    private DmaapProducer msProducer;
    @Captor
    private ArgumentCaptor<UserMessage> messageCaptor;
    
    private UserOperation userCacheOperation;
    
    @Before
    public void setUp() {
        userCacheOperation = new UserOperation(msProducer);
    }

    @Test
    public void testUpdate() {
        userCacheOperation.updateUserCache(UserOperationEnum.CREATE, "id", "role");
        Mockito.verify(msProducer).pushMessage(messageCaptor.capture());
        
        UserMessage message = messageCaptor.getValue();
        
        assertThat(message.getOperation()).isEqualTo(UserOperationEnum.CREATE);
        assertThat(message.getUserId()).isEqualTo("id");
        assertThat(message.getRole()).isEqualTo("role");
    }

}
