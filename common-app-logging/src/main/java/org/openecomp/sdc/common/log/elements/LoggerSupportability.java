package org.openecomp.sdc.common.log.elements;

import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.util.*;

public class LoggerSupportability extends LoggerBase {

  public LoggerSupportability(ILogFieldsHandler ecompLogFieldsHandler, Logger logger) {
    super(ecompLogFieldsHandler, MarkerFactory.getMarker(LogMarkers.SUPPORTABILITY_MARKER.text()),
        logger);
  }

  public static LoggerSupportability getLogger(String className) {
    return LoggerFactory.getMdcLogger(LoggerSupportability.class,
        org.slf4j.LoggerFactory.getLogger(className));
  }


  public void log(LoggerSupportabilityActions action, Map<String,String> componentMetaData,StatusCode statusCode, String message, Object...params) {
    fillFieldsBeforeLogging(action,componentMetaData,statusCode);
    super.log(LogLevel.INFO,message, params);
  }

  public void log(LoggerSupportabilityActions action, StatusCode statusCode, String message, Object...params) {
    log(action, null, statusCode, message, params);
  }

  private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(
      ILogConfiguration.MDC_SUPPORTABLITY_ACTION,
      ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID,
      ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION,
      ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME,
      ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID,
      ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION,
      ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE));

  private void fillFieldsBeforeLogging(LoggerSupportabilityActions action, Map<String,String> componentMetaData,StatusCode statusCode) {
    clear();
    if (componentMetaData!=null){
      ecompLogFieldsHandler.setSupportablityCsarUUID(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID));
      ecompLogFieldsHandler.setSupportablityCsarVersion(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION));
      ecompLogFieldsHandler.setSupportablityComponentName(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME));
      ecompLogFieldsHandler.setSupportablityComponentUUID(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID));
      ecompLogFieldsHandler.setSupportablityComponentVersion(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION));
    }
    ecompLogFieldsHandler.setSupportablityAction(action.getName());
    ecompLogFieldsHandler.setSupportablityStatusCode(statusCode.getStatusCodeEnum());
    }

  @Override
  public LoggerSupportability  clear(){
    LogFieldsMdcHandler.getInstance().removeSupportablityAction();
    LogFieldsMdcHandler.getInstance().removeSupportablityCsarUUID();
    LogFieldsMdcHandler.getInstance().removeSupportablityCsarVersion();
    LogFieldsMdcHandler.getInstance().removeSupportablityComponentName();
    LogFieldsMdcHandler.getInstance().removeSupportablityComponentUUID();
    LogFieldsMdcHandler.getInstance().removeSupportablityComponentVersion();
    LogFieldsMdcHandler.getInstance().removeSupportablityStatusCode();
    return this;
  }


  @Override
  public List<String> getMandatoryFields() {
    return Collections.unmodifiableList(mandatoryFields);
  }

  @Override
  public LoggerSupportability startTimer() {
    return this;
  }

}
