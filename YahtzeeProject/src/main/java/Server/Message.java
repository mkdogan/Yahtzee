/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author kasim
 */
public class Message {

    public enum Type {
        NONE,
        CLIENTID,
        MSGFROMCLIENT,
        MSGFROMSERVER,
        TOCLIENT;
    }
    
    public enum MsgContent{
        READY,
        START,
        STARTED,
        TURN,
        SCORE,
        UPDATED,
        GAME_OVER,
        REPLAY,
        FINISH,
        OPPONENT_DISCONNECTED
    }

    public static String GenerateMsg(Message.Type type, String data) {
        //mtype#d1,d2,d3
        String gmsg = type.toString().trim() + "#" + data.trim() + "\n";
        return gmsg;
    }
    
    
    public static String GenerateMsg(Message.Type type, Object content) {
        //mtype#d1,d2,d3
        String gmsg = type.toString().trim() + "#" + content.toString().trim() + "\n";
        return gmsg;
    }
  

}
