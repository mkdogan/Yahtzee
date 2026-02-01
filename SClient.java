package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Çok clientlı Yahtzee oyunu için client işlemlerini yöneten sınıf
 * @author kasim
 */
public class SClient implements Runnable {

    int id;
    Socket csocket;
    OutputStream coutput;
    InputStream cinput;
    BufferedReader breader;
    Server ownerServer;
    GameRoom gameRoom;
    boolean isReady = false;
    boolean isStarted = false;
    boolean isUpdated = false;
    boolean isFinished = false;
    boolean wantsReplay = false;
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

    public void MsgParser(String msg) throws IOException {
        String tokens[] = msg.split("#");
        Message.Type mt = Message.Type.valueOf(tokens[0].trim());
        switch (mt) {
            case CLIENTID:
                // Client ID işlemleri
                break;

            case MSGFROMCLIENT:
                Message.MsgContent mc = Message.MsgContent.valueOf(tokens[1].trim());
                switch (mc) {
                    case READY:
                        this.isReady = true;
                        if (this.gameRoom != null) {
                            this.broadcastStartMessage();
                        }
                        break;
                    case STARTED:
                        this.isStarted = true;
                        if (this.gameRoom != null) {
                            this.gameRoom.StartGame();
                        }
                        break;
                    case SCORE:
                        // Sonuç skorunu diğer client'a gönder
                        sendScoreMessage(tokens[2], tokens[3]);
                        byte filledCount = Byte.parseByte(tokens[4]);
                        isClientFinished(filledCount);
                        checkGameOverMessage();
                        break;
                    case UPDATED:
                        // UPDATED mesajını işle
                        if (this.gameRoom != null) {
                            this.gameRoom.nextTurn();
                        }
                        break;
                    case GAME_OVER:
                        // Oyun bitirme mesajı yayınla
                        broadcastFinishMessage();
                        break;
                    case REPLAY:
                        this.wantsReplay = true;
                        broadcastReplayMessage();
                        break;
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public void StartListening(SClient client) {
        Thread clientThread = new Thread(client);
        clientThread.start();
    }

    @Override
    public void run() {
        try {
            while (!this.csocket.isClosed()) {
                String rmsg;
                while ((rmsg = breader.readLine()) != null) {
                    MsgParser(rmsg);
                }
            }
        } catch (IOException ex) {
            System.out.println("SClient run() exception: " + ex.getMessage());
            
            // Client bağlantısı kesildiğinde temizlik işlemleri
            ownerServer.removeDisconnectedClient(this);
            
            try {
                if (this.gameRoom != null) {
                    // Rakibe bilgilendirme mesajı gönder
                    for (SClient client : this.gameRoom.getClients()) {
                        if (client.id != this.id) {
                            ownerServer.SendMessageToClient(client, Message.MsgContent.OPPONENT_DISCONNECTED.toString());
                        }
                    }
                }
            } catch (IOException e) {
                Logger.getLogger(SClient.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void SendMessage(byte[] msg) throws IOException {
        this.coutput.write(msg);
    }

    /**
     * Tüm clientlar hazır olduğunda başlama mesajı yayınlar
     */
    public void broadcastStartMessage() throws IOException {
        synchronized (broadcastStartLock) {
            if (this.gameRoom != null && this.gameRoom.AreAllClientsReady()) {
                String msg = Message.MsgContent.START.toString();
                
                // Odadaki tüm clientlara mesaj gönder
                for (SClient client : this.gameRoom.getClients()) {
                    ownerServer.SendMessageToClient(client, msg);
                    client.isReady = false;
                }
            }
        }
    }

    /**
     * Skor mesajını diğer client'a gönderir
     */
    public void sendScoreMessage(String rowIndex, String score) throws IOException {
        if (this.gameRoom == null) return;
        
        String msg = Message.MsgContent.SCORE.toString();
        
        // Odadaki diğer client'a mesaj gönder
        for (SClient client : this.gameRoom.getClients()) {
            if (client.id != this.id) {
                msg += "#" + rowIndex + "#" + score;
                ownerServer.SendMessageToClient(client, msg);
            }
        }
    }

    private void isClientFinished(byte filledCount) throws IOException {
        this.isFinished = filledCount == 13;
    }

    private synchronized void checkGameOverMessage() throws IOException {
        if (this.gameRoom == null) return;
        
        boolean gameFinished = true;
        for (SClient client : this.gameRoom.getClients()) {
            if (!client.isFinished) {
                gameFinished = false;
                break;
            }
        }
        
        if (gameFinished) {
            broadcastGameOverMessage();
        }
    }

    private void broadcastGameOverMessage() throws IOException {
        if (this.gameRoom == null) return;
        
        String msg = Message.MsgContent.GAME_OVER.toString();
        
        // Odadaki tüm clientlara mesaj gönder
        for (SClient client : this.gameRoom.getClients()) {
            ownerServer.SendMessageToClient(client, msg);
        }
    }
    
    private void broadcastFinishMessage() throws IOException {
        if (this.gameRoom == null) return;
        
        String msg = Message.MsgContent.FINISH.toString();
        
        // Odadaki tüm clientlara mesaj gönder
        for (SClient client : this.gameRoom.getClients()) {
            ownerServer.SendMessageToClient(client, msg);
        }
        
        // Oyun odasını temizle
        int roomId = this.gameRoom.getRoomId();
        ownerServer.gameRooms.remove(roomId);
        
        // Clientları bekleme listesine geri koy
        for (SClient client : this.gameRoom.getClients()) {
            client.gameRoom = null;
            ownerServer.waitingClients.add(client);
        }
        
        // Yeni eşleşmeleri kontrol et
        ownerServer.matchClients();
    }
    
    private synchronized void broadcastReplayMessage() throws IOException {
        if (this.gameRoom == null) return;
        
        if (this.gameRoom.areAllClientsRequestsReplay()) {
            String msg = Message.MsgContent.REPLAY.toString();
            
            // Odadaki tüm clientlara mesaj gönder
            for (SClient client : this.gameRoom.getClients()) {
                ownerServer.SendMessageToClient(client, msg);
                client.wantsReplay = false;
            }
            
            // Oyun odasını yeni oyun için sıfırla
            this.gameRoom.resetForNewGame();
        }
    }
}
