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

package org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.impl;

/**
 * @author KATYR
 * @since December 07, 2016
 */

public class TxtInformationArtifactConstants {
  public static final String QUOTEMARK = "\"";
  public static final String SPACE = " ";
  public static final String TAB = SPACE + SPACE + SPACE + SPACE;
  public static final String NL = System.lineSeparator();
  public static final String DELIMITER = ":";
  public static final String FOUR_TABS = TAB + TAB + TAB + TAB;

  public static final String HEADER = "AT&T Proprietary (Restricted)\n"
      + "Only for use by authorized individuals or any above-designated team(s)\n"
      + "within the AT&T companies and not for general distribution" + NL + NL + NL;

  public static final String FOOTER = NL + NL + NL + "AT&T Proprietary (Restricted)\n"
      + "Only for use by authorized individuals or any above-designated team(s)\n"
      + "within the AT&T companies and not for general distribution";


  public static final String TITLE = "1." + TAB + "VSP Details" + DELIMITER;
  public static final String VSP_NAME = "a." + TAB + "Name" + DELIMITER;
  public static final String VSP_DESC = "b." + TAB + "Description";
  public static final String VSP_VENDOR = "d." + TAB + "Vendor" + DELIMITER;
  public static final String VSP_VERSION = "c." + TAB + "Version" + DELIMITER;
  public static final String VSP_CATEGORY = "e." + TAB + "Category" + DELIMITER;
  public static final String LICENSE_DETAILS = "f." + TAB + "License Details" + DELIMITER;
  public static final String LICENSE_MODEL_VERSION =
      "i." + TAB + "License Model Version" + DELIMITER;
  public static final String LICENSE_AGREEMENT_NAME =
      "ii." + TAB + "License Agreement Name" + DELIMITER;
  public static final String LIST_OF_FEATURE_GROUPS = "iii" + TAB + "List of Feature Groups"+DELIMITER;
  public static final String HIGH_AVAILABILITY = "g." + TAB + "High Availability";
  public static final String USING_AVAILABILITY_ZONES = "i." + TAB + "Using Availability "
      + "Zones" + DELIMITER;
  public static final String STORAGE_BACKUP_DETAILS = "h." + TAB + "Storage Data Back-up "
      + "Details" + DELIMITER;
  public static final String IS_DATA_REPLICATION =
      "i." + TAB + "Data Replication Required?" + DELIMITER;
  public static final String DATA_SIZE_TO_REP =
      "ii." + TAB + "	Data Size to replicate in GB" + DELIMITER;
  public static final String DATA_REP_FREQUENCY =
      "iii." + TAB + "Data replication frequency" + DELIMITER;
  public static final String DATA_REP_SOURCE = "iv." + TAB + "Replication Source" + DELIMITER;
  public static final String DATA_REP_DEST = "v." + TAB + "Replication Destination" + DELIMITER;
  public static final String LIST_OF_NETWORKS = "List of Internal Networks";//currently not used
  public static final String LIST_OF_VFCS = "3." + TAB + "List of VFCs";
  public static final String FOR_EACH_VFC = "a." + TAB + "For each VFC" + DELIMITER;
  public static final String VFC_NAME = "i." + TAB + "VFC Name" + DELIMITER;
  public static final String VFC_DESC = "ii." + TAB + "Description" + DELIMITER;
  public static final String VFC_IMAGES = "iii." + TAB + "Images" + DELIMITER;
  public static final String VFC_COMPUTE = "iv." + TAB + "Compute/VM Characteristics";
  public static final String VFC_COMPUTE_VCPU = "1." + TAB + "vCPU" + DELIMITER;
  public static final String VFC_COMPUTE_CPU_OVER_SUBSCRIPTION = "2." + TAB + "CPU "
      + "over-subscription" + DELIMITER;
  public static final String VFC_COMPUTE_MEMORY = "3." + TAB + "Memory" + DELIMITER;
  public static final String VFC_COMPUTE_DISK = "4." + TAB + "Disk" + DELIMITER;
  public static final String HYPERVISOR_DETAILS = "v." + TAB + "Hypervisor Details";
  public static final String HYPERVISOR_DETAILS_NAME = "1." + TAB + "Name" + DELIMITER;
  public static final String HYPERVISOR_DETAILS_DRIVERS = "2." + TAB + "Drivers" + DELIMITER;
  public static final String GUEST_OS_DETAILS = "vi." + TAB + "Guest OS Details";
  public static final String GUEST_OS_NAME = "1." + TAB + "Name" + DELIMITER;
  public static final String GUEST_OS_BIT_SIZE = "2." + TAB + "Bit Size" + DELIMITER;
  public static final String GUEST_OS_TOOLS = "3." + TAB + "Tools" + DELIMITER;
  public static final String VFC_INSTANCE_NUMBER = "vii." + TAB + "Number of VFC Instances";
  public static final String VFC_INSTANCE_NUMBER_MIN = "1." + TAB + "Minimum" + DELIMITER;
  public static final String VFC_INSTANCE_NUMBER_MAX = "2." + TAB + "Maximum" + DELIMITER;
  public static final String VNICS = "viii." + TAB + "vNICs";
  public static final String VNICS_NAME = "1." + TAB + "Name" + DELIMITER;
  public static final String VNICS_PURPOSE = "2." + TAB + "Purpose" + DELIMITER;
  public static final String VNICS_INT_EXT = "3." + TAB + "Internal/External" + DELIMITER;
  public static final String VNICS_NETWORK = "4." + TAB + "Network" + DELIMITER;
  public static final String VNICS_PROTOCOLS = "5." + TAB + "Protocols" + DELIMITER;
  public static final String VNICS_IPV4 = "6." + TAB + "IP v4 Required" + DELIMITER;
  public static final String VNICS_IPV6 = "7." + TAB + "IP v6 Required" + DELIMITER;
  public static final String RECOVERY_DETAILS = "ix." + TAB + "Recovery Details" + DELIMITER;
  public static final String RECOVERY_DETAILS_POINT = "1." + TAB + "Recovery Point" + DELIMITER;
  public static final String RECOVERY_DETAILS_TIME = "2." + TAB + "Recovery Time" + DELIMITER;


}
