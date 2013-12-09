package org.x4444.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class RestApiLoadTest implements Runnable {

  static ArrayBlockingQueue<String> errors = new ArrayBlockingQueue<String>(100);

  public static void main(String[] args) {

    // set user and password
    Authenticator.setDefault(new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("user1@customer1", "welcome".toCharArray());
      }
    });

    // Number of client threads - 10
    List<Thread> tt = new ArrayList<Thread>();
    for (int j = 0; j < 10; j++) {
      RestApiLoadTest c = new RestApiLoadTest();
      Thread t = new Thread(c);
      tt.add(t);
    }

    long ts = System.currentTimeMillis();
    for (Thread t : tt) {
      t.start();
    }

    for (Thread t : tt) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    ts = System.currentTimeMillis() - ts;
    // print number of error at the end of all threads execution
    System.out.println("Error count: " + errors.size());
    System.out.println("Time: " + ts + " ms");
  }

  @Override
  public void run() {
    // list of urls to call
    ArrayList<String> urls = new ArrayList<String>();
    urls.add("http://t1:8080/controller/rest/applications");
    urls.add("http://t1:8080/controller/rest/applications/2/nodes");
    urls.add("http://t1:8080/controller/rest/applications/2/business-transactions");
    urls.add("http://t1:8080/controller/rest/applications/2/tiers");

    // call server 1000 * number of urls times
    for (int i = 0; i < 1000; i++) {
      Collections.shuffle(urls);
      for (String url : urls) {
        String res = callAndRead(url);
        if (res != null) {
          errors.add(res);
        }
      }
    }
  }

  public String callAndRead(String url) {
    try {
      URL yahoo = new URL(url);

      URLConnection yc = yahoo.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
      try {
        String inputLine;

        // print top 3 lines from response
        int cnt = 0;
        while ((inputLine = in.readLine()) != null) {
          if (cnt < 3) {
            cnt++;
            System.out.println(inputLine);
          }
        }
      } finally {
        in.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage();
    }
    return null;
  }
}
