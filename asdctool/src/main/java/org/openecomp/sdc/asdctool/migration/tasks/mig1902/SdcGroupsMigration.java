/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.asdctool.migration.tasks.mig1902;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.tasks.InstanceMigrationBase;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SdcGroupsMigration extends InstanceMigrationBase implements Migration {

    private static final Logger log = LoggerFactory.getLogger(SdcGroupsMigration.class);

    private final GroupTypeOperation groupTypeOperation;

    private Map<String, GroupTypeDefinition> latestGroupTypeMap = new HashMap<>();

    public enum GroupsForUpgrade {
        NW_COLLECTION_GROUP_NAME("org.openecomp.groups.NetworkCollection"),
        VFC_INSTANCE_GROUP_NAME("org.openecomp.groups.VfcInstanceGroup");

        private String toscaType;

        GroupsForUpgrade(String toscaType) {
            this.toscaType = toscaType;
        }

        public static boolean containsToscaType(String type) {
            try {
                return Arrays.stream(values()).anyMatch(g->g.getToscaType().equals(type));
            }
            catch (IllegalArgumentException ex) {
                return false;
            }
        }

        public String getToscaType() {
            return toscaType;
        }

    }
    public SdcGroupsMigration(JanusGraphDao janusGraphDao, GroupTypeOperation groupTypeOperation) {
        super(janusGraphDao);
        this.groupTypeOperation = groupTypeOperation;
    }

    @Override
    public String description() {
        return "update derived from field value for NetworkCollection and VfcInstanceGroup group instances ";
    }

    @Override
    public DBVersion getVersion() {
        return DBVersion.from(BigInteger.valueOf(1902), BigInteger.valueOf(0));
    }

    @Override
    public MigrationResult migrate() {
        loadLatestGroupTypeDefinitions();
        StorageOperationStatus status = upgradeTopologyTemplates();
        return status == StorageOperationStatus.OK ?
                MigrationResult.success() : MigrationResult.error("failed to update derived from value for NetworkCollection and VfcInstanceGroup group instances. Error : " + status);
    }

    void loadLatestGroupTypeDefinitions() {
        Arrays.stream(GroupsForUpgrade.values()).forEach(this::getLatestGroupTypeDefinition);
    }

    @Override
    protected StorageOperationStatus handleOneContainer(GraphVertex containerVorig) {
        StorageOperationStatus status = StorageOperationStatus.NOT_FOUND;
        GraphVertex containerV = getVertexById(containerVorig.getUniqueId());

        try {
            status = janusGraphDao.getChildVertex(containerV, EdgeLabelEnum.GROUPS, JsonParseFlagEnum.ParseAll)
                    .either(this::updateGroupPropertiesIfRequired, this::handleError);
        }
        catch (Exception e) {
            log.error("Exception occurred:", e);
            status = StorageOperationStatus.GENERAL_ERROR;
        }
        finally {
            if (status != StorageOperationStatus.OK) {
                janusGraphDao.rollback();
                if (status == StorageOperationStatus.NOT_FOUND) {
                    //it is happy flow as well
                    status = StorageOperationStatus.OK;
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Upgrade status is <{}> for topology template <{}> uniqueId <{}>",
                        status.name(), containerV.getMetadataProperties().get(GraphPropertyEnum.NAME),
                        containerV.getMetadataProperties().get(GraphPropertyEnum.UNIQUE_ID));
            }
        }
        return status;
    }

    private StorageOperationStatus updateGroupPropertiesIfRequired(GraphVertex vertex) {
        StorageOperationStatus status = StorageOperationStatus.NOT_FOUND;
        boolean isUpdated = false;
        Map<String, GroupDataDefinition> groupDefinitionMap = (Map<String, GroupDataDefinition>) vertex.getJson();
        for (GroupDataDefinition groupDef : groupDefinitionMap.values()) {
           if (GroupsForUpgrade.containsToscaType(groupDef.getType())) {
                if (log.isDebugEnabled()) {
                    log.debug("Group instance named <{}> of type <{}> is supposed to be updated on vertex <{}>",
                            groupDef.getName(), groupDef.getType(), vertex.getUniqueId());
                }
                isUpdated = isGroupPropertiesUpdateDone(groupDef.getProperties(), latestGroupTypeMap.get(groupDef.getType()).getProperties());
                if (log.isDebugEnabled()) {
                    String result = isUpdated ? "has been updated" : "is up to date ";
                    log.debug("Group instance named <{}> of type <{}> uniqueID <{}> {} on vertex <{}>",
                                            groupDef.getName(), groupDef.getType(), groupDef.getUniqueId(), result, vertex.getUniqueId());
                }
            }
        }
        if (isUpdated) {
            vertex.setJson(groupDefinitionMap);
            status = updateVertexAndCommit(vertex);
            if (status == StorageOperationStatus.OK && log.isDebugEnabled()) {
                log.debug("Group properties change is committed on vertex <{}>", vertex.getUniqueId());
            }
        }
        return status;
    }

    private boolean isGroupPropertiesUpdateDone(List<PropertyDataDefinition> curPropDefList, List<PropertyDefinition> latestGroupDefList) {
        boolean isUpdated = false;
        for (PropertyDefinition prop: latestGroupDefList) {
            if (curPropDefList.stream().noneMatch(l->l.getName().equals(prop.getName()))) {
                curPropDefList.add(prop);
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    StorageOperationStatus getLatestGroupTypeDefinition(GroupsForUpgrade groupsForUpgrade) {
        return groupTypeOperation.getLatestGroupTypeByType(groupsForUpgrade.getToscaType(), false)
                .either(g-> {
                    latestGroupTypeMap.put(groupsForUpgrade.getToscaType(), g);
                    return StorageOperationStatus.OK;
                }, err->err);
    }


}
