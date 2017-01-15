package xyz.donit.rest;

import org.apache.commons.dbcp2.BasicDataSource;
import org.glassfish.hk2.api.Factory;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

public class DataSourceFactory implements Factory<BasicDataSource> {
    private static BasicDataSource connectionPool;
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    public DataSourceFactory(){
        try {
            Class.forName(JDBC_DRIVER);
            this.connectionPool = new BasicDataSource();
            URI dbUri = new URI(System.getenv("DONITDB"));
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost()+":"+dbUri.getPort() + dbUri.getPath();
            if (dbUri.getUserInfo() != null) {
                connectionPool.setUsername(dbUri.getUserInfo().split(":")[0]);
                connectionPool.setPassword(dbUri.getUserInfo().split(":")[1]);
            }
            connectionPool.setDriverClassName("org.postgresql.Driver");
            connectionPool.setUrl(dbUrl);
            connectionPool.setInitialSize(1);
            connectionPool.setDefaultAutoCommit(true);
        }catch (URISyntaxException e){
            e.printStackTrace();
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
