package org.openecomp.sdc.be.dao.cassandra;

import org.openecomp.sdc.be.resources.data.SdcSchemaFilesData;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface SdcSchemaFilesAccessor {
	@Query("SELECT * FROM sdcartifact.sdcschemafiles WHERE SDCRELEASENUM = :sdcreleasenum AND CONFORMANCELEVEL = :conformancelevel")
	Result<SdcSchemaFilesData> getSpecificSdcSchemaFiles(@Param("sdcreleasenum") String sdcreleasenum, @Param("conformancelevel") String conformancelevel);
}
