package com.assignment4.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VectorClientThread implements Runnable {

  private final DatagramSocket clientSocket;
  private final VectorClock vcl;
  private final int id;
  private final byte[] receiveData = new byte[1024]; // Buffer for incoming data
  private final List<Message> buffer = new ArrayList<>(); // This buffer can be used for Task 2.2

  public VectorClientThread(DatagramSocket clientSocket, VectorClock vcl, int id) {
    this.clientSocket = clientSocket;
    this.vcl = vcl;
    this.id = id;
  }

  @Override
  public void run() {

    try {
      while (true) {
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        clientSocket.receive(receivePacket);
        String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());

        // Parse the message format: "message:[v1, v2, v3, v4]:id"
        String[] parts = receivedData.split(":", 3);
        if (parts.length < 3) {
          continue;
        }

        String messageBody = parts[0];
        String vectorClockString = parts[1];
        int senderId = Integer.parseInt(parts[2]);

        // Split the received vector clock into its components using .replaceAll("[\\[\\]]", "").split(",\\s*")
        String[] clockComponents = vectorClockString.replaceAll("[\\[\\]]", "").split(",\\s*");
        VectorClock senderClock = new VectorClock(clockComponents.length);
        for (int i = 0; i < clockComponents.length; i++) {
          senderClock.setVectorClock(i, Integer.parseInt(clockComponents[i]));
        }

        // Create Message object
        Message message = new Message(messageBody, senderClock, senderId);

        // Task 2.2: Check if message can be accepted or needs to be buffered
        if (vcl.checkAcceptMessage(senderId - 1, senderClock)) {
          // Message can be delivered - display it
          displayMessage(message);

          // Check if any buffered messages can now be delivered
          checkBuffer();
        } else {
          // Buffer the message for later delivery
          buffer.add(message);
          System.out.println("Buffered message from Client " + senderId);
        }
      }
    } catch (IOException e) {
      if (!clientSocket.isClosed()) {
        e.printStackTrace();
      }
    }
  }

  // Check buffered messages and deliver those that can now be accepted
  private void checkBuffer() {
    boolean delivered;
    do {
      delivered = false;
      Iterator<Message> iterator = buffer.iterator();
      while (iterator.hasNext()) {
        Message bufferedMessage = iterator.next();
        VectorClock bufferedClock = bufferedMessage.getClock();
        int bufferedSenderId = bufferedMessage.getSenderID();

        if (vcl.checkAcceptMessage(bufferedSenderId - 1, bufferedClock)) {
          displayMessage(bufferedMessage);
          iterator.remove();
          delivered = true;
          break; // Restart iteration after delivery
        }
      }
    } while (delivered && !buffer.isEmpty());
  }

  private void displayMessage(Message message) {
    if (message == null) {
      return;
    }

    // Print out the message with its vector clock
    System.out.println("Client " + message.getSenderID() + ": " + message.getMessage() + ": " + message.getClock().showClock());

    // Update the vector clock without ticking on receive
    vcl.updateClock(message.getClock());

    // Display the updated vector clock
    System.out.println("Current clock: " + vcl.showClock());
  }
}