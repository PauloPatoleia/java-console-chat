package org.academiadecodigo.ascimos.consolechat.Server;

import java.net.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ChatServer {

    public static final int PORT = 9000;

    private ServerSocket serverSocket;
    private List<ClientThread> clientThreads = new ArrayList<ClientThread>();

    public void connect(Socket socket) {

        try {
            ClientThread newClient = new ClientThread(socket);
            UUID randomUserId = UUID.randomUUID();
            newClient.setName(("User-" + randomUserId.hashCode()).replace("-", ""));
            clientThreads.add(newClient);
            broadcast(newClient, newClient.getName() + " has joined the room.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void disconnect(ClientThread client) {

        Iterator<ClientThread> itr = clientThreads.iterator();

        while(itr.hasNext()) {

            if (itr.next().equals(client)) {
                itr.remove();
            }
            break;
        }
    }


    public void broadcast(ClientThread activeClient, String message) {

        for(ClientThread client: clientThreads) {

            if(!client.equals(activeClient)) {
                client.sendMessage(message);
            }
        }
    }

    public void start() {

        try {

            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on " + PORT);

            while(true) {
                Socket socket = serverSocket.accept();
                connect(socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }
    }

    private class ClientThread extends Thread {

        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientThread(Socket socket) throws IOException {

            this.clientSocket = socket;
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(),true);
            start();
        }

        public void readMessage(String message) {
            broadcast(this, message);
        }

        public void sendMessage(String message) {
            output.println(message);
        }

        public void close() {

            try {
                if(input != null)
                    input.close();
                if(output != null)
                    output.close();
                if(clientSocket != null)
                    clientSocket.close();
                disconnect(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            String message;
            try {
                while(true) {
                    message = input.readLine();
                    if(message == null) {
                        close();
                        break;
                    }
                    readMessage(message);
                }
            } catch (IOException e) {
                System.out.print(e.getMessage());
            } finally {
                close();
            }
        }

    }

}