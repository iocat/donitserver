package xyz.donit.rest;

import org.apache.commons.dbcp2.BasicDataSource;
import org.glassfish.hk2.api.Factory;

import javax.sql.DataSource;

public class DataSourceFactory implements Factory<DataSource> {
    private static DataSource connectionPool;
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String HOST = "donit-database";
    private static final String PORT = "26257";
    private static final String DBNAME = "donit";
    public DataSourceFactory(){
        try {
            Class.forName(JDBC_DRIVER);
            BasicDataSource ds = new BasicDataSource();
            String dbUrl = "jdbc:postgresql://" + HOST+":"+PORT + "/"+ DBNAME;
            ds.setUsername(USERNAME);
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUrl(dbUrl);
            ds.setInitialSize(10);
            ds.setDefaultAutoCommit(true);
            this.connectionPool = ds;
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public DataSource provide(){
        return this.connectionPool;
    }
    public void dispose(DataSource  bds){

    }
}
