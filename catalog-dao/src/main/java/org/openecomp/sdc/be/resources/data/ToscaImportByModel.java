package org.openecomp.sdc.be.resources.data;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@CqlName("tosca_import_by_model")
@EqualsAndHashCode
public class ToscaImportByModel {

    @PartitionKey
    @CqlName("model_id")
    private String modelId;

    @PartitionKey(1)
    @CqlName("full_path")
    private String fullPath;

    @CqlName("content")
    @EqualsAndHashCode.Exclude
    private String content;
}
