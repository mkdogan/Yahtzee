# 🎲 Networked Multiplayer Yahtzee Game (Java)

## 📑 Table of Contents
1. [Project Overview](#1-project-overview)
2. [System Architecture](#2-system-architecture)  
   - [Client-Server Model](#21-client-server-model)  
   - [Key Components](#22-key-components)  
3. [Implementation Details](#3-implementation-details)  
   - [Server Implementation](#31-server-implementation)  
     - [Server Class](#311-server-class)  
     - [GameRoom Class](#312-gameroom-class)  
     - [SClient Class](#313-sclient-class)  
   - [Client Implementation](#32-client-implementation)  
     - [CClient Class](#321-cclient-class)  
     - [GameFrm Class](#322-gamefrm-class)  
     - [StartFrm Class](#323-startfrm-class)  
4. [Communication Protocol](#4-communication-protocol)  
5. [Game Flow](#5-game-flow)  
6. [Server-Client Sequence Diagram](#6-server-client-sequence-diagram)  
7. [Technical Challenges and Solutions](#7-technical-challenges-and-solutions)  
   - [Synchronization](#71-synchronization)  
   - [Disconnection Handling](#72-disconnection-handling)  
   - [Score Calculation](#73-score-calculation)  
   - [User Interface](#74-user-interface)  
8. [Conclusion](#8-conclusion)  

---

## 1. Project Overview
This project is a **networked multiplayer Yahtzee game** developed in **Java** using a **Client-Server architecture**.  
Two players compete in real-time over a network connection following standard Yahtzee rules: roll dice, choose scoring categories, and aim for the highest score.

---

## 2. System Architecture

### 2.1 Client-Server Model
- **Server**: Manages connections, matchmaking, game rooms, and core game logic.
- **Client**: Handles UI, game interaction, and communication with the server.

### 2.2 Key Components

#### Server Components
- `Server.java` → Handles connections, matchmaking.
- `SClient.java` → Server representation of connected clients.
- `GameRoom.java` → Manages game state between two players.
- `Message.java` → Defines communication protocol.

#### Client Components
- `CClient.java` → Handles communication with the server.
- `StartFrm.java` → Initial UI for connecting to the server.
- `GameFrm.java` → Main game interface with dice, scoring, and game state.

---

## 3. Implementation Details

### 3.1 Server Implementation

#### 3.1.1 Server Class
- Entry point for client connections.
- Manages waiting clients & active game rooms.
- Handles matchmaking, message routing, and disconnection cleanup.

#### 3.1.2 GameRoom Class
- Represents a game session.
- Tracks players, turn order, and state.
- Synchronizes state between players.
- Handles start, game over, and replay.

#### 3.1.3 SClient Class
- Represents a client on the server side.
- Processes messages.
- Tracks player status.
- Handles disconnections.

---

### 3.2 Client Implementation

#### 3.2.1 CClient Class
- Manages TCP connection with server.
- Sends/receives messages.
- Updates game state locally.

#### 3.2.2 GameFrm Class
- Displays dice & scoring options.
- Manages gameplay flow.
- Shows turns, score preview, and results.

#### 3.2.3 StartFrm Class
- Allows server connection.
- Displays matchmaking status.
- Transitions to game UI.

---

## 4. Communication Protocol
Custom protocol defined in `Message.java`:
Uses **TCP sockets** for reliable message delivery.

---

## 5. Game Flow

1. **Connection & Matchmaking**
   - Client connects → Assigned ID → Waits for opponent.
2. **Game Initialization**
   - Server sends `START` → Random first player → Sends `TURN`.
3. **Gameplay Loop**
   - Player rolls dice (max 3 times) → Selects category.
   - Server updates scores & opponent view.
4. **Game Completion**
   - All categories filled → Compare scores → Show winner.
5. **Disconnection Handling**
   - Opponent notified → Cleanup → Back to start screen.

---

## 6. Server-Client Sequence Diagram
<img width="673" height="669" alt="image" src="https://github.com/user-attachments/assets/6fbcae74-b6d3-4d40-aede-fe6ee025c2bc" />


---

## 7. Technical Challenges and Solutions

### 7.1 Synchronization
- **Challenge**: Prevent race conditions.
- **Solution**: Locking, synchronized methods, client state flags.

### 7.2 Disconnection Handling
- **Challenge**: Handle mid-game disconnects.
- **Solution**: Exception handling, cleanup, opponent notification.

### 7.3 Score Calculation
- **Challenge**: Implement full Yahtzee scoring.
- **Solution**: Comprehensive system for all scoring categories.

### 7.4 User Interface
- **Challenge**: Make gameplay intuitive.
- **Solution**: Custom dice visualization, dynamic score preview.

---

## 8. Conclusion
This project demonstrates:
- **Java networking** with TCP sockets
- **Multithreading** for concurrent games
- **Event-driven GUI** using Swing
- **Robust client-server architecture**

---

## 🚀 How to Run
Write your own server ip or localhost and assign a port number in StartFrm class for client. Determine the port number for server in Server class.
