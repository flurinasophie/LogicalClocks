package com.assignment4.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// This Class handles the continuous listening for incoming messages from the server
public class LTClientThread implements Runnable {

  private final DatagramSocket clientSocket;
  private final LamportTimestamp lc;
  byte[] receiveData = new byte[1024];

  public LTClientThread(DatagramSocket clientSocket, LamportTimestamp lc) {
    this.clientSocket = clientSocket;
    this.lc = lc;
  }

  @Override
  public void run() {
    // Write your code here to continuously listen for incoming messages from the server and display them.
    try {
      while (true) {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

        String[] parts = receivedMessage.split(":");
        if (parts.length < 3) {
          continue;
        }

        String messageBody = parts[0];
        int receivedTimestamp = Integer.parseInt(parts[1]);
        int senderId = Integer.parseInt(parts[2]);

        lc.updateClock(receivedTimestamp);

        System.out.println("Client " + senderId + ": " + messageBody + ":" + receivedTimestamp);
        System.out.println("Current clock: " + lc.getCurrentTimestamp());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}