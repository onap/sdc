package org.openecomp.sdc.fe.servlets;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.impl.MdcData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

public abstract class LoggingServlet extends BasicServlet {

    private static final Logger log = LoggerFactory.getLogger(BasicServlet.class.getName());
    private static final Cache<String, MdcData> mdcDataCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    /**
     * log incoming requests
     * @param httpRequest the http request
     */
    protected void logFeRequest(HttpServletRequest httpRequest) {

        MDC.clear();

        Long transactionStartTime = System.currentTimeMillis();
        String uuid = httpRequest.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
        String serviceInstanceID = httpRequest.getHeader(Constants.X_ECOMP_SERVICE_ID_HEADER);

        if (uuid != null && uuid.length() > 0) {
            String userId = httpRequest.getHeader(Constants.USER_ID_HEADER);

            String remoteAddr = httpRequest.getRemoteAddr();
            String localAddr = httpRequest.getLocalAddr();

            mdcDataCache.put(uuid, new MdcData(serviceInstanceID, userId, remoteAddr, localAddr, transactionStartTime));

            updateMdc(uuid, serviceInstanceID, userId, remoteAddr, localAddr, null);
        }
        inHttpRequest(httpRequest);
    }

    /**
     * log response
     * @param request orig request
     * @param response returned response
     */
    protected void logFeResponse(HttpServletRequest request, Response response) {
        String uuid = request.getHeader(Constants.X_ECOMP_REQUEST_ID_HEADER);
        String transactionRoundTime = null;

        if (uuid != null) {
            MdcData mdcData = mdcDataCache.getIfPresent(uuid);
            if (mdcData != null) {
                Long transactionStartTime = mdcData.getTransactionStartTime();
                if (transactionStartTime != null) {// should'n ever be null, but
                    // just to be defensive
                    transactionRoundTime = Long.toString(System.currentTimeMillis() - transactionStartTime);
                }
                updateMdc(uuid, mdcData.getServiceInstanceID(), mdcData.getUserId(), mdcData.getRemoteAddr(), mdcData.getLocalAddr(), transactionRoundTime);
            }
        }
        outHttpResponse(response);

        MDC.clear();
    }

    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     * @param httpRequest http request
     */
    protected abstract void inHttpRequest(HttpServletRequest httpRequest) ;


    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     * @param response http response
     */
    protected abstract void outHttpResponse(Response response);

    /**
     * update mdc with values from the request
     * @param uuid service uuid
     * @param serviceInstanceID serviceInstanceID
     * @param userId userId
     * @param remoteAddr remoteAddr
     * @param localAddr localAddr
     * @param transactionStartTime transactionStartTime
     */
    private void updateMdc(String uuid, String serviceInstanceID, String userId, String remoteAddr, String localAddr, String transactionStartTime) {
        MDC.put("uuid", uuid);
        MDC.put("serviceInstanceID", serviceInstanceID);
        MDC.put("userId", userId);
        MDC.put("remoteAddr", remoteAddr);
        MDC.put("localAddr", localAddr);
        MDC.put("timer", transactionStartTime);
    }
}
