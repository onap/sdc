package org.openecomp.sdc.common.ecomplog;

import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_BEGIN_TIMESTAMP;
import static org.openecomp.sdc.common.ecomplog.api.IEcompLogConfiguration.MDC_KEY_REQUEST_ID;

import org.openecomp.sdc.common.ecomplog.Enums.LogLevel;
import org.openecomp.sdc.common.ecomplog.Enums.LogMarkers;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;
import org.slf4j.MarkerFactory;

public class EcompLoggerDebug extends EcompLoggerBase {

    private static EcompLoggerDebug instanceLoggerDebug = EcompLoggerFactory.getLogger(EcompLoggerDebug.class);

    protected final String endOfRecordDelimiter = "|^\\n";

    EcompLoggerDebug(IEcompMdcWrapper ecompMdcWrapper) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.DEBUG_MARKER.text()));
    }

    public static EcompLoggerDebug getInstance() {
        return instanceLoggerDebug;
    }

    @Override
    public void log(LogLevel errorLevel, String message) {
        String formattedMessage = String.format("%s%s", message, endOfRecordDelimiter);
        super.log(errorLevel, formattedMessage);
    }

    @Override
    public void initializeMandatoryFields() {
        ecompMdcWrapper.setMandatoryField(MDC_BEGIN_TIMESTAMP);
        ecompMdcWrapper.setMandatoryField(MDC_KEY_REQUEST_ID);
    }

    @Override
    public EcompLoggerDebug clear() {
        return (EcompLoggerDebug) super.clear();
    }

    @Override
    public EcompLoggerDebug startTimer() {
        return (EcompLoggerDebug) super.startTimer();
    }

    @Override
    public EcompLoggerDebug setKeyRequestId(String keyRequestId) {
        return (EcompLoggerDebug) super.setKeyRequestId(keyRequestId);
    }
}
