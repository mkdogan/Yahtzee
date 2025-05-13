/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Server.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import Application.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.table.TableModel;

/**
 *
 * @author kasim
 */
public class CClient implements Runnable {

    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    public StartFrm startFrm;
    public GameFrm gameFrm;
    BufferedReader breader;
    private int id;
    private TableModel scoreTableModel;

    public CClient(String ip, int port) throws IOException {
        this.csocket = new Socket(ip, port);
        this.coutput = csocket.getOutputStream();
        this.cinput = csocket.getInputStream();
        breader = new BufferedReader(new InputStreamReader(cinput));
    }

    public void MsgParser(String msg) {
        try {

            String tokens[] = msg.split("#");

            Message.Type mt = Message.Type.valueOf(tokens[0].trim());

            switch (mt) {
                case CLIENTID:
                    String clientId = tokens[1];
                    this.id = Integer.parseInt(clientId.trim());
                    break;

                case MSGFROMSERVER:
                    Message.MsgContent mc = Message.MsgContent.valueOf(tokens[1].trim());
                    switch (mc) {
                        case START:
                            this.openGameFrm();
                            scoreTableModel = gameFrm.getScoreTableModel();
                            this.sendStartedMessage();
                            break;
                        case TURN:
                            // Handle Turn message
                            gameFrm.setTurn(true);
                            break;
                        case SCORE:
                            // Update UI
                            int rowIndex = Integer.parseInt(tokens[2]);
                            int score = Integer.parseInt(tokens[3]);
                            scoreTableModel.setValueAt(score, rowIndex, 2);
                            updateTotalScores();
                            // send update message
                            this.sendUpdatedMessage();
                            break;
                        default:
                            throw new AssertionError();
                    }
                    break;

                default:
                    throw new AssertionError();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void SendMessage(String msg) throws IOException {
        this.coutput.write(msg.getBytes());
    }

    public void Listen(CClient client) throws IOException {

        Thread clientThread = new Thread(client);
        clientThread.start();
    }

    public int getId() {
        return this.id;
    }

    private void openGameFrm() {
        if (startFrm != null) {
            startFrm.dispose();
        }
        gameFrm = new GameFrm(this);
        gameFrm.setVisible(true);
    }

    public void sendStartedMessage() throws IOException {
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMCLIENT, Message.MsgContent.STARTED);
        this.SendMessage(rmsg);
    }

    public void sendReadyMessage() throws IOException {
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMCLIENT, Message.MsgContent.READY);
        this.SendMessage(rmsg);
    }

    public void sendScoreMessage(int rowIndex, int score) throws IOException {
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMCLIENT, Message.MsgContent.SCORE);
        rmsg = rmsg.trim();
        rmsg += "#" + rowIndex + "#" + score + "\n";
        this.SendMessage(rmsg);
    }

    public void sendUpdatedMessage() throws IOException {
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMCLIENT, Message.MsgContent.UPDATED);
        this.SendMessage(rmsg);
    }
    
    /**
     * Update total scores in the score table
     */
    private void updateTotalScores() {
        // Upper section subtotal (1-6)
        int upperSectionSum = 0;
        for (int i = 0; i < 6; i++) {
            Object value = scoreTableModel.getValueAt(i, 2);
            // if cell is not null and not hint
            if (value != null) {
                upperSectionSum += (Integer) value;
            }
        }
        scoreTableModel.setValueAt(upperSectionSum, 6, 2); // write upperSectionSum to opponents Subtotal cell

        // Bonus check (35 points if upper section is 63 or more)
        int bonus = (upperSectionSum >= 63) ? 35 : 0;
        scoreTableModel.setValueAt(bonus, 7, 2);

        // Lower section total
        int lowerSectionSum = 0;
        for (int i = 8; i < 15; i++) {
            Object value = scoreTableModel.getValueAt(i, 2);
            if (value != null) {
                lowerSectionSum += (Integer) value;
            }
        }

        // Total score
        int totalScore = upperSectionSum + bonus + lowerSectionSum;
        scoreTableModel.setValueAt(totalScore, 15, 2);
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
            Logger.getLogger(CClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
