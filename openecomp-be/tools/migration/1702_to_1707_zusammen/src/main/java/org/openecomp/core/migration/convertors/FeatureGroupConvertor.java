package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.RelationType;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VlmZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 4/25/2017
 */
public class FeatureGroupConvertor {

    private static Logger logger = LoggerFactory.getLogger(FeatureGroupConvertor.class);
    private static Set<String> FeatureGroupsLoaded = new HashSet<>();

    public static ElementEntityContext convertFeatureGroupToElementContext(FeatureGroupEntity featureGroupEntity) {

        return new ElementEntityContext("GLOBAL_USER", new
                ElementContext(featureGroupEntity.getVendorLicenseModelId(), featureGroupEntity.getVersion().toString()));
    }

    public static CollaborationElement[] convertFeatureGroupToElement(FeatureGroupEntity featureGroupEntity) {
//        printMessage(logger, "source FeatureGroupEntity -> " + featureGroupEntity.toString());
        CollaborationElement[] elements;
        List<String> featureGroupNamespace = getFeatureGroupNamespace(featureGroupEntity);

        int index = 0;
        String featureGroupsEntityId = StructureElement.FeatureGroups.name();
        String uniqueId = featureGroupEntity.getVendorLicenseModelId() + "_" + featureGroupEntity.getVersion().toString();

        if (FeatureGroupsLoaded.contains(uniqueId)) {
            elements = new CollaborationElement[1];
        } else {
            FeatureGroupsLoaded.add(uniqueId);
            elements = new CollaborationElement[2];
            elements[index] = ElementHandler.getElementEntity(
                    featureGroupEntity.getVendorLicenseModelId(), featureGroupEntity.getVersion().toString(),
                    featureGroupsEntityId, featureGroupNamespace,
                    ElementHandler.getStructuralElementInfo(StructureElement.FeatureGroups.name()),
                    null, null, null);
            index++;
        }

        featureGroupNamespace.add(featureGroupsEntityId);

        elements[index] = ElementHandler.getElementEntity(
                featureGroupEntity.getVendorLicenseModelId(), featureGroupEntity.getVersion().toString(),
                featureGroupEntity.getId(), featureGroupNamespace, getFeatureGroupInfo(featureGroupEntity),
                getAllFeatureGroupRelations(featureGroupEntity), null, null);

        return elements;
    }

    private static Collection<Relation> getAllFeatureGroupRelations(FeatureGroupEntity featureGroup) {
        Collection<Relation> relations = new ArrayList<>();

        relations.addAll(featureGroup.getEntitlementPoolIds().stream().map(rel ->
                VlmZusammenUtil.createRelation( RelationType.FeatureGroupToEntitlmentPool, rel))
                .collect(Collectors.toList()));

        relations.addAll(featureGroup.getLicenseKeyGroupIds().stream().map(rel ->
                VlmZusammenUtil.createRelation( RelationType.FeatureGroupToLicenseKeyGroup, rel))
                .collect(Collectors.toList()));

        relations.addAll(featureGroup.getReferencingLicenseAgreements().stream().map(rel ->
                VlmZusammenUtil.createRelation( RelationType.FeatureGroupToReferencingLicenseAgreement,
                        rel)).collect(Collectors.toList()));

        return relations;
    }

    private static Info getFeatureGroupInfo(FeatureGroupEntity featureGroup) {

        Info info = new Info();
        info.setName(featureGroup.getName());
        info.setDescription(featureGroup.getDescription());
        info.addProperty("partNumber", featureGroup.getPartNumber());
        info.addProperty("manufacturerReferenceNumber", featureGroup.getManufacturerReferenceNumber());

        return info;
    }


    private static List<String> getFeatureGroupNamespace(FeatureGroupEntity featureGroupEntity) {
        return ElementHandler.getElementPath("");
    }
}
