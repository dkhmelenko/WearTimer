package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class StartTimerEvent {

  private int seconds;
  private int minutes;

  public StartTimerEvent(int seconds, int minutes) {
    this.seconds = seconds;
    this.minutes = minutes;
  }

  public int getSeconds() {
    return seconds;
  }

  public int getMinutes() {
    return minutes;
  }
}
