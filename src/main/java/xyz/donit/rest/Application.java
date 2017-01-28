package xyz.donit.rest;

import org.apache.commons.dbcp2.BasicDataSource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.sql.DataSource;

/**
 * Created by felix on 1/12/17.
 */
public class Application extends ResourceConfig {
    public static class ApplicationBinder extends AbstractBinder{
        @Override
        protected void configure() {
            bind(new DataSourceFactory().provide()).to(DataSource.class);
        }
    }

    public Application(){
        // Application wide set up
        register(new ApplicationBinder());
        registerClasses(JacksonFeature.class);
        packages(true, "xyz.donit.rest");
    }
}
