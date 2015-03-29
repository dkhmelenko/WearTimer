package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class TimerTickEvent {

  private long millisUntil;

  public TimerTickEvent(long millis) {
    this.millisUntil = millis;
  }

  public long getMillisUntil() {
    return millisUntil;
  }
}
