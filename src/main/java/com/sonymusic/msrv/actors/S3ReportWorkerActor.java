package com.sonymusic.msrv.actors;

import akka.actor.UntypedActor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sonymusic.msrv.bean.S3File;
import com.sonymusic.msrv.enums.ActorMessageEnum;
import com.sonymusic.msrv.enums.S3FileStatusEnum;
import com.sonymusic.msrv.repository.S3FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.inject.Named;

@Named("S3ReportWorkerActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class S3ReportWorkerActor extends UntypedActor {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(S3ReportWorkerActor.class);

  @Autowired
  AmazonS3 s3Client;

  @Autowired
  private Environment environment;

  @Autowired
  private S3FileRepository s3FileRepository;

    @Autowired
    private String bucketName;
  
  @Override
  public void onReceive(Object msg) throws Throwable {
    LOGGER.info("Message: {}", msg);
    try {
      if (msg instanceof S3File) {
        S3File s3File = (S3File) msg;
        LOGGER.info("Processing S3File {}",s3File);
        ObjectMetadata metaData = s3Client.getObjectMetadata(bucketName, "88691935959_AdamLambert_Trespassing_DDP.zip");
        LOGGER.info("Content length {}", metaData.getContentLength());
        s3File.setStatusId(Long.valueOf(S3FileStatusEnum.PROCESSED.ordinal()));
          s3FileRepository.save(s3File);
      }
    } catch (Exception e) {
      LOGGER.error("An error occurred in PageProcessWorkerActor while processing message: " + msg, e);
    } finally {
      //sender().tell(ActorMessageEnum.COMPLETE.getMessage(), self());
    }
  }



}
