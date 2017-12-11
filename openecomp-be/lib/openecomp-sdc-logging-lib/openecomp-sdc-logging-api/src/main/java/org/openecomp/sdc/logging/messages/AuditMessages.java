/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.messages;

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
  private AuditMessages (){

  }
}
