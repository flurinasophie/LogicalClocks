package com.assignment4.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UdpLTClient {

  public static void main(String[] args) throws Exception {
    // Prompt the user to enter their ID
    System.out.println("Enter your id (1 to 4): ");
    Scanner idInput = new Scanner(System.in);
    int id = idInput.nextInt(); // Read the user's ID
    int port = 4040; // Server's port number

    // Prepare the client socket for communication
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress ipAddress = InetAddress.getByName("localhost"); // Server's IP address

    // Initialize the buffers for sending data
    byte[] sendData;
    int startTime = 0;

    // Initialize Lamport Clock with a starting timestamp
    LamportTimestamp lc = new LamportTimestamp(startTime);

    // Start a separate thread to continuously listen for incoming messages from the server
    LTClientThread client = new LTClientThread(clientSocket, lc);
    Thread receiverThread = new Thread(client);
    receiverThread.start();

    String joinMessage = "join:0:" + id;

    sendData = joinMessage.getBytes();
    DatagramPacket joinPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);

    clientSocket.send(joinPacket);

    // Prompt the user to enter messages
    System.out.println("[Client " + id + "] Enter any message:");
    Scanner input = new Scanner(System.in);

    while (true) {
      try {
        String messageBody = input.nextLine();

        if (messageBody.equalsIgnoreCase("quit")) {
          clientSocket.close();
          receiverThread.interrupt();
          System.exit(0);
        }

        if (!messageBody.isEmpty()) {

          lc.tick();

          int currentTimestamp = lc.getCurrentTimestamp();

          String responseMessage = messageBody + ":" + currentTimestamp + ":" + id;

          sendData = responseMessage.getBytes();
          DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
          clientSocket.send(sendPacket);

          System.out.println("Sent message: " + messageBody + " with timestamp: " + currentTimestamp);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}