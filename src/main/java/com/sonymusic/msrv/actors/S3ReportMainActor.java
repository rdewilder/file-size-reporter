package com.sonymusic.msrv.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinPool;
import akka.routing.RouterConfig;
import com.sonymusic.msrv.bean.S3File;
import com.sonymusic.msrv.enums.ActorMessageEnum;
import com.sonymusic.msrv.service.S3ReporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.inject.Named;
import java.util.List;

import static com.sonymusic.msrv.main.SpringExtension.SpringExtProvider;

@Named("S3ReportMainActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class S3ReportMainActor extends UntypedActor {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3ReportMainActor.class);

  @Autowired
  private S3ReporterService s3ReporterService;
  
  @Autowired
  private ActorSystem actorSystem;

    @Autowired
    private Environment environment;
  
  private ActorRef s3ReportWorkerActor;
  
  private Cancellable poll = null;
  
  private Long runningActors = 0L;
  private Integer maxS3ReportWorkerActors;
  private long messagePoolInterval = 0L;
  private final long messageMaxPoolInterval = 60000L;
  private Long sourceId = 0L;
  
  @Override
  public void preStart() throws Exception {
    super.preStart();
    maxS3ReportWorkerActors = Integer.parseInt(environment.getProperty("s3.report.worker.count"));
    sourceId = Long.parseLong(environment.getProperty("s3.report.sourceId"));

    RouterConfig routerConfig = new RoundRobinPool(maxS3ReportWorkerActors);
    s3ReportWorkerActor = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("S3ReportWorkerActor").withRouter(routerConfig), "S3ReportWorkerActor");
  }
  
  @Override
  public void postStop() {
    poll.cancel();
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    LOGGER.info("Message: {}", msg);
    if (ActorMessageEnum.START.getMessage().equalsIgnoreCase((String) msg)) {
        List<S3File> s3Files = s3ReporterService.getUnprocessedS3Files(sourceId);

        LOGGER.info("S3Files count: {}", s3Files.size());
        if (s3Files.size() > 0) {
          for (S3File s3File : s3Files) {
            s3ReportWorkerActor.tell(s3File, self());
          }
        } else {
            LOGGER.info("No S3 files to process for source id {}", sourceId);
        }

    } else {
      LOGGER.info("Invalid message: {}", msg);
    }
  }


}
