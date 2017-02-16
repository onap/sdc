package org.openecomp.core.nosqldb;

import org.openecomp.core.nosqldb.api.NoSqlDb;

import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class NoSqlDbTest {

    private static NoSqlDb noSqlDb;


    @Test
    public void testNoSqlDbFactoryFactoryInit(){
        this.noSqlDb = NoSqlDbFactory.getInstance().createInterface();
        Assert.assertNotNull(this.noSqlDb);
        Assert.assertEquals(this.noSqlDb.getClass().getName(),"org.openecomp.core.nosqldb.impl.cassandra.CassandraNoSqlDbImpl");
    }

    @Test(dependsOnMethods = {"testNoSqlDbFactoryFactoryInit"})
    public void testCreateTable(){
        this.noSqlDb.execute("test.drop",null);
        this.noSqlDb.execute("test.create",null);
    }

    @Test(dependsOnMethods = {"testCreateTable"})
    public void testInsertTable(){
        this.noSqlDb.insert("test",new String[]{"name","value"},new String[]{"TestName","testValue"});
        this.noSqlDb.execute("test.insert",new String[]{"TestName2","testValue2"});
    }

    @Test(dependsOnMethods = {"testInsertTable"})
    public void gettestSelectTable(){
        ResultSet result = this.noSqlDb.execute("test.select.all",null);
        List<Row> rows = result.all();
        Assert.assertEquals(rows.size(),2);
        for (Row row:rows){
            System.out.format("%s %s\n", row.getString("name"), row.getString("value"));
        }
    }
}
