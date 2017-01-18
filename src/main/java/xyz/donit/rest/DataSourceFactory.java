package xyz.donit.rest;

import org.apache.commons.dbcp2.BasicDataSource;
import org.glassfish.hk2.api.Factory;

public class DataSourceFactory implements Factory<BasicDataSource> {
    private static BasicDataSource connectionPool;
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String HOST = "donit-database";
    private static final String PORT = "26257";
    private static final String DBNAME = "donit";
    public DataSourceFactory(){
        try {
            Class.forName(JDBC_DRIVER);
            this.connectionPool = new BasicDataSource();
            String dbUrl = "jdbc:postgresql://" + HOST+":"+PORT + "/"+ DBNAME;
            connectionPool.setUsername(USERNAME);
            connectionPool.setDriverClassName("org.postgresql.Driver");
            connectionPool.setUrl(dbUrl);
            connectionPool.setInitialSize(1);
            connectionPool.setDefaultAutoCommit(true);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public BasicDataSource provide(){
        return this.connectionPool;
    }
    public void dispose(BasicDataSource  bds){

    }
}
