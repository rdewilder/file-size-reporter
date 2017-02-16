package com.sonymusic.msrv.service;

import com.sonymusic.msrv.bean.S3File;
import com.sonymusic.msrv.enums.S3FileStatusEnum;
import com.sonymusic.msrv.repository.S3FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("s3ReporterService")
public class S3ReporterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3ReporterService.class);

  @Autowired
  private S3FileRepository s3FileRepository;

  public List<S3File> getUnprocessedS3Files(Long sourceId) {
    LOGGER.info("In getNewS3Files");
    return s3FileRepository.findByStatusAndSourceId(Long.valueOf(S3FileStatusEnum.NEW.ordinal()), sourceId);
  }

}
