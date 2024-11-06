package io.jsong.sample_server.repositories;
/*
 * 
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import org.springframework.kafka.support.Acknowledgment;

public class RecordProcessingSignal {

  private Acknowledgment acknowledgment;
  private final Lock lock = new ReentrantLock();
  private final Condition doneProcessingCondition = lock.newCondition();
  private boolean isDoneProcessing = false;


  public RecordProcessingSignal(Acknowledgment acknowledgment) {
    this.acknowledgment = acknowledgment;
  }

  public RecordProcessingSignal() {
  }


  public void waitUntilDone() {
    lock.lock();
    try {

      while (!isDoneProcessing) {
        doneProcessingCondition.await();
      }

    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

  public void signalDone(){
    lock.lock();

    try{
      isDoneProcessing = true;

      //Ack
      if(acknowledgment != null) {
        acknowledgment.acknowledge();
      }

      //Wake up any thread
      doneProcessingCondition.signal();
    } finally {
      lock.unlock();
    }
  }

}
