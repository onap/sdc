package org.openecomp.sdc.common.log.elements;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcUtilBase;
import org.slf4j.MarkerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static java.net.HttpURLConnection.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by dd4296 on 12/19/2017.
 *
 * test get partner name
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerAsdcUtilTest {

    private final String chromeUserAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
    private final String firefoxUserAgent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.13) Gecko/20080313 Firefox";
    private final String explorerUserAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)";

    private final String testUserId = "ml007";
    private final String urlWithUserName = "/api/v1/user/" + testUserId;
    private final String xOnapPartnerName = "ml007";

    class SdcEelfAuditTest extends LoggerSdcAudit {

        SdcEelfAuditTest(Class<?> clazz) {
            super(clazz);
        }

        public String getPartnerName(String userAgent, String userID, String url, String xOnapPartnerName) {
            return super.getPartnerName(userAgent, userID, url, xOnapPartnerName);
        }
    }

    @Mock
    private ContainerRequestContext requestContext;
    @Mock
    UriInfo uriInfo;

    private SdcEelfAuditTest asdcEelfAudit;

    @Before
    public void Init () throws URISyntaxException {
        asdcEelfAudit = new SdcEelfAuditTest(LoggerAsdcUtilTest.class);
        when(requestContext.getHeaderString(anyString())).thenReturn("ab2222");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        URI uri = new URI("http:/abc.com/getId");
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uriInfo.getBaseUri()).thenReturn(uri);

    }

    @Test
    public void extract_user_id_from_userAgentTest() {

        String userIdChrome = asdcEelfAudit.getPartnerName(chromeUserAgent, "", "", "");
        assertEquals(userIdChrome.toLowerCase(), "chrome_FE".toLowerCase());

        String userIdFireFox = asdcEelfAudit.getPartnerName(firefoxUserAgent, "", "", "");
        assertEquals(userIdFireFox.toLowerCase(), "firefox_FE".toLowerCase());

        String userIdIE = asdcEelfAudit.getPartnerName(explorerUserAgent, "", "", "");
        assertEquals(userIdIE.toLowerCase(), "explorer_FE".toLowerCase());
    }

    @Test
    public void extract_user_id_from_urlTest() {

        String userId = asdcEelfAudit.getPartnerName("", "", urlWithUserName, "");
        assertEquals(testUserId, userId);
    }

    @Test
    public void extract_user_id_from_paramTest() {

        String userId = asdcEelfAudit.getPartnerName("", testUserId, "", "");
        assertEquals(userId, testUserId);
    }

    @Test
    public void extract_user_id_from_xOnapPartnerNameTest() {

        String userId = asdcEelfAudit.getPartnerName("", "", "", xOnapPartnerName);
        assertEquals(userId, testUserId);
    }

    @Test
    public void extract_user_id_priorityTest() {

        String userId = asdcEelfAudit.getPartnerName(chromeUserAgent, testUserId, urlWithUserName, xOnapPartnerName);
        assertEquals(userId, testUserId);

        String userIdUrl = asdcEelfAudit.getPartnerName(chromeUserAgent, "", urlWithUserName, "");
        assertEquals(userIdUrl, testUserId);

        String userIdUserAgent = asdcEelfAudit.getPartnerName(chromeUserAgent, "", "", "");
        assertEquals(userIdUserAgent.toLowerCase(), "chrome_FE".toLowerCase());
    }

    @Test
    public void check_http_error_convert_to_eelf_code() {
        class LoggerSdcUtilBaseTest extends LoggerSdcUtilBase {
            public EcompLoggerErrorCode convertHttpCodeToErrorCode(int httpResponseCode) {
                return super.convertHttpCodeToErrorCode(httpResponseCode);
            }
        }

        LoggerSdcUtilBaseTest utilBase = new LoggerSdcUtilBaseTest();

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_BAD_REQUEST), EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_UNAUTHORIZED), EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_NOT_FOUND), EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_CLIENT_TIMEOUT), EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_GONE), EcompLoggerErrorCode.AVAILABILITY_TIMEOUTS_ERROR);

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_PAYMENT_REQUIRED), EcompLoggerErrorCode.PERMISSION_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_FORBIDDEN), EcompLoggerErrorCode.PERMISSION_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_BAD_METHOD), EcompLoggerErrorCode.PERMISSION_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_PROXY_AUTH), EcompLoggerErrorCode.PERMISSION_ERROR);

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_NOT_ACCEPTABLE), EcompLoggerErrorCode.DATA_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_LENGTH_REQUIRED), EcompLoggerErrorCode.DATA_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_PRECON_FAILED), EcompLoggerErrorCode.DATA_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_REQ_TOO_LONG), EcompLoggerErrorCode.DATA_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_ENTITY_TOO_LARGE), EcompLoggerErrorCode.DATA_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_UNSUPPORTED_TYPE), EcompLoggerErrorCode.DATA_ERROR);

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_CONFLICT), EcompLoggerErrorCode.SCHEMA_ERROR);

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_OK), EcompLoggerErrorCode.SUCCESS);

        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_INTERNAL_ERROR), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_NOT_IMPLEMENTED), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_BAD_GATEWAY), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_UNAVAILABLE), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_GATEWAY_TIMEOUT), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);
        Assert.assertEquals(utilBase.convertHttpCodeToErrorCode(HTTP_VERSION), EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR);

    }

    @Test
    public void takenCareOf_shouldBeTrue_ifStartedLogWasCalled(){
        asdcEelfAudit.startLog(requestContext);
        assertTrue(asdcEelfAudit.isFlowBeingTakenCare());
    }

    @Test
    public void takenCareOf_shouldBeFalse_ifStartedLogWasNoCalled(){
        assertFalse(asdcEelfAudit.isFlowBeingTakenCare());
    }

    @Test
    public void takenCareOf_shouldBeFalse_ifStartedLogWasCalleAndLogWasCalledToo(){
        asdcEelfAudit.startLog(requestContext);
        
        asdcEelfAudit.logExit(null,null,null, LogLevel.INFO,  Severity.OK,
                "message", MarkerFactory.getMarker(ONAPLogConstants.Markers.EXIT.getName()));
        assertFalse(asdcEelfAudit.isFlowBeingTakenCare());
    }
}
