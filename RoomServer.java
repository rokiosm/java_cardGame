package network_game.src;

import java.io.*;
import java.net.*;
import java.util.*;

public class RoomServer {

    private static final int PORT = 5001;

    /** 전체 접속 클라이언트 */
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    /** 방 목록 (roomName → RoomInfo) */
    private static final Map<String, RoomInfo> rooms = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Room Server 시작 - 포트: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        }
    }

    // RoomInfo
    static class RoomInfo {
        String name;
        List<ClientHandler> users = Collections.synchronizedList(new ArrayList<>());

        RoomInfo(String name) {
            this.name = name;
        }

        boolean isFull() {
            return users.size() >= 4;
        }
    }

    // ClientHandle
    static class ClientHandler extends Thread {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        private String name;
        private String team;
        private String roomName;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                setupStream();
                requestUserName();
                listenCommands();
            } catch (IOException ignored) {
            } finally {
                cleanup();
            }
        }

        // Stream Setup
        private void setupStream() throws IOException {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        private void requestUserName() throws IOException {
            out.println("ENTER_NAME");
            name = in.readLine();
        }

        // Command Loop
        private void listenCommands() throws IOException {
            String line;

            while ((line = in.readLine()) != null) {

                if (line.startsWith("CREATE ")) {
                    handleCreate(line.substring(7).trim());
                }

                else if (line.startsWith("JOIN ")) {
                    handleJoin(line.substring(5).trim());
                }

                else if (line.startsWith("ALL ")) {
                    handleAllChat(line.substring(4));
                }

                else if (line.startsWith("TEAM ")) {
                    handleTeamChat(line.substring(5));
                }
            }
        }

        // Room Creation
        private void handleCreate(String roomName) {
            this.roomName = roomName;

            RoomInfo room = new RoomInfo(roomName);
            room.users.add(this);
            rooms.put(roomName, room);

            broadcastRoomList();
        }

        // Join Room
        private void handleJoin(String roomName) {
            RoomInfo room = rooms.get(roomName);

            if (room == null) {
                out.println("MSG 존재하지 않는 방입니다.");
                return;
            }
            if (room.isFull()) {
                out.println("MSG 방이 가득 찼습니다!");
                return;
            }

            this.roomName = roomName;

            // A/B 팀 자동 배정
            this.team = (room.users.size() % 2 == 0) ? "A" : "B";
            room.users.add(this);

            sendRoomUsers(room);
            broadcastRoomMessage(room, "[" + name + "] 입장하였습니다.");
        }

        // Chat — 전체
        private void handleAllChat(String msg) {
            RoomInfo room = rooms.get(roomName);
            if (room == null) return;

            broadcastRoomMessage(room, "[" + name + "] : " + msg);
        }

        // Chat — 팀
        private void handleTeamChat(String msg) {
            RoomInfo room = rooms.get(roomName);
            if (room == null) return;

            broadcastTeam(room, team, "[" + name + "][" + team + "] : " + msg);
        }

        // Broadcast Helpers
        private void broadcastRoomList() {
            synchronized (clients) {
                for (ClientHandler c : clients) {
                    for (String r : rooms.keySet()) {
                        c.out.println("ROOM " + r);
                    }
                    c.out.println("ROOM_END");
                }
            }
        }

        private void sendRoomUsers(RoomInfo room) {
            StringBuilder sb = new StringBuilder();

            synchronized (room.users) {
                for (ClientHandler u : room.users) {
                    sb.append(u.name).append(":").append(u.team).append(",");
                }

                for (ClientHandler u : room.users) {
                    u.out.println("USERLIST " + sb);
                }
            }
        }

        private void broadcastRoomMessage(RoomInfo room, String msg) {
            synchronized (room.users) {
                for (ClientHandler u : room.users) {
                    u.out.println("MSG " + msg);
                }
            }
        }

        private void broadcastTeam(RoomInfo room, String team, String msg) {
            synchronized (room.users) {
                for (ClientHandler u : room.users) {
                    if (team.equals(u.team)) {
                        u.out.println("MSG " + msg);
                    }
                }
            }
        }

        // Cleanup
        private void cleanup() {
            try {
                if (roomName != null && rooms.containsKey(roomName)) {
                    RoomInfo room = rooms.get(roomName);

                    room.users.remove(this);

                    if (room.users.isEmpty()) {
                        rooms.remove(roomName);
                    } else {
                        sendRoomUsers(room);
                    }
                }

                clients.remove(this);

                socket.close();
            } catch (Exception ignored) {}
        }
    }
}
