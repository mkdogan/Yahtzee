package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Çok clientlı Yahtzee oyunu için sunucu sınıfı
 * @author kasim
 */
public class Server implements Runnable {

    int clientId;
    ServerSocket ssocket;
    ArrayList<SClient> waitingClients; // Eşleşme bekleyen clientlar
    HashMap<Integer, GameRoom> gameRooms; // Oyun odaları - key: roomId
    int currentRoomId;
    
    public Server(int port) throws IOException {
        this.clientId = 0;
        this.ssocket = new ServerSocket(port);
        this.waitingClients = new ArrayList<>();
        this.gameRooms = new HashMap<>();
        this.currentRoomId = 0;
    }
    
    public void StartAcceptance(Server server) throws IOException {
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
    
    public void SendMessageToClient(SClient client, String msg) throws IOException {
        String rmsg = Message.GenerateMsg(Message.Type.MSGFROMSERVER, msg);
        client.SendMessage(rmsg.getBytes());
    }
    
    public void SendIdToClient(int id, String msg) throws IOException {
        for (SClient client : waitingClients) {
            if (client.id == id) {
                String rmsg = Message.GenerateMsg(Message.Type.CLIENTID, msg);
                client.SendMessage(rmsg.getBytes());
                return;
            }
        }
        
        // Waiting client listesinde bulunamadıysa oyun odalarında ara
        for (GameRoom room : gameRooms.values()) {
            for (SClient client : room.getClients()) {
                if (client.id == id) {
                    String rmsg = Message.GenerateMsg(Message.Type.CLIENTID, msg);
                    client.SendMessage(rmsg.getBytes());
                    return;
                }
            }
        }
    }
    
    /**
     * İki client eşleştiğinde oyun odası oluşturur
     */
    public void matchClients() {
        // Bekleyen client sayısı en az 2 ise eşleştirme yap
        if (waitingClients.size() >= 2) {
            // Listedeki ilk iki client'ı eşleştir
            SClient client1 = waitingClients.get(0);
            SClient client2 = waitingClients.get(1);
            
            // Oyun odası oluştur
            GameRoom newRoom = new GameRoom(currentRoomId, client1, client2);
            gameRooms.put(currentRoomId, newRoom);
            currentRoomId++;
            
            // Clientları bilgilendir
            try {
                SendMessageToClient(client1, Message.MsgContent.START.toString());
                SendMessageToClient(client2, Message.MsgContent.START.toString());
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            }
            
            // Eşleşen clientları waiting listesinden kaldır
            waitingClients.remove(client1);
            waitingClients.remove(client2);
        }
    }
    
    /**
     * Client'ın bulunduğu oyun odasını bulur
     * @param clientId Aranan client ID'si
     * @return GameRoom objesi veya null
     */
    public GameRoom findGameRoomByClientId(int clientId) {
        for (GameRoom room : gameRooms.values()) {
            for (SClient client : room.getClients()) {
                if (client.id == clientId) {
                    return room;
                }
            }
        }
        return null;
    }
    
    /**
     * Belirtilen client için oyun odasını kaldırır ve diğer clientları bilgilendirir
     * @param clientId Odadan çıkarılacak client ID'si
     * @throws IOException 
     */
    public void removeGameRoom(int clientId) throws IOException {
        GameRoom room = findGameRoomByClientId(clientId);
        if (room != null) {
            // Odadaki diğer client'a bilgi ver
            for (SClient client : room.getClients()) {
                if (client.id != clientId) {
                    SendMessageToClient(client, Message.MsgContent.OPPONENT_DISCONNECTED.toString());
                }
            }
            
            // Odayı kaldır
            gameRooms.remove(room.getRoomId());
        }
    }
    
    /**
     * Oyun odasındaki diğer client'ı bulur
     * @param roomId Oda ID'si
     * @param clientId Aranan client'ın ID'si
     * @return Diğer client objesi veya null
     */
    public SClient getOpponentInRoom(int roomId, int clientId) {
        GameRoom room = gameRooms.get(roomId);
        if (room != null) {
            for (SClient client : room.getClients()) {
                if (client.id != clientId) {
                    return client;
                }
            }
        }
        return null;
    }
    
    /**
     * Clientların bağlantısının koptuğu durumlarda client'ı temizler
     * @param client Temizlenecek client
     */
    public void removeDisconnectedClient(SClient client) {
        // Bekleme listesinden kaldır
        if (waitingClients.contains(client)) {
            waitingClients.remove(client);
            return;
        }
        
        // Oyun odasından kaldır
        try {
            removeGameRoom(client.id);
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void run() {
        try {
            while (!this.ssocket.isClosed()) {
                // Yeni client bağlantısı kabul et
                Socket csocket = this.ssocket.accept();
                SClient newClient = new SClient(csocket, this);
                newClient.StartListening(newClient);
                
                // Yeni client'a ID gönder
                String msg = Integer.toString(clientId);
                this.SendIdToClient(newClient.id, msg);
                
                // Bekleme listesine ekle
                waitingClients.add(newClient);
                
                // Client eşleştirme kontrolü yap
                matchClients();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        try {
            Server s1 = new Server(5999);
            s1.StartAcceptance(s1);

            while (true) {
                // Sunucu çalışıyor
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/**
 * Oyun odası sınıfı
 */
class GameRoom {
    private int roomId;
    private ArrayList<SClient> clients;
    private int currentTurnIndex;
    private final Object turnLock = new Object();
    private final Object startLock = new Object();
    
    public GameRoom(int roomId, SClient client1, SClient client2) {
        this.roomId = roomId;
        this.clients = new ArrayList<>();
        this.clients.add(client1);
        this.clients.add(client2);
        this.currentTurnIndex = -1;
        
        // Client'ların oda referansını güncelle
        client1.setGameRoom(this);
        client2.setGameRoom(this);
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public ArrayList<SClient> getClients() {
        return clients;
    }
    
    public void StartGame() throws IOException {
        synchronized (startLock) {
            if (AreAllClientsStarted()) {
                currentTurnIndex = RandomizeTurnIndex();
                sendTurnToCurrentPlayer();
                for (SClient client : clients) {
                    client.isStarted = false;
                }
            }
        }
    }
    
    private int RandomizeTurnIndex() {
        Random rnd = new Random();
        return rnd.nextInt(clients.size()); // 0 veya 1
    }
    
    public void nextTurn() throws IOException {
        currentTurnIndex = (currentTurnIndex + 1) % 2; // 0 veya 1
        sendTurnToCurrentPlayer();
    }
    
    private void sendTurnToCurrentPlayer() throws IOException {
        SClient currentClient = clients.get(currentTurnIndex);
        Server server = currentClient.ownerServer;
        server.SendMessageToClient(currentClient, Message.MsgContent.TURN.toString());
        currentClient.isUpdated = false;
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
    
    public boolean areAllClientsRequestsReplay() {
        if (clients.size() < 2) {
            return false;
        }

        for (SClient client : clients) {
            if (!client.wantsReplay) {
                return false;
            }
        }
        return true;
    }
    
    public void resetForNewGame() {
        for (SClient client : clients) {
            client.isReady = false;
            client.isStarted = false;
            client.isUpdated = false;
            client.isFinished = false;
            client.wantsReplay = false;
        }
        currentTurnIndex = -1;
    }
}
