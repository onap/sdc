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

package org.openecomp.sdc.translator.services.heattotosca;


import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;

import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationBase;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationCinderVolumeAttachmentImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationCinderVolumeImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailAttachPolicyImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailNetworkPolicyImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailServiceInstanceImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailServiceTemplateImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailV2NetworkPolicyImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailV2VirtualNetworkImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailV2VmInterfaceImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationContrailVirtualNetworkImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationDefaultImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNestedImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNeutronNetImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNeutronPortImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNeutronSecurityGroupImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNeutronSubnetImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNovaServerGroupsImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNovaServerImpl;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationResourceGroupImpl;

import java.util.Objects;

public class ResourceTranslationFactory {

  /**
   * Gets instance.
   *
   * @param resource the resource
   * @return the instance
   */
  public static ResourceTranslationBase getInstance(Resource resource) {
    HeatResourcesTypes heatResource = HeatResourcesTypes.findByHeatResource(resource.getType());
    if (Objects.isNull(heatResource)) {
      if (HeatToToscaUtil.isYmlFileType(resource.getType())) {
        return new ResourceTranslationNestedImpl();
      }
      return new ResourceTranslationDefaultImpl();
    }
    switch (heatResource) {
      case NOVA_SERVER_RESOURCE_TYPE:
        return new ResourceTranslationNovaServerImpl();
      case NOVA_SERVER_GROUP_RESOURCE_TYPE:
        return new ResourceTranslationNovaServerGroupsImpl();
      case NEUTRON_SECURITY_GROUP_RESOURCE_TYPE:
        return new ResourceTranslationNeutronSecurityGroupImpl();
      case NEUTRON_PORT_RESOURCE_TYPE:
        return new ResourceTranslationNeutronPortImpl();
      case CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE:
        return new ResourceTranslationContrailVirtualNetworkImpl();
      case CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE:
        return new ResourceTranslationContrailV2VirtualNetworkImpl();
      case CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE:
        return new ResourceTranslationContrailV2VmInterfaceImpl();
      case CINDER_VOLUME_RESOURCE_TYPE:
        return new ResourceTranslationCinderVolumeImpl();
      case CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE:
        return new ResourceTranslationCinderVolumeAttachmentImpl();
      case NEUTRON_NET_RESOURCE_TYPE:
        return new ResourceTranslationNeutronNetImpl();
      case NEUTRON_SUBNET_RESOURCE_TYPE:
        return new ResourceTranslationNeutronSubnetImpl();
      case CONTRAIL_NETWORK_RULE_RESOURCE_TYPE:
        return new ResourceTranslationContrailNetworkPolicyImpl();
      case CONTRAIL_V2_NETWORK_RULE_RESOURCE_TYPE:
        return new ResourceTranslationContrailV2NetworkPolicyImpl();
      case CONTRAIL_NETWORK_ATTACH_RULE_RESOURCE_TYPE:
        return new ResourceTranslationContrailAttachPolicyImpl();
      case RESOURCE_GROUP_RESOURCE_TYPE:
        return new ResourceTranslationResourceGroupImpl();
      case CONTRAIL_SERVICE_TEMPLATE:
        return new ResourceTranslationContrailServiceTemplateImpl();
      case CONTRAIL_SERVICE_INSTANCE:
        return new ResourceTranslationContrailServiceInstanceImpl();
      default:
        return new ResourceTranslationDefaultImpl();
    }
  }


}
