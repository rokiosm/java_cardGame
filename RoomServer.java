package network_game.src;

import java.io.*;
import java.net.*;
import java.util.*;

public class RoomServer {

    private static final int PORT = 5001;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Map<String, RoomInfo> rooms = new HashMap<>();

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
            e.printStackTrace();
        }
    }

    static class RoomInfo {
        String name;
        List<ClientHandler> users = new ArrayList<>();
        RoomInfo(String name) { this.name = name; }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private String roomName;
        private String team;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("ENTER_NAME");
                name = in.readLine();

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("CREATE ")) {
                        roomName = line.substring(7);
                        RoomInfo r = new RoomInfo(roomName);
                        r.users.add(this);
                        rooms.put(roomName, r);
                        broadcastRooms();
                    } else if (line.startsWith("JOIN ")) {
                        roomName = line.substring(5);
                        RoomInfo r = rooms.get(roomName);
                        if (r != null && r.users.size() < 4) {
                            team = r.users.size() % 2 == 0 ? "A" : "B";
                            r.users.add(this);
                            sendRoomUsers(r);
                        } else {
                            out.println("MSG 방이 가득 찼습니다!");
                        }
                    } else if (line.startsWith("ALL ")) {
                        RoomInfo r = rooms.get(roomName);
                        if (r != null) broadcastRoom(r, name + ": " + line.substring(4));
                    } else if (line.startsWith("TEAM ")) {
                        RoomInfo r = rooms.get(roomName);
                        if (r != null) broadcastTeam(r, team, name + "(팀): " + line.substring(5));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                leaveRoom();
            }
        }

        private void broadcastRooms() {
            for (ClientHandler c : clients) {
                try {
                    for (String roomName : rooms.keySet()) {
                        c.out.println("ROOM " + roomName);
                    }
                    c.out.println("ROOM_END");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        private void sendRoomUsers(RoomInfo r) {
            StringBuilder sb = new StringBuilder();
            for (ClientHandler u : r.users) {
                sb.append(u.name).append(":").append(u.team).append(",");
            }
            for (ClientHandler u : r.users) {
                u.out.println("USERLIST " + sb.toString());
                broadcastRoom(r, "[" + u.name + "] 입장하였습니다.");
            }
        }

        private void broadcastRoom(RoomInfo r, String msg) {
            for (ClientHandler u : r.users) u.out.println("MSG " + msg);
        }

        private void broadcastTeam(RoomInfo r, String team, String msg) {
            for (ClientHandler u : r.users) {
                if (u.team.equals(team)) u.out.println("MSG " + msg);
            }
        }

        private void leaveRoom() {
            if (roomName != null && rooms.containsKey(roomName)) {
                RoomInfo r = rooms.get(roomName);
                r.users.remove(this);
                if (r.users.isEmpty()) rooms.remove(roomName);
                else sendRoomUsers(r);
            }
            clients.remove(this);
        }
    }
}
