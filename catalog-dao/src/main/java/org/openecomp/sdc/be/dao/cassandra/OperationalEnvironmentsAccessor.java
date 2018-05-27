package org.openecomp.sdc.be.dao.cassandra;

import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface OperationalEnvironmentsAccessor {
    @Query("SELECT * FROM sdcrepository.operationalenvironment WHERE status = :envStatus")
    Result<OperationalEnvironmentEntry> getByEnvironmentsStatus(@Param("envStatus") String envStatus);
}

