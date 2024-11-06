package io.jsong.sample_server;
/*
 * 
 */

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.codec.digest.MurmurHash2;
import org.junit.jupiter.api.Test;

public class HashTest {

  @Test
  void HashCollisionTest() {
    var numberOfPartition = 5;
    var numberOfHash = 10;
    var loop = 20;


    double totalCollision = 0;

    for (int i = 0; i <loop; i++) {
      totalCollision += hashCollision(numberOfPartition, numberOfHash);
    }

    System.out.println("Avg collision rate: " + (totalCollision * 100)/(loop * numberOfHash) + "%" + " in " + (loop * numberOfHash) + " chance");

  }

  double hashCollision(int numberOfPartition, int numberOfHash) {

    var partitionCollision = new int[numberOfPartition];

    for (int i = 0; i < numberOfHash; i++) {

      byte[] b = new byte[16];

      new SecureRandom().nextBytes(b);
      var id = UUID.nameUUIDFromBytes(b).toString();

      int hash = Math.abs(MurmurHash2.hash32(id));
      var idx = hash % numberOfPartition;
      partitionCollision[idx] += 1;
    }


    //Get collision rate
    AtomicInteger collision = new AtomicInteger();
    for (int v : partitionCollision) {
      if (v > 1) {
        collision.addAndGet(v - 1);
      }
    }
    double collisionRate = (double) (collision.get() * 100)/numberOfHash;

    System.out.println("Collision Rate for : "
        + numberOfPartition + " partition with "
        + numberOfHash + " of assigned id ="
        + collisionRate + "%");

//    if(collisionRate != 0){
      System.out.println("Hash: " + Arrays.toString(partitionCollision));
//    }


    return collision.get();

  }

}
