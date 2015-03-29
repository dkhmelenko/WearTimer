package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class TimerStatusResponseEvent {

  private boolean isRunning;

  public TimerStatusResponseEvent(boolean isRunning) {
    this.isRunning = isRunning;
  }

  public boolean isRunning() {
    return isRunning;
  }
}
