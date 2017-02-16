package com.sonymusic.msrv.enums;

public enum ActorMessageEnum {
  START("start"), POLL("poll"), COMPLETE("complete");
  
  private String message;
  
  ActorMessageEnum(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
