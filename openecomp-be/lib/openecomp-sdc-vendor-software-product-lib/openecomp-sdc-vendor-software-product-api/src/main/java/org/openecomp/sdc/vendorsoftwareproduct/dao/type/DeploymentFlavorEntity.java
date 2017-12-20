package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;

@Table(keyspace = "dox", name = "vsp_deployment_flavor")
public class DeploymentFlavorEntity implements CompositionEntity{
    private static final String ENTITY_TYPE = "Vendor Software Product Deployment Flavor";

    @PartitionKey
    @Column(name = "vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    @Frozen
    private Version version;
    @ClusteringColumn
    @Column(name = "deployment_flavor_id")
    private String id;
    @Column(name = "composition_data")
    private String compositionData;
    @Column(name = "questionnaire_data")
    private String questionnaireData;


    public DeploymentFlavorEntity(){

    }

    /**
     * Instantiates a new DeploymentFlavor entity.
     *
     * @param vspId   the vsp id
     * @param version the version
     * @param id      the id
     */
    public DeploymentFlavorEntity(String vspId, Version version, String id) {
        this.vspId = vspId;
        this.version = version;
        this.id = id;
    }
    @Override
    public CompositionEntityType getType() {
        return CompositionEntityType.deployment;
    }

    @Override
    public CompositionEntityId getCompositionEntityId() {
        return new CompositionEntityId(getId(), new CompositionEntityId(getVspId(), null));
    }

    @Override
    public String getCompositionData() {
        return compositionData;
    }

    @Override
    public void setCompositionData(String compositionData) {
        this.compositionData = compositionData;
    }

    @Override
    public String getQuestionnaireData() {
        return questionnaireData;
    }

    @Override
    public void setQuestionnaireData(String questionnaireData) {
        this.questionnaireData = questionnaireData;
    }

    public String getVspId() {
        return vspId;
    }

    public void setVspId(String vspId) {
        this.vspId = vspId;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getVspId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public void setVersion(Version version) {
        this.version = version;
    }

   public DeploymentFlavor getDeploymentFlavorCompositionData() {
        return compositionData == null ? null : JsonUtil.json2Object(compositionData, DeploymentFlavor.class);
    }

    public void setDeploymentFlavorCompositionData(DeploymentFlavor deploymentFlavor) {
        this.compositionData = deploymentFlavor == null ? null : JsonUtil.object2Json(deploymentFlavor);
    }

    @Override
    public int hashCode() {
        int result = vspId != null ? vspId.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (compositionData != null ? compositionData.hashCode() : 0);
        result = 31 * result + (questionnaireData != null ? questionnaireData.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        DeploymentFlavorEntity that = (DeploymentFlavorEntity) object;

        if (vspId != null ? !vspId.equals(that.vspId) : that.vspId != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (compositionData != null ? !compositionData.equals(that.compositionData)
                : that.compositionData != null) {
            return false;
        }
        return questionnaireData != null ? questionnaireData.equals(that.questionnaireData)
                : that.questionnaireData == null;

    }
}