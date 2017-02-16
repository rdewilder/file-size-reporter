package com.sonymusic.msrv.repository;

import com.sonymusic.msrv.bean.S3File;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface S3FileRepository extends JpaRepository<S3File, Long> {

  @Transactional(readOnly = true)
  public List<S3File> findByStatusAndSourceId(Long status, Long sourceId);

}