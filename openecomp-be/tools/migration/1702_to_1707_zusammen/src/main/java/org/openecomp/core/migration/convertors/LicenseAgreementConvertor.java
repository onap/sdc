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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 4/25/2017
 */
public class LicenseAgreementConvertor {

    private static Logger logger = LoggerFactory.getLogger(LicenseAgreementConvertor.class);
    private static Set<String> LicenseAgreementsLoaded = new HashSet<>();

    public static ElementEntityContext convertLicenseAgreementToElementContext(LicenseAgreementEntity licenseAgreementEntity) {

        return new ElementEntityContext("GLOBAL_USER", new
                ElementContext(licenseAgreementEntity.getVendorLicenseModelId(), licenseAgreementEntity.getVersion().toString()));
    }


    public static CollaborationElement[] convertLicenseAgreementToElement(LicenseAgreementEntity licenseAgreementEntity) {
//        printMessage(logger, "source LicenseAgreementEntity -> " + licenseAgreementEntity.toString());
        CollaborationElement[] elements;
        List<String> featureGroupNamespace = getLicenseAgreementNamespace(licenseAgreementEntity);

        int index = 0;
        String featureGroupsEntityId = StructureElement.LicenseAgreements.name();
        String uniqueId = licenseAgreementEntity.getVendorLicenseModelId() + "_" + licenseAgreementEntity.getVersion().toString();

        if (LicenseAgreementsLoaded.contains(uniqueId)) {
            elements = new CollaborationElement[1];
        } else {
            LicenseAgreementsLoaded.add(uniqueId);
            elements = new CollaborationElement[2];
            elements[index] = ElementHandler.getElementEntity(
                    licenseAgreementEntity.getVendorLicenseModelId(), licenseAgreementEntity.getVersion().toString(),
                    featureGroupsEntityId, featureGroupNamespace,
                    ElementHandler.getStructuralElementInfo(StructureElement.LicenseAgreements.name()),
                    null, null, null);
            index++;
        }

        featureGroupNamespace.add(featureGroupsEntityId);

        elements[index] = ElementHandler.getElementEntity(
                licenseAgreementEntity.getVendorLicenseModelId(), licenseAgreementEntity.getVersion().toString(),
                licenseAgreementEntity.getId(), featureGroupNamespace, getLicenseAgreementInfo(licenseAgreementEntity),
                licenseAgreementEntity.getFeatureGroupIds().stream().map(rel ->
                    VlmZusammenUtil.createRelation( RelationType.LicenseAgreementToFeatureGroup, rel))
                        .collect(Collectors.toList()), null, null);

        return elements;
    }

    private static Info getLicenseAgreementInfo(LicenseAgreementEntity licenseAgreement) {

        Info info = new Info();
        info.setName(licenseAgreement.getName());
        info.setDescription(licenseAgreement.getDescription());
        info.addProperty("licenseTerm", licenseAgreement.getLicenseTerm());
        info.addProperty("requirementsAndConstrains", licenseAgreement.getRequirementsAndConstrains());

        return info;
    }

    private static List<String> getLicenseAgreementNamespace(LicenseAgreementEntity licenseAgreement) {
        return ElementHandler.getElementPath("");
    }

}
