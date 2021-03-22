/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.generator.util;

public final class GeneratorConstants {

    public static final String ALLOWED_FLAVORS_PROPERTY = "allowed_flavors";
    public static final String IMAGES_PROPERTY = "images";
    public static final String RELEASE_VENDOR = "releaseVendor";
    public static final String VNF_CONFIG_NODE_TEMPLATE_ID_SUFFIX = "_VNF_Configuration";
    public static final String VNF_NODE_TEMPLATE_ID_SUFFIX = "_VNF";
    public static final String PORT_TYPE_INTERNAL_NODE_TEMPLATE_SUFFIX = "_lan";
    public static final String PORT_TYPE_EXTERNAL_NODE_TEMPLATE_SUFFIX = "_wan";
    public static final String PORT_NODE_TEMPLATE_ID_SUFFIX = "_port";
    public static final String TOSCA_SERVICE_TEMPLATE_FILE_NAME_SUFFIX = "ServiceTemplate.yaml";
    //Manual VSP propeties
    public static final String NUM_CPUS = "num_cpus";
    public static final String DISK_SIZE = "disk_size";
    public static final String MEM_SIZE = "mem_size";
    public static final String NUM_CPUS_PROP_DESC_PREFIX = "Number of cpu for ";
    public static final String DISK_SIZE_PROP_DESC_PREFIX = "Disk size for ";
    public static final String MEM_SIZE_PROP_DESC_PREFIX = "Memory size for ";

    // prevent utility class instantiation
    private GeneratorConstants() {
    }
}
