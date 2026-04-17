package org.openecomp.sdc.vendorsoftwareproduct.dao;

import com.amdocs.zusammen.datatypes.item.Resolution;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
@Dao
public interface VspMergeHintDao {

    @Query("SELECT model_id, model_resolution FROM vsp_merge_hint "
         + "WHERE space=:space AND item_id=:itemId AND version_id=:versionId")
    Row get(String space, String itemId, String versionId);

    @Query("UPDATE vsp_merge_hint SET model_id=:vspModelId, model_resolution=:modelResolution "
         + "WHERE space=:space AND item_id=:itemId AND version_id=:versionId")
    void update(String vspModelId, Resolution modelResolution, String space, String itemId, String versionId);

    @Query("UPDATE vsp_merge_hint SET model_resolution=:modelResolution "
         + "WHERE space=:space AND item_id=:itemId AND version_id=:versionId")
    void updateModelResolution(Resolution modelResolution, String space, String itemId, String versionId);

    @Query("DELETE FROM vsp_merge_hint WHERE space=:space AND item_id=:itemId AND version_id=:versionId")
    void delete(String space, String itemId, String versionId);
}

