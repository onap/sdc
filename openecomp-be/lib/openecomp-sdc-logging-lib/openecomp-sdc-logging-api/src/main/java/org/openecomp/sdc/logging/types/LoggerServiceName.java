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

package org.openecomp.sdc.logging.types;


public enum LoggerServiceName {

  Create_VLM,
  Get_VLM,
  Checkout_VLM,
  Checkin_VLM,
  Undo_Checkout_VLM,
  Submit_VLM,
  Update_VLM,
  List_VLM,

  List_EP,
  Create_EP,
  Update_EP,
  Get_EP,
  Delete_EP,

  Create_LIMIT,
  Delete_LIMIT,
  Update_LIMIT,
  Get_LIMIT,

  List_FG,
  Create_FG,
  Update_FG,
  Get_FG,
  Delete_FG,

  List_LA,
  Create_LA,
  Update_LA,
  Get_LA,
  Delete_LA,

  List_LKG,
  Create_LKG,
  Update_LKG,
  Get_LKG,
  Delete_LKG,

  List_VSPs,
  List_Packages,
  Create_VSP,
  Get_VSP,
  Checkout_VSP,
  Checkin_VSP,
  Undo_Checkout_VSP,
  Submit_VSP,
  Update_VSP,
  Re_Submit_ALL_Final_VSPs,
  Create_Package,
  List_VSP,
  Upload_File,
  Get_Uploaded_File,
  Get_Translated_File,
  Get_Questionnaire_VSP,
  Update_Questionnaire_VSP,
  Get_Information_Artifact,


  List_Component_Processes,
  Delete_List_Component_Processes,
  Create_Component_Processes,
  Update_Component_Processes,
  Get_Component_Processes,
  Delete_Component_Processes,
  Upload_File_Component_Processes,
  Get_Uploaded_File_Component_Processes,
  Delete_Uploaded_File_Component_Processes,

  List_Processes,
  Delete_List_Processes,
  Create_Processes,
  Update_Processes,
  Get_Processes,
  Delete_Processes,
  Upload_File_Processes,
  Get_Uploaded_File_Processes,
  Delete_Uploaded_File_Processes,

  List_Components,
  Delete_List_Components,
  Create_Component,
  Update_Component,
  Get_Component,
  Delete_Component,
  Get_Questionnaire_Component,
  Update_Questionnaire_Component,

  Upload_Monitoring_Artifact,
  Delete_Monitoring_Artifact,
  List_Monitoring_Artifacts,

  List_Network,
  Create_Network,
  Update_Network,
  Get_Network,
  Delete_Network,

  List_nics,
  Delete_List_nics,
  Create_nic,
  Update_nic,
  Get_nic,
  Delete_nic,
  Get_Questionnaire_nic,
  Update_Questionnaire_nic,

  Create_Process,
  Update_Process,

  Create_Compute,
  List_Computes,
  Get_Compute,
  Update_Compute,
  Delete_Compute,
  Get_Questionnaire_Compute,
  Update_Questionnaire_Compute,

  Insert_To_ApplicationConfig_Table,
  Get_From_ApplicationConfig_Table,
  Get_List_From_ApplicationConfig_Table_By_Namespace,

  Create_Deployment_Flavor,
  Get_List_Deployment_flavor,
  Get_Deployment_flavor,
  Delete_Deployment_flavor,
  Update_Deployment_flavor,

  Get_List_Activity_Log,

  Validate,
  Enrich, Delete_VSP, Get_Process_Artifact, Create_Entity, Checkout_Entity, Undo_Checkout_Entity,
  Checkin_Entity, Submit_Entity, Get_Entity_Version, Delete_Entity, Undo_Delete_Entity,
  Translate_Resource, Translate_HEAT, LoggerServiceName, Get_VSP_List, Delete_VLM, Update_Manifest,
  Create_Image,
  GET_Image_Schema,
  List_Images,
  GET_Image,
  Delete_Image,
  Update_Image,

  CREATE_COMPONENT_DEPENDENCY_MODEL,
  GET_COMPONENT_DEPENDENCY_MODEL,

  Health_check
  ;

  public static String getServiceName(LoggerServiceName serviceName) {
    return serviceName.name().replace("_", " ");
  }

  @Override
  public String toString(){
    return this.name().replace("_", " ");
  }


}
