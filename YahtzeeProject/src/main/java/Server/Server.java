/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kasim
 */
public class Server implements Runnable {

    int clientId;
    ServerSocket ssocket;
    ArrayList<SClient> clients;
    int currentTurnIndex = -1;
    private final Object turnLock = new Object();
    private final Object startLock = new Object();

    public Server(int port) throws IOException {
        this.clientId = 0;
        this.ssocket = new ServerSocket(port);
        this.clients = new ArrayList<>();
    }

    public void StartAcceptance(Server server) throws IOException {
        //Socket csocket = this.ssocket.accept();//blocking
        Thread serverThread = new Thread(server);
        serverThread.start();

    }

//    public void SendConnectedClientIdsToAll() throws IOException {
//        String data = "";
//        for (SClient client : clients) {
//            data += client.id + ",";
//        }
//
//        String msg = Message.GenerateMsg(Message.Type.CLIENTID, data);
//        this.SendBroadcastMsg(msg.getBytes());
//    }
    public void SendMessageToClient(SClient client, String msg) throws IOException { //
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMSERVER, msg);
        client.SendMessage(rmsg.getBytes());

    }

    public void SendIdToClient(int id, String msg) throws IOException {
        for (SClient client : clients) {
            if (client.id == id) {
                String rmsg = Message.GenerateMsg(Message.Type.CLIENTID, msg);
                client.SendMessage(rmsg.getBytes());
                break;
            }
        }
    }

    public void SendBroadcastMsg(String msg) throws IOException {
        for (SClient client : clients) {
            SendMessageToClient(client, msg);
        }
    }

    public boolean AreAllClientsReady() {

        if (clients.size() < 2) {
            return false;
        }

        for (SClient client : clients) {
            if (!client.isReady) {
                return false;
            }
        }
        return true;
    }

    public boolean AreAllClientsStarted() {

        if (clients.size() < 2) {
            return false;
        }

        for (SClient client : clients) {
            if (!client.isStarted) {
                return false;
            }
        }
        return true;
    }

    public boolean AreAllClientsUpdated() {

        if (clients.size() < 2) {
            return false;
        }

        for (SClient client : clients) {
            if (!client.isUpdated) {
                return false;
            }
        }
        return true;
    }

    /**
     * It checks whether it has received the 'STARTED' message from all clients.
     * If it has, starts the game. The method is synchronized
     *
     * @throws IOException
     */
    public void StartGame() throws IOException {
        synchronized (startLock) {

            if (AreAllClientsStarted()) {
                //System.out.println("Entered start game");
                currentTurnIndex = RandomizeTurnIndex();
                sendTurnToCurrentPlayer();

            }
        }
    }

    private int RandomizeTurnIndex() {
        Random rnd = new Random();
        return rnd.nextInt(clients.size()); // 0 or 1
    }

    public void nextTurn() throws IOException {
        currentTurnIndex = (currentTurnIndex + 1) % 2; // 0 or 1
        sendTurnToCurrentPlayer();
        //System.out.println("(Server) next turn executed");
    }

    private void sendTurnToCurrentPlayer() throws IOException {
        SClient currentClient = clients.get(currentTurnIndex);
        SendMessageToClient(currentClient, Message.MsgContent.TURN.toString());
        currentClient.isUpdated = false;

    }

    @Override
    public void run() {

        try {
            while (!this.ssocket.isClosed()) {
                if (clients.size() < 2) {

                    Socket csocket = this.ssocket.accept();
                    SClient newClient = new SClient(csocket, this);     // set clients id field and increment clientId field in server
                    newClient.StartListening(newClient);
                    this.clients.add(newClient);                       // add client to list
                    String msg = Integer.toString(clientId);
                    this.SendIdToClient(newClient.id, msg);       // send client its id

                } else {
                    //ignore the connection request
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(Server.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        try {
            Server s1 = new Server(6000);
            s1.StartAcceptance(s1);

            while (true) {

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
