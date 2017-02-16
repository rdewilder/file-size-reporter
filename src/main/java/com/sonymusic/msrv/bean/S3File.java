package com.sonymusic.msrv.bean;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "s3_file")
public class S3File implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "file_id")
  private Long fileId;

  @Column(name = "source_id")
  private Long sourceId;
  
  @Column(name = "key")
  private String key;
  
  @Column(name = "file_path")
  private String filePath;

  @Column(name = "status")
  private Long status;

  @Column(name = "message")
  private String message;

  public Long getFileId() {
    return fileId;
  }

  public void setFileId(Long fileId) {
    this.fileId = fileId;
  }

  public Long getSourceId() {
    return sourceId;
  }

  public void setSourceId(Long sourceId) {
    this.sourceId = sourceId;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public Long getStatus() {
    return status;
  }

  public void setStatusId(Long status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("S3File [fileId=").append(fileId).append(", key=").append(key).append(", filePath=").append(filePath)
        .append(", status=").append(status).append(", message=").append(message).append("]");
    return builder.toString();
  }
}
