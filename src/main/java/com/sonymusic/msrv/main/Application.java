package com.sonymusic.msrv.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sonymusic.msrv.enums.ActorMessageEnum;
import com.sonymusic.msrv.repository.S3FileRepository;
import com.sonymusic.msrv.service.S3ReporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.sonymusic.msrv.main.SpringExtension.SpringExtProvider;

@SpringBootApplication
public class Application {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
  
  private AnnotationConfigApplicationContext ctx;

  @Autowired
  private S3ReporterService s3ReporterService;

  public static void main(String[] args) {
    LOGGER.info("Starting...");
    Application application = new Application();
    application.addShutdownHook();
    application.start();
  }

  private void start() {
    ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.sonymusic.msrv.main");
    ctx.scan("com.sonymusic.msrv.service");
    ctx.scan("com.sonymusic.msrv.repository");
    ctx.scan("com.sonymusic.msrv.bean");
    ctx.scan("com.sonymusic.msrv.enums");
    ctx.scan("com.sonymusic.msrv.actors");
    ctx.scan("com.sonymusic.msrv.utils");
    ctx.refresh();

    ActorSystem system = ctx.getBean(ActorSystem.class);
    ActorRef s3ReportMainActor = system.actorOf(SpringExtProvider.get(system).props("S3ReportMainActor"), "S3ReportMainActor");
      s3ReportMainActor.tell(ActorMessageEnum.START.getMessage(), null);
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
