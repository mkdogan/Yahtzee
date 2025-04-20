/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Application.StartFrm;
import Server.Message;
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
public class CClient implements Runnable{
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    int id;

    public CClient(String ip, int port) throws IOException {
        this.csocket = new Socket(ip, port);
        this.coutput = csocket.getOutputStream();
        this.cinput = csocket.getInputStream();
    }

    public void MsgParser(String msg) {
        try {

            String tokens[] = msg.split("#");

            Message.Type mt = Message.Type.valueOf(tokens[0]);
            switch (mt) {
                case CLIENTID:
                    String clientId = tokens[1];
                    this.id = Integer.parseInt(clientId.trim());
                    System.out.println("client id: " + clientId);
                    break;

                case MSGFROMCLIENT:
                    

                    break;
                default:
                    throw new AssertionError();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void SendMessage(String msg) throws IOException {

        this.coutput.write(msg.getBytes());
    }

    public void Listen(CClient client) throws IOException {
        
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
            Logger.getLogger(CClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
