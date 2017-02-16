package com.sonymusic.msrv.main;

import static com.sonymusic.msrv.main.SpringExtension.SpringExtProvider;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import akka.actor.ActorSystem;


/**
 * The application configuration.
 */
@Configuration
@EnableJpaRepositories(basePackages={"com.sonymusic.msrv.repository"})
@EnableTransactionManagement
@PropertySource("${spring.config.location}")
class ApplicationConfiguration {
  
  // the application context is needed to initialize the Akka Spring Extension
  @Autowired
  private ApplicationContext applicationContext;
  
  @Autowired
  private Environment environment;

  /**
   * Actor system singleton for this application.
   */
  @Bean
  public ActorSystem actorSystem() {
    ActorSystem system = ActorSystem.create("S3FileAdapter");
    // initialize the application context in the Akka Spring Extension
    SpringExtProvider.get(system).initialize(applicationContext);
    return system;
  }
  
  @Bean
  public DataSource dataSource() throws PropertyVetoException {
    ComboPooledDataSource dataSource = new ComboPooledDataSource();
    dataSource.setDriverClass(environment.getProperty("s3r.datasource.driverClassName"));
    dataSource.setJdbcUrl(environment.getProperty("s3r.datasource.url"));
    dataSource.setUser(environment.getProperty("s3r.datasource.username"));
    dataSource.setPassword(environment.getProperty("s3r.datasource.password"));
    return dataSource;
  }
  
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws PropertyVetoException{
    LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource());
    entityManagerFactory.setPackagesToScan(new String[] { "com.sonymusic.msrv.bean" });
    entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactory.setJpaDialect(new HibernateJpaDialect());
    return entityManagerFactory;
  }
  
  @Bean
  @Autowired
  public PlatformTransactionManager transactionManager(DataSource dataSource) throws PropertyVetoException {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory( entityManagerFactory().getObject() );
    transactionManager.setDataSource(dataSource);
    return transactionManager;
  }
}