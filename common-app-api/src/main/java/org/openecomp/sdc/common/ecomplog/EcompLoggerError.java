package org.openecomp.sdc.common.ecomplog;

import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ERROR_CATEGORY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ERROR_CODE;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_ERROR_DESC;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_KEY_REQUEST_ID;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_PARTNER_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_SERVICE_NAME;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_TARGET_ENTITY;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_TARGET_SERVICE_NAME;

import org.openecomp.sdc.common.ecomplog.Enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.ecomplog.Enums.LogMarkers;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.slf4j.MarkerFactory;

public class EcompLoggerError extends EcompLoggerBase{
    private static EcompLoggerError instanceLoggerError = EcompLoggerFactory.getLogger(EcompLoggerError.class);

    EcompLoggerError(IEcompMdcWrapper ecompMdcWrapper) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.ERROR_MARKER.text()));
    }

    public static EcompLoggerError getInstance() {
        return instanceLoggerError;
    }

    @Override
    public void initializeMandatoryFields() {
        ecompMdcWrapper.setMandatoryField(MDC_BEGIN_TIMESTAMP);
        ecompMdcWrapper.setMandatoryField(MDC_KEY_REQUEST_ID);
        ecompMdcWrapper.setMandatoryField(MDC_SERVICE_NAME);
        ecompMdcWrapper.setMandatoryField(MDC_PARTNER_NAME);
        ecompMdcWrapper.setMandatoryField(MDC_ERROR_CATEGORY);
        ecompMdcWrapper.setMandatoryField(MDC_ERROR_CODE);
        ecompMdcWrapper.setMandatoryField(MDC_ERROR_DESC);

        ecompMdcWrapper.setOptionalField(MDC_TARGET_ENTITY);
        ecompMdcWrapper.setOptionalField(MDC_TARGET_SERVICE_NAME);
    }

    @Override
    public EcompLoggerError startTimer() {
        return (EcompLoggerError) super.startTimer();
    }

    @Override
    public EcompLoggerError setKeyRequestId(String keyRequestId) {
        return (EcompLoggerError) super.setKeyRequestId(keyRequestId);
    }

    public EcompLoggerError setServiceName(String serviceName) {
        ecompMdcWrapper.setServiceName(serviceName);
        return this;
    }

    public EcompLoggerError setTargetEntity(String targetEntity) {
        ecompMdcWrapper.setTargetEntity(targetEntity);
        return this;
    }

    public EcompLoggerError setErrorCode(EcompLoggerErrorCode errorCode) {
        ecompMdcWrapper.setErrorCode(errorCode.getErrorCode());
        return this;
    }

    public EcompLoggerError setErrorDescription(String errorDescription) {
        ecompMdcWrapper.setErrorDescription(errorDescription);
        return this;
    }

    public EcompLoggerError clear() {
        return (EcompLoggerError) super.clear();
    }

}
