/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.forgerock.openidm.repo.test;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author katuska
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../../../../application-context-repository.xml", "../../../../../application-context-repository-test.xml"})
public class UserTypeDataTest {

    @Autowired
    private DataSource dataSource;


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Test
    @Ignore
    public void getDataFromBase() throws Exception {
        // database connection

        Connection con = DataSourceUtils.getConnection(getDataSource());
        IDatabaseConnection connection = new DatabaseConnection(con);
      // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full-dataset.xml"));
    }

    @Test
    @Ignore
    public void setUpDatabase() throws Exception{


        Connection con = DataSourceUtils.getConnection(getDataSource());
        IDatabaseConnection connection = new DatabaseConnection(con);

        // initialize your dataset here
        IDataSet dataSet = new FlatXmlDataSet(new File("full-dataset.xml"));

        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } finally {
            connection.close();
        }

    }

}
