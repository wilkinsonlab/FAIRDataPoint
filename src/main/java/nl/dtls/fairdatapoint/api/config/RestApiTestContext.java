/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dtls.fairdatapoint.api.config;

import java.io.IOException;
import nl.dtls.fairdatapoint.api.repository.StoreManager;
import nl.dtls.fairdatapoint.api.repository.StoreManagerException;
import nl.dtls.fairdatapoint.api.repository.impl.StoreManagerImpl;
import nl.dtls.fairdatapoint.utils.ExampleFilesUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring test context file. 
 * @author Rajaram Kaliyaperumal
 * @since 2016-02-11
 * @version 0.1
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "nl.dtls.fairdatapoint.*")
public class RestApiTestContext {
    @Bean(name="repository", initMethod = "initialize",
            destroyMethod = "shutDown")
    public Repository repository(final Environment env)
            throws RepositoryException, IOException, RDFParseException {
        // For tets we use only in memory
        Repository repository = ExampleFilesUtils.getRepository();
        return repository;
    }

    @Bean(name = "storeManager")
    @DependsOn({"repository", "prepopulateStore", "baseURI"})
    public StoreManager storeManager() throws RepositoryException,
            StoreManagerException {
        return new StoreManagerImpl();
    }

    @Bean(name = "properties")
    public static PropertySourcesPlaceholderConfigurer
        propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name = "baseURI")
    public String baseURI(final Environment env)  {
        String rdfBaseURI = env.getRequiredProperty("baseUri");
        return rdfBaseURI;
    }

    @Bean(name = "prepopulateStore")
    public boolean prepopulateStore(final Environment env)  {
        return true;
    }
    @Bean(name = "placeHolderFile")
    public String placeHolderFile()  {        
        return ExampleFilesUtils.FDP_METADATA_FILE;
    }
}
