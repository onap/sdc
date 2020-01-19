package org.openecomp.sdc.common.log.elements;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.enums.ConstantsLogging;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoggerBaseTest {

    @Mock
    HttpServletRequest httpServletRequest;

    private static final String ONAP_REQ_ID_VALUE = "onapReqId";
    private static final String REQ_ID_VALUE = "reqId";
    private static final String TRANSACTION_RE_IQ_VALUE = "transactionReqId";
    private static final String ECOMP_REQ_ID_VALUE = "ecompReqId";
    private static final String USER_ID_VALUE = "userId";
    private static final String ONAP_PARTNER_NAME_VALUE = "partnerName";
    private static final String USER_UGENT = "userAgent";

    @Test
    public void testGetRequestIfFromOnapHeader() {
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(ONAP_REQ_ID_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_REQUEST_ID)).thenReturn(REQ_ID_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER)).thenReturn(TRANSACTION_RE_IQ_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(ECOMP_REQ_ID_VALUE);

        String requestId = LoggerBase.getRequestId(httpServletRequest);
        assertThat(requestId).isEqualTo(ONAP_REQ_ID_VALUE);
    }

    @Test
    public void testGetRequestIfFromReqIdHeader() {
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_REQUEST_ID)).thenReturn(REQ_ID_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER)).thenReturn(TRANSACTION_RE_IQ_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(ECOMP_REQ_ID_VALUE);

        String requestId = LoggerBase.getRequestId(httpServletRequest);
        assertThat(requestId).isEqualTo(REQ_ID_VALUE);
    }

    @Test
    public void testGetRequestIfFromTransactionIdHeader() {
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER)).thenReturn(TRANSACTION_RE_IQ_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(ECOMP_REQ_ID_VALUE);

        String requestId = LoggerBase.getRequestId(httpServletRequest);
        assertThat(requestId).isEqualTo(TRANSACTION_RE_IQ_VALUE);
    }

    @Test
    public void testGetRequestIfFromEcompIdHeader() {
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(ECOMP_REQ_ID_VALUE);

        String requestId = LoggerBase.getRequestId(httpServletRequest);
        assertThat(requestId).isEqualTo(ECOMP_REQ_ID_VALUE);
    }

    @Test
    public void testGetRequestIfFromRandonHeader() {
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_REQUEST_ID)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_TRANSACTION_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.X_ECOMP_REQUEST_ID_HEADER)).thenReturn(null);

        String requestId = LoggerBase.getRequestId(httpServletRequest);
        assertThat(requestId).isNotEmpty();
    }

    @Test
    public void testPartnerNameFromUserHeader() {
        when(httpServletRequest.getHeader(ConstantsLogging.USER_ID_HEADER)).thenReturn(USER_ID_VALUE);
        when(httpServletRequest.getHeader(ConstantsLogging.USER_AGENT_HEADER)).thenReturn(USER_UGENT);
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(ONAP_PARTNER_NAME_VALUE);

        String partnerName = LoggerBase.getPartnerName(httpServletRequest);
        assertThat(partnerName).isEqualTo(USER_ID_VALUE);
    }

    @Test
    public void testPartnerNameFromOnapPartnerNameHeader() {
        when(httpServletRequest.getHeader(ConstantsLogging.USER_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(ONAP_PARTNER_NAME_VALUE);

        String partnerName = LoggerBase.getPartnerName(httpServletRequest);
        assertThat(partnerName).isEqualTo(ONAP_PARTNER_NAME_VALUE);
    }

    @Test
    public void testPartnerNameFromReqUriHeader() {
        when(httpServletRequest.getHeader(ConstantsLogging.USER_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.USER_AGENT_HEADER)).thenReturn(USER_UGENT);

        String partnerName = LoggerBase.getPartnerName(httpServletRequest);
        assertThat(partnerName).isEqualTo(USER_UGENT);
    }

    @Test
    public void testPartnerNameUnknown() {
        when(httpServletRequest.getHeader(ConstantsLogging.USER_ID_HEADER)).thenReturn(null);
        when(httpServletRequest.getHeader(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(null);
        when(httpServletRequest.getHeader(ConstantsLogging.USER_AGENT_HEADER)).thenReturn(null);

        String partnerName = LoggerBase.getPartnerName(httpServletRequest);
        assertThat(partnerName).isEqualTo(ConstantsLogging.PartnerName_Unknown);
    }
}
