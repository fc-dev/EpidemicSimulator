package dev.federicocapece.jdaze;

public class StopWatch {
  private long startTime = 0;
  private long stopTime = 0;
  private boolean running = false;

  public void start() {
    this.startTime = System.currentTimeMillis();
    this.running = true;
  }

  public void stop() {
    this.stopTime = System.currentTimeMillis();
    this.running = false;
  }

  public long getElapsedTime() {
    return running ? (System.currentTimeMillis() - startTime) : (stopTime - startTime);
  }

  public long getElapsedTimeSecs() {
    return getElapsedTime() / 1000;
  }

}