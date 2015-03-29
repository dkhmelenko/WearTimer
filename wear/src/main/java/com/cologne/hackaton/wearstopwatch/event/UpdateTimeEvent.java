package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class UpdateTimeEvent {

  private long time;

  public UpdateTimeEvent(long time) {
    this.time = time;
  }

  public long getTime() {
    return time;
  }
}
