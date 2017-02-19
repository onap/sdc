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

package org.openecomp.sdc.translator.services.heattotosca.globaltypes;

import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.AttributeDefinition;
import org.openecomp.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.services.heattotosca.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CinderVolumeGlobalType {

  /**
   * Create service template service template.
   *
   * @return the service template
   */
  public static ServiceTemplate createServiceTemplate() {
    ServiceTemplate cinderVolumeServiceTemplate = new ServiceTemplate();
    cinderVolumeServiceTemplate
        .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
    cinderVolumeServiceTemplate.setMetadata(
        DataModelUtil.createMetadata(Constants.CINDER_VOLUME_TEMPLATE_NAME, "1.0.0", null));
    cinderVolumeServiceTemplate.setDescription("Cinder Volume TOSCA Global Types");
    cinderVolumeServiceTemplate.setRelationship_types(createGlobalRelationshipTypes());
    cinderVolumeServiceTemplate.setNode_types(createGlobalNodeTypes());
    return cinderVolumeServiceTemplate;
  }

  private static Map<String, NodeType> createGlobalNodeTypes() {
    Map<String, NodeType> globalNodeTypes = new HashMap<>();
    globalNodeTypes.put(ToscaNodeType.CINDER_VOLUME.getDisplayName(), createCinderVolumeNodeType());
    return globalNodeTypes;
  }

  private static NodeType createCinderVolumeNodeType() {
    NodeType cinderVolumeNodeType = new NodeType();
    cinderVolumeNodeType.setDerived_from(ToscaNodeType.BLOCK_STORAGE.getDisplayName());
    cinderVolumeNodeType.setProperties(createCinderVolumeProperties());
    cinderVolumeNodeType.setAttributes(createCinderVolumeAttributes());
    return cinderVolumeNodeType;
  }

  private static Map<String, RelationshipType> createGlobalRelationshipTypes() {
    Map<String, RelationshipType> globalRelationshipTypes = new HashMap<>();
    globalRelationshipTypes.put(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO.getDisplayName(),
        createCinderVolumeAttachesToRelationshipType());
    return globalRelationshipTypes;
  }

  private static RelationshipType createCinderVolumeAttachesToRelationshipType() {
    RelationshipType cinderVolumeAttachesToRelationType = new RelationshipType();
    cinderVolumeAttachesToRelationType
        .setDerived_from(ToscaRelationshipType.NATIVE_ATTACHES_TO.getDisplayName());
    cinderVolumeAttachesToRelationType
        .setDescription("This type represents an attachment relationship for associating volume");

    Map<String, PropertyDefinition> cinderVolumeAttachesToProps = new HashMap<>();
    cinderVolumeAttachesToProps.put("location", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The location where the volume is exposed on the instance, mountpoint", false, null,
            null, null, null)); //overridden location prop from attachesTo
    cinderVolumeAttachesToProps.put("instance_uuid", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID of the server to which the volume attaches", true, null, null, null, null));
    cinderVolumeAttachesToProps.put("volume_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The ID of the volume to be attached", true, null, null, null, null));
    cinderVolumeAttachesToRelationType.setProperties(cinderVolumeAttachesToProps);

    Map<String, AttributeDefinition> cinderVolumeAttachesToAttributes = new HashMap<>();
    cinderVolumeAttachesToAttributes.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    cinderVolumeAttachesToRelationType.setAttributes(cinderVolumeAttachesToAttributes);

    return cinderVolumeAttachesToRelationType;
  }

  private static Map<String, PropertyDefinition> createCinderVolumeProperties() {
    Map<String, PropertyDefinition> cinderVolumePropertyDefMap = new HashMap<>();
    cinderVolumePropertyDefMap.put("availability_zone", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "The availability zone in which the volume will be created", false, null, null, null,
            null));
    cinderVolumePropertyDefMap.put("backup_id", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "If specified, the backup to create the volume from", false, null, null, null, null));
    cinderVolumePropertyDefMap.put(Constants.DESCRIPTION_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A description of the volume", false, null, null, null, null));
    cinderVolumePropertyDefMap.put("image", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "If specified, the name or ID of the image to create the volume from", false, null,
            null, null, null));
    cinderVolumePropertyDefMap.put("metadata", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Key/value pairs to associate with the volume", false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    cinderVolumePropertyDefMap.put("multiattach", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Whether allow the volume to be attached more than once", false, null, null, null,
            null));
    cinderVolumePropertyDefMap.put(Constants.NAME_PROPERTY_NAME, DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "A name used to distinguish the volume", false, null, null, null, null));
    cinderVolumePropertyDefMap.put("read_only", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Enables or disables read-only access mode of volume", false, null, null, null, null));
    cinderVolumePropertyDefMap.put("scheduler_hints", DataModelUtil
        .createPropertyDefinition(PropertyType.MAP.getDisplayName(),
            "Arbitrary key-value pairs specified by the client "
                    + "to help the Cinder scheduler creating a volume",
            false, null, null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    cinderVolumePropertyDefMap.put("size", DataModelUtil
        .createPropertyDefinition(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(),
            "The requested storage size (default unit is MB)", false, getSizeConstraints(), null,
            null, null));
    cinderVolumePropertyDefMap.put("source_volid", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "If specified, the volume to use as source", false, null, null, null, null));
    cinderVolumePropertyDefMap.put("volume_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "If specified, the type of volume to use, mapping to a specific backend", false, null,
            null, null, null));
    cinderVolumePropertyDefMap.put("delete_on_termination", DataModelUtil
        .createPropertyDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Indicate whether the volume should be deleted when the server is terminated", false,
            null, null, null, null));
    cinderVolumePropertyDefMap.put("boot_index", DataModelUtil
        .createPropertyDefinition(PropertyType.INTEGER.getDisplayName(),
            "Integer used for ordering the boot disks", false, null, null, null, null));
    cinderVolumePropertyDefMap.put("device_type", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(), "Device type", false,
            getDeviceTypeConstraints(), null, null, null));
    cinderVolumePropertyDefMap.put("disk_bus", DataModelUtil
        .createPropertyDefinition(PropertyType.STRING.getDisplayName(),
            "Bus of the device: hypervisor driver chooses a suitable default if omitted", false,
            getDiskBusConstraints(), null, null, null));
    cinderVolumePropertyDefMap.put("swap_size", DataModelUtil
        .createPropertyDefinition(PropertyType.SCALAR_UNIT_SIZE.getDisplayName(),
            "The size of the swap, in MB", false, null, null, null, null));

    return cinderVolumePropertyDefMap;
  }

  private static Map<String, AttributeDefinition> createCinderVolumeAttributes() {
    Map<String, AttributeDefinition> cinderVolumeAttributesDefMap = new HashMap<>();
    cinderVolumeAttributesDefMap.put("attachments", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The list of attachments of the volume", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    cinderVolumeAttributesDefMap.put("bootable", DataModelUtil
        .createAttributeDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Boolean indicating if the volume can be booted or not", null, null, null));
    cinderVolumeAttributesDefMap.put("created_at", DataModelUtil
        .createAttributeDefinition(PropertyType.TIMESTAMP.getDisplayName(),
            "The timestamp indicating volume creation", null, null, null));
    cinderVolumeAttributesDefMap.put("display_description", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Description of the volume", null, null, null));
    cinderVolumeAttributesDefMap.put("display_name", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(), "Name of the volume", null,
            null, null));
    cinderVolumeAttributesDefMap.put("encrypted", DataModelUtil
        .createAttributeDefinition(PropertyType.BOOLEAN.getDisplayName(),
            "Boolean indicating if the volume is encrypted or not", null, null, null));
    cinderVolumeAttributesDefMap.put("metadata_values", DataModelUtil
        .createAttributeDefinition(PropertyType.MAP.getDisplayName(),
            "Key/value pairs associated with the volume in raw dict form", null,
            DataModelUtil.createEntrySchema(PropertyType.STRING.getDisplayName(), null, null),
            null));
    cinderVolumeAttributesDefMap.put("show", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "Detailed information about resource", null, null, null));
    cinderVolumeAttributesDefMap.put("status", DataModelUtil
        .createAttributeDefinition(PropertyType.STRING.getDisplayName(),
            "The current status of the volume", null, null, null));
    return cinderVolumeAttributesDefMap;
  }

  private static List<Constraint> getDeviceTypeConstraints() {
    Constraint validValues;
    List<Constraint> constraints = new ArrayList<>();
    validValues = DataModelUtil.createValidValuesConstraint("cdrom", "disk");
    constraints.add(validValues);
    return constraints;
  }

  private static List<Constraint> getDiskBusConstraints() {
    Constraint validValues;
    List<Constraint> constraints = new ArrayList<>();
    validValues =
        DataModelUtil.createValidValuesConstraint("ide", "lame_bus", "scsi", "usb", "virtio");
    constraints.add(validValues);
    return constraints;
  }


  private static List<Constraint> getSizeConstraints() {
    List<Constraint> constraints;
    constraints = new ArrayList<>();
    Constraint constraint = new Constraint();
    constraint.setGreater_or_equal("1 GB");
    constraints.add(constraint);
    return constraints;
  }
}
