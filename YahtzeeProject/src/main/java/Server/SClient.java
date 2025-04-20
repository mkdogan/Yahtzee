/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kasim
 */
public class SClient implements Runnable{
    
    int id;
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    Server ownerServer;

    public SClient(Socket connectedSocket, Server server) throws IOException {
        this.csocket = connectedSocket;
        this.coutput = this.csocket.getOutputStream();
        this.cinput = this.csocket.getInputStream();
        this.ownerServer = server;
        this.id = server.clientId;
        server.clientId++;
    }

    public void MsgParser(String msg) throws IOException {
        String tokens[] = msg.split("#");
        Message.Type mt = Message.Type.valueOf(tokens[0]);
        switch (mt) {
            case CLIENTID:
                
                break;
            case TOCLIENT:
                String datas[]= tokens[1].split(",");
                int id= Integer.parseInt(datas[0]);
                this.ownerServer.SendMessageToClient(id, datas[1]);
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
                int bsize = this.cinput.read();
                byte buffer[] = new byte[bsize];
                this.cinput.read(buffer);
                String rsMsg = new String(buffer);
                this.MsgParser(rsMsg);
            }
        } catch (IOException ex) {
            this.ownerServer.clients.remove(this);
           
        }

    }

    public void SendMessage(byte[] msg) throws IOException {

        this.coutput.write(msg);
    }
}
