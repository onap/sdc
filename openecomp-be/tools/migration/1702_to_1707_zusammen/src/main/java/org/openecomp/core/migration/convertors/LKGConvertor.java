package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.RelationType;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VlmZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 4/24/2017
 */
public class LKGConvertor {

    private static Logger logger = LoggerFactory.getLogger(LKGConvertor.class);
    private static Set<String> compLKGLoaded = new HashSet<>();

    public static CollaborationElement[] convertLKGToElement(LicenseKeyGroupEntity licenseKeyGroupEntity) {
        CollaborationElement[] elements;
        List<String> lkgNamespace = getLKGNamespace(licenseKeyGroupEntity);

        int index = 0;
        String lkgsEntityId = StructureElement.LicenseKeyGroups.name();
        String uniqueId = licenseKeyGroupEntity.getVendorLicenseModelId() + "_" + licenseKeyGroupEntity.getVersion().toString();

        if (compLKGLoaded.contains(uniqueId)) {
            elements = new CollaborationElement[1];
        } else {
            compLKGLoaded.add(uniqueId);
            elements = new CollaborationElement[2];
            elements[index] = ElementHandler.getElementEntity(
                    licenseKeyGroupEntity.getVendorLicenseModelId(), licenseKeyGroupEntity.getVersion().toString(), lkgsEntityId, lkgNamespace,
                    ElementHandler.getStructuralElementInfo(StructureElement.LicenseKeyGroups.name()), null, null, null);
            index++;
        }

        lkgNamespace.add(lkgsEntityId);

        elements[index] = ElementHandler.getElementEntity(
                licenseKeyGroupEntity.getVendorLicenseModelId(), licenseKeyGroupEntity.getVersion().toString(), licenseKeyGroupEntity.getId(), lkgNamespace,
                getLKGInfo(licenseKeyGroupEntity), licenseKeyGroupEntity.getReferencingFeatureGroups().stream().map(rel ->
                VlmZusammenUtil.createRelation( RelationType.LicenseKeyGroupToReferencingFeatureGroup, rel))
                        .collect(Collectors.toList()), null, null);

        return elements;
    }

    private static Info getLKGInfo(LicenseKeyGroupEntity licenseKeyGroupEntity) {

        Info info = new Info();
        info.setName(licenseKeyGroupEntity.getName());
        info.setDescription(licenseKeyGroupEntity.getDescription());
        info.addProperty("LicenseKeyType", licenseKeyGroupEntity.getType());
        info.addProperty("operational_scope", licenseKeyGroupEntity.getOperationalScope());

        return info;
    }

    private static List<String> getLKGNamespace(LicenseKeyGroupEntity LKGEntity) {
        return ElementHandler.getElementPath("");
    }

    public static ElementEntityContext convertLKGToElementContext(LicenseKeyGroupEntity licenseKeyGroupEntity) {

        return new ElementEntityContext("GLOBAL_USER", new
                ElementContext(licenseKeyGroupEntity.getVendorLicenseModelId(), licenseKeyGroupEntity.getVersion().toString()));
    }

}
