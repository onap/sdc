/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.filter.CadiFilter;
import org.openecomp.sdc.be.components.impl.CADIHealthCheck;
import org.openecomp.sdc.be.config.CadiFilterParams;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Supplier;

@Priority(2)
public class BeCadiServletFilter extends CadiFilter {

    private static final Logger log = Logger.getLogger(BeCadiServletFilter.class);
    private ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();
    private static final String BE_CADI_SERVICE_FILTER = "BeCadiServletFilter: ";


    public BeCadiServletFilter() {
        super();
        log.debug(BE_CADI_SERVICE_FILTER);
    }

    /**
     * This constructor to be used when directly constructing and placing in HTTP Engine
     *
     * @param access
     * @param moreTafLurs
     * @throws ServletException
     */
    public BeCadiServletFilter(Access access, Object... moreTafLurs) throws ServletException {
        super(access, moreTafLurs);
        log.debug(BE_CADI_SERVICE_FILTER);
    }


    /**
     * Use this to pass in a PreContructed CADI Filter, but with initializing... let Servlet do it
     *
     * @param init
     * @param access
     * @param moreTafLurs
     * @throws ServletException
     */
    public BeCadiServletFilter(boolean init, PropAccess access, Object... moreTafLurs) throws ServletException {

        super(init, access, moreTafLurs);
        log.debug(BE_CADI_SERVICE_FILTER);
    }

    private void checkIfNullProperty(String key, String value) {
        /* When value is null, so not defined in application.properties
           set nothing in System properties */
        if (value != null) {
            /* Ensure that any properties already defined in System.prop by JVM params
                won't be overwritten by Spring application.properties values */
            System.setProperty(key, System.getProperty(key, value));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        // set some properties in System so that Cadi filter will find its config
        // The JVM values set will always overwrite the Spring ones.
        CadiFilterParams cadiFilterParams = configurationManager.getConfiguration().getCadiFilterParams();
        checkIfNullProperty(Config.HOSTNAME, cadiFilterParams.getHostname());
        log.debug("BeCadiServletFilter: HOSTNAME", cadiFilterParams.getHostname());

        checkIfNullProperty(Config.CADI_KEYFILE, cadiFilterParams.getCadi_keyfile());
        checkIfNullProperty(Config.CADI_LOGLEVEL, cadiFilterParams.getCadi_loglevel());


        checkIfNullProperty(Config.CADI_LATITUDE, cadiFilterParams.getAFT_LATITUDE());
        checkIfNullProperty(Config.CADI_LONGITUDE, cadiFilterParams.getAFT_LONGITUDE());

        checkIfNullProperty(Config.AAF_URL, cadiFilterParams.getAaf_url());
        //checkIfNullProperty(Config.AAF_LOCATE_URL, cadiFilterParams.getAafLocateUrl());
        checkIfNullProperty(Config.AAF_APPID, cadiFilterParams.getAaf_id());
        checkIfNullProperty(Config.AAF_APPPASS, cadiFilterParams.getAaf_password());
        checkIfNullProperty(Config.AAF_ENV, cadiFilterParams.getAFT_ENVIRONMENT());

        checkIfNullProperty(Config.CADI_X509_ISSUERS, cadiFilterParams.getCadiX509Issuers());
        checkIfNullProperty(Config.CADI_TRUSTSTORE, cadiFilterParams.getCadi_truststore());
        checkIfNullProperty(Config.CADI_TRUSTSTORE_PASSWORD, cadiFilterParams.getCadi_truststore_password());
        super.init(filterConfig);
        log.debug("BeCadiServletFilter finishing init(), Current status of CADI would be UP");
        if (!isNeedAuth()) {
            CADIHealthCheck.getCADIHealthCheckInstance().setIsCADIUp(HealthCheckInfo.HealthCheckStatus.DOWN);
        } else {
            CADIHealthCheck.getCADIHealthCheckInstance().setIsCADIUp(HealthCheckInfo.HealthCheckStatus.UP);
        }
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (ThreadLocalsHolder.isExternalRequest() && isNeedAuth()) {
            log.debug("doFilter: {}", request.getContentType());
            HttpServletRequest hreq = (HttpServletRequest) request;
            log.debug("Need aaf authentication : {}", hreq);
            ThreadLocalUtils threadLocalUtils = getThreadLocalUtils(((HttpServletRequest) request).getSession().getServletContext());
            threadLocalUtils.setUserContext((HttpServletRequest) request);
            super.doFilter(request, response, chain);
        } else {
            log.debug("No need aaf authentication");
            chain.doFilter(request, response);
        }
    }

    private boolean isNeedAuth() {
        return configurationManager.getConfiguration().getAafAuthNeeded();
    }


    ThreadLocalUtils getThreadLocalUtils(ServletContext context) {
        return getClassFromWebAppContext(context, () -> ThreadLocalUtils.class);
    }

    <T> T getClassFromWebAppContext(ServletContext context, Supplier<Class<T>> businessLogicClassGen) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(businessLogicClassGen.get());
    }


}















