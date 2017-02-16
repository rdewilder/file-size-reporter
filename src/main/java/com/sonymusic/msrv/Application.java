package com.sonymusic.msrv;

import com.sonymusic.msrv.service.S3ReporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Application {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
  
  private AnnotationConfigApplicationContext ctx;

  @Autowired
  private S3ReporterService s3ReporterService;

  public static void main(String[] args) {
    LOGGER.info("Starting...");
    SpringApplication.run(Application.class);

  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        if (ctx != null) {
          ctx.close();
        }
        LOGGER.warn("Shutting down.");
      }
    });
  }
}
