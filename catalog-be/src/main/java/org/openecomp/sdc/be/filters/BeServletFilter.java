/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.GsonBuilder;
import org.onap.logging.filter.base.AuditLogContainerFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.HealingJanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.MDC;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(1)
public class BeServletFilter extends AuditLogContainerFilter {

    @Context
    private HttpServletRequest sr;
    private static final Logger log = Logger.getLogger(BeServletFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (isLoggedRequest()) {
            try {
                super.filter(requestContext);
                // In case of 405 response code, this function is not entered, then
                // we'll process
                // the MDC fields and UUID during the response
                ThreadLocalsHolder.setMdcProcessed(true);
                // Timing HTTP request
                ThreadLocalsHolder.setRequestStartTime(System.currentTimeMillis());
                processMdcFields(requestContext);
                ThreadLocalsHolder.setUuid(MDC.get(ONAPLogConstants.MDCs.REQUEST_ID));
                inHttpRequest();
            } catch (Exception e) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Error during request filter");
                log.debug("Error during request filter: {} ", e);
            }
        }
    }

    @Override
    protected void additionalPreHandling(ContainerRequestContext containerRequestContext) {
        MDC.put(ILogConfiguration.MDC_REMOTE_HOST, sr.getRemoteAddr());
        MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, containerRequestContext.getHeaderString(Constants.X_ECOMP_SERVICE_ID_HEADER));
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_SEVERITY, String.valueOf(Severity.OK.getSeverityType()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (isLoggedRequest()) {
            try {
                super.filter(requestContext, responseContext);
                // Formatting the response in case of 405
                if (responseContext.getStatus() == Response.Status.METHOD_NOT_ALLOWED.getStatusCode()) {
                    ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_ALLOWED);
                    responseContext.setEntity(new GsonBuilder().setPrettyPrinting().create().toJson(responseFormat.getRequestError()));
                }

                if (ThreadLocalsHolder.isMdcProcessed()) {
                    // filter() was executed during request - this is the regular
                    // flow
                    responseContext.getHeaders().add(Constants.X_ECOMP_REQUEST_ID_HEADER, ThreadLocalsHolder.getUuid());
                }
                writeToJanusGraph(responseContext);

                outHttpResponse(responseContext);

            } catch (Exception e) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Error during request filter");
                log.debug("Error during response filter: {} ", e);
            } finally {
                // Cleaning up
                MDC.clear();
                ThreadLocalsHolder.cleanup();
            }
        }
    }

    private void writeToJanusGraph(ContainerResponseContext responseContext) {
        log.debug("Close transaction from filter");
        HealingJanusGraphDao janusGraphDao = getJanusGraphDao();
        if (janusGraphDao != null) {
            int status = responseContext.getStatus();
            if (status == Response.Status.OK.getStatusCode() ||
                    status == Response.Status.CREATED.getStatusCode() ||
                    status == Response.Status.NO_CONTENT.getStatusCode()) {
                janusGraphDao.commit();
                log.debug("Doing commit from filter");
            } else {
                janusGraphDao.rollback();
                log.debug("Doing rollback from filter");
            }
        }
    }

    private void processMdcFields(ContainerRequestContext requestContext) {
        // UserId for logging
        String userId = requestContext.getHeaderString(Constants.USER_ID_HEADER);
        MDC.put("userId", userId);

        String serviceInstanceID = requestContext.getHeaderString(Constants.X_ECOMP_SERVICE_ID_HEADER);
        MDC.put(ILogConfiguration.MDC_SERVICE_INSTANCE_ID, serviceInstanceID);

        MDC.put("remoteAddr", sr.getRemoteAddr());
        MDC.put("localAddr", sr.getLocalAddr());
    }

    private ComponentsUtils getComponentsUtils() {
        ServletContext context = this.sr.getSession().getServletContext();

        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ComponentsUtils.class);
    }

    private HealingJanusGraphDao getJanusGraphDao() {
        ServletContext context = this.sr.getSession().getServletContext();

        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(HealingJanusGraphDao.class);
    }

    // Extracted for purpose of clear method name, for logback %M parameter
    private void inHttpRequest() {
        log.info("{} {} {}", sr.getMethod(), sr.getRequestURI(), sr.getProtocol());
    }

    // Extracted for purpose of clear method name, for logback %M parameter
    private void outHttpResponse(ContainerResponseContext responseContext) {
        log.info("{} {} {} SC=\"{}\"", sr.getMethod(), sr.getRequestURI(), sr.getProtocol(), responseContext.getStatus());
    }

    private boolean isLoggedRequest() {
        boolean logRequest = true;
        Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
        String requestURI = sr.getRequestURI();
        if (requestURI != null && configuration.getUnLoggedUrls() != null) {
            logRequest = !configuration.getUnLoggedUrls().contains(requestURI);
        }
        return logRequest;
    }
}
