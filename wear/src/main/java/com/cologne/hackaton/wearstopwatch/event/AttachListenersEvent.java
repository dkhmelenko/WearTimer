package com.cologne.hackaton.wearstopwatch.event;

/**
 * Created by admin on 3/5/2015.
 */
public class AttachListenersEvent {

  private StartStopwatchEvent.StopWatchTickCallback tickCallback;

  public AttachListenersEvent(
      StartStopwatchEvent.StopWatchTickCallback tickCallback) {
    this.tickCallback = tickCallback;
  }

  public StartStopwatchEvent.StopWatchTickCallback getTickCallback() {
    return tickCallback;
  }
}
