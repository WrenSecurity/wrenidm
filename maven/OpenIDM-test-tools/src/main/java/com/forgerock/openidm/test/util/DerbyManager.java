package com.forgerock.openidm.test.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.io.FileUtils;

/**
 * Helper class to start and stop embedded derby database with default resource tables.
 * @author elek
 */
public class DerbyManager {

    public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    protected String dbDir = "target/testdb";

    protected String conn = "jdbc:derby:" + dbDir;

    protected String createConn = conn + ";create=true";

    protected String shutdownConn = conn + ";shutdown=true";

    public void deleteDatabase() throws Exception {
        try {
            DriverManager.getConnection(shutdownConn);
        } catch (Exception ex) {
            //A clean shutdown always throws SQL exception XJ015, which can be ignored.
            //(from here http://db.apache.org/derby/papers/DerbyTut/embedded_intro.html#shutdown)
        }
        FileUtils.deleteDirectory(new File(dbDir));
    }

    public Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        return DriverManager.getConnection(conn, "", "");
    }

    public void createDatabase() throws Exception {
        if (new File(dbDir).exists()) {
            throw new IllegalArgumentException("Probably windows build will be failed.");
        }
        FileUtils.deleteDirectory(new File(dbDir));
        // attempt to create the database in the directory..
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(createConn, "", "");
            // create the database..
            stmt = conn.createStatement();

            stmt.execute("create table account(id varchar(50),password varchar(50),attr1 varchar(51),changelog int)");
            stmt.execute("insert into account values ('1','1','value1',3)");
            conn.commit();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
