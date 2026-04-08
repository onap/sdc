package org.openecomp.sdc.vendorsoftwareproduct.dao;

import java.util.List;
import java.util.UUID;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusRecord;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.core.PagingIterable;

@Dao
public interface VspUploadStatusRecordDaoInternal {
    @Insert
    void save(VspUploadStatusRecord record);

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId")
    PagingIterable<VspUploadStatusRecord> findAllByVspIdAndVersionId(String vspId, String vspVersionId);

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId AND lock_id = :lockId")
    VspUploadStatusRecord findByVspIdAndVersionIdAndLockId(String vspId, String vspVersionId, UUID lockId);

    @Select(customWhereClause = "vsp_id = :vspId AND vsp_version_id = :vspVersionId AND status = 'IN_PROGRESS'")
    PagingIterable<VspUploadStatusRecord> findAllIncomplete(String vspId, String vspVersionId);

    
}
