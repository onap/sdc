package org.openecomp.sdc.logging.messages;

/**
 * Created by ayalaben on 4/20/2017.
 */
public class AuditMessages {

  public static final String AUDIT_MSG = " --Audit-- ";

  public static final String CREATE_VLM = "Create VLM. VLM Name: ";
  public static final String CHECK_IN_VLM = "Check in VLM. VLM Id: ";
  public static final String CHECK_OUT_VLM = "Check out VLM. VLM Id: ";
  public static final String SUBMIT_VLM = "submit VLM. VLM Name: ";

  public static final String CREATE_VSP = "Create VSP. VSP Name: ";
  public static final String CHECK_IN_VSP = "Check in VSP. VSP Id: ";
  public static final String CHECK_OUT_VSP = "Check out VSP. VSP Id: ";
  public static final String RESUBMIT_ALL_FINAL_VSPS = "Check out, check in and submit all " +
      "submitted VSPs, see ids below ";
  public static final String SUBMIT_VSP = "Submit VSP. VSP Id: ";
  public static final String SUBMIT_VSP_FAIL = "Submit VSP failed!. VSP Id: ";
  public static final String SUBMIT_VSP_ERROR = "Submit VSP error: %s. VSP Id: %s";

  public static final String UPLOAD_HEAT = "Upload HEAT. VSP Id: ";
  public static final String UPLOAD_PROCESS_ARTIFACT = "Upload Process Artifact. VSP Id: ";
  public static final String UPLOAD_MONITORING_FILE = "Upload Monitoring File of type %s "
      +  ". VSP Id: %s, component id: %s";

  public static final String IMPORT_SUCCESS = "VSP import to VF success. VSP Id: ";
  public static final String IMPORT_FAIL = "VSP import to VF fail. VSP Id: ";

  public static final String HEAT_VALIDATION_STARTED = "HEAT validation started. VSP Id: ";
  public static final String HEAT_VALIDATION_COMPLETED = "HEAT validation completed. VSP Id: ";
  public static final String HEAT_VALIDATION_ERROR = "HEAT validation error: %s. VSP Id: %s";
  public static final String CSAR_VALIDATION_STARTED = "CSAR validation started. VSP Id: ";
  public static final String HEAT_TRANSLATION_STARTED = "HEAT translation started. VSP Id: ";
  public static final String HEAT_TRANSLATION_COMPLETED = "HEAT translation completed. VSP Id: ";
  public static final String ENRICHMENT_ERROR = "Enrichment error: %s. VSP Id: %s";
  public static final String ENRICHMENT_COMPLETED = "Enrichment completed. VSP Id: ";
  public static final String CREATE_PACKAGE = "Created package. VSP Id: ";

}
