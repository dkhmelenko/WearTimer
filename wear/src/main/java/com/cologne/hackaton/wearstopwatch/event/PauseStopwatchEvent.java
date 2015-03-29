package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class PauseStopwatchEvent {

  private PauseTimeReceiver callback;

  public PauseStopwatchEvent(PauseTimeReceiver callback) {
    this.callback = callback;
  }

  public PauseTimeReceiver getCallback() {
    return callback;
  }

  public interface PauseTimeReceiver {
    public void receivePauseTime(long pauseTime);
  }
}
