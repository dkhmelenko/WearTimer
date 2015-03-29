package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class StatusResponseEvent {

  private boolean isRunning;

  public StatusResponseEvent(boolean isRunning) {
    this.isRunning = isRunning;
  }

  public boolean isRunning() {
    return isRunning;
  }
}
