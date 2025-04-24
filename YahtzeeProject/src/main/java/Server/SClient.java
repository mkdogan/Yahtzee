/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author kasim
 */
public class SClient implements Runnable {

    int id;
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    BufferedReader breader;
    Server ownerServer;
    boolean isReady = false;
    boolean isStarted = false;
    boolean isUpdated = false;
    private final Object broadcastStartLock = new Object();

    public SClient(Socket connectedSocket, Server server) throws IOException {
        this.csocket = connectedSocket;
        this.coutput = this.csocket.getOutputStream();
        this.cinput = this.csocket.getInputStream();
        this.ownerServer = server;
        this.id = server.clientId;
        breader = new BufferedReader(new InputStreamReader(cinput));
        server.clientId++;
    }

    public synchronized void MsgParser(String msg) throws IOException {
        String tokens[] = msg.split("#");
        Message.Type mt = Message.Type.valueOf(tokens[0].trim());
        switch (mt) {
            case CLIENTID:

                break;

            case MSGFROMCLIENT:
                Message.MsgContent mc = Message.MsgContent.valueOf(tokens[1].trim());
                switch (mc) {
                    case READY:
                        this.isReady = true;
                        this.broadcastStartMessage();
                        break;

                    case STARTED:
                        this.isStarted = true;
                        ownerServer.StartGame();
                        break;

                    case SCORE:
                        // If valid, make calculations. Then broadcast resulting SCORE message
                        this.broadcastScoreMessage();
                        break;
                    case UPDATED:
                        // Handle UPDATED message (Comes 2 UPDATED message)
                        this.isUpdated = true;
                        ownerServer.nextTurn();
                        //System.out.println("(SClient) next turn executed");

                        break;
                }
                break;
            default:
                throw new AssertionError();
        }

    }

    public void StartListening(SClient client) {
        Thread clientThread = new Thread(client);
        clientThread.start();
    }

    public void run() {
        try {
            while (!this.csocket.isClosed()) {

                String rmsg;
                while ((rmsg = breader.readLine()) != null) {
                    MsgParser(rmsg);
                }

            }
        } catch (IOException ex) {
            System.out.println("SClient run() exception");
            this.ownerServer.clients.remove(this);

        }

    }

    public void SendMessage(byte[] msg) throws IOException {

        this.coutput.write(msg);
    }

    /**
     * Broadcasts start message if all clients are ready
     */
    public void broadcastStartMessage() throws IOException {
        synchronized (broadcastStartLock) {

            if (ownerServer.AreAllClientsReady()) {
                //System.out.println("Entered broadcastStartMessage");
                String msg = Message.MsgContent.START.toString();
                ownerServer.SendBroadcastMsg(msg);
            }

        }
    }

    /**
     * Broadcasts score message for all clients
     *
     * @throws IOException
     */
    public void broadcastScoreMessage() throws IOException {
        //System.out.println("BroadcastScoreMessage");
        String msg = Message.MsgContent.SCORE.toString();
        ownerServer.SendBroadcastMsg(msg);
    }
}
