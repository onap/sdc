/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.openecomp.sdc.be.filters;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerResponseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(MockitoExtension.class)
class BeServletFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletContext servletContext;
    @Mock
    private WebAppContextWrapper webAppContextWrapper;
    @Mock
    private WebApplicationContext webApplicationContext;
    @Mock
    private HealingJanusGraphDao janusGraphDao;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ContainerResponseContext responseContext;
    @InjectMocks
    private BeServletFilter filter;

    @BeforeEach
    void wireApplicationContext() {
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(HealingJanusGraphDao.class)).thenReturn(janusGraphDao);
        lenient().when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
    }

    @Test
    void answersWith500WhenTheCommitOfACreatedResponseFails() {
        when(responseContext.getStatus()).thenReturn(201);
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.JANUSGRAPH_SCHEMA_VIOLATION);
        ResponseFormat generalError = new ResponseFormat(500);
        generalError.setServiceException(new ServiceException("SVC4000", "Error: Internal Server Error. Please try again later.", null));
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(generalError);

        filter.writeToJanusGraph(responseContext);

        verify(responseContext).setStatus(500);
        verify(responseContext).setEntity(contains("SVC4000"));
    }

    @Test
    void keepsTheResponseWhenTheCommitSucceeds() {
        when(responseContext.getStatus()).thenReturn(201);
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);

        filter.writeToJanusGraph(responseContext);

        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void rollsBackAnErrorResponseWithoutCommitting() {
        when(responseContext.getStatus()).thenReturn(404);

        filter.writeToJanusGraph(responseContext);

        verify(janusGraphDao).rollback();
        verify(janusGraphDao, never()).commit();
        verify(responseContext, never()).setStatus(anyInt());
    }
}
