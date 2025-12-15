package network_game;

import java.io.*;
import java.net.*;
import java.util.*;

public class RoomServer {

    private static final int PORT = 5001;

    // ================== 글로벌 상태 ==================
    private static final Set<String> usedNames =
            Collections.synchronizedSet(new HashSet<>());
    private static final List<ClientHandler> allHandlers =
            Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, RoomInfo> rooms =
            Collections.synchronizedMap(new LinkedHashMap<>());

    // ================== 비속어 ==================
    private static final List<String> BAD_WORDS = new ArrayList<>();
    private static final int MAX_WARNING = 3;
    private static final long MUTE_TIME = 30_000;

    // ================== main ==================
    public static void main(String[] args) {
        loadBadWords();
        System.out.println("RoomServer 시작 — 포트 " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket sock = serverSocket.accept();
                ClientHandler handler = new ClientHandler(sock);
                allHandlers.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================== 비속어 로딩 ==================
    private static void loadBadWords() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        RoomServer.class.getClassLoader()
                                .getResourceAsStream("badwords.txt")
                )
        )) {
            String line;
            while ((line = br.readLine()) != null)
                BAD_WORDS.add(line.trim().toLowerCase());
        } catch (Exception ignored) {}
    }

    // ======================= RoomInfo =========================
    static class RoomInfo {
        final String name;
        final List<ClientHandler> users =
                Collections.synchronizedList(new ArrayList<>());

        boolean gameStarted = false;
        final Object gameLock = new Object();

        GameState game;

        RoomInfo(String name) {
            this.name = name;
        }

        boolean isFull() {
            return users.size() >= 4;
        }
    }

    // ==================== ClientHandler ======================
    static class ClientHandler extends Thread {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        private String name;
        private String joinedRoom;
        private String team;

        private int badCount = 0;
        private long muteUntil = 0;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("[CONNECT] " + socket);
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("ENTER_NAME");
                name = in.readLine();

                synchronized (usedNames) {
                    if (name == null || name.isBlank() || usedNames.contains(name)) {
                        out.println("MSG 이미 사용 중인 닉네임");
                        return;
                    }
                    usedNames.add(name);
                }

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (line.startsWith("ENTER_ROOM "))
                        handleEnterRoom(line.substring(11));
                    else if (joinedRoom == null) {
                        if (line.equals("GET_ROOMS")) sendRoomList();
                        else if (line.startsWith("CREATE ")) createRoom(line.substring(7));
                    }
                    else {
                        if (line.startsWith("PLAY "))
                            handlePlay(line.substring(5));
                        else if (line.startsWith("ALL "))
                            handleChat(line.substring(4), false);
                        else if (line.startsWith("TEAM "))
                            handleChat(line.substring(5), true);
                    }
                }
            } catch (IOException ignored) {}
            finally { cleanup(); }
        }

        // ================== 로비 ==================
        private void sendRoomList() {
            synchronized (rooms) {
                for (String rn : rooms.keySet())
                    out.println("ROOM " + rn);
            }
            out.println("ROOM_END");
        }

        private void createRoom(String roomName) {
            synchronized (rooms) {
                if (rooms.containsKey(roomName)) return;
                rooms.put(roomName, new RoomInfo(roomName));
            }
            broadcastRoomListToLobbyClients();
        }

        private void broadcastRoomListToLobbyClients() {
            synchronized (allHandlers) {
                for (ClientHandler ch : allHandlers)
                    if (ch.joinedRoom == null)
                        ch.sendRoomList();
            }
        }

        // ================== 룸 ==================
        private void handleEnterRoom(String roomName) {
            RoomInfo r;
            synchronized (rooms) {
                r = rooms.get(roomName);
                if (r == null || r.isFull()) return;

                team = (r.users.size() % 2 == 0) ? "A" : "B";
                joinedRoom = roomName;
                r.users.add(this);
            }

            broadcast(r, "MSG [SYSTEM] " + name + " 입장 (" + team + ")");

            if (r.users.size() == 4)
                startGame(r);
        }

        private void startGame(RoomInfo r) {
            synchronized (r.gameLock) {
                if (r.gameStarted) return;
                r.gameStarted = true;

                List<String> players = new ArrayList<>();
                for (ClientHandler u : r.users)
                    players.add(u.name);

                r.game = new GameState(players);

                for (ClientHandler u : r.users)
                    u.out.println("HAND " + r.game.getHandString(u.name));

                broadcast(r, "GAME_START");
                broadcastCenter(r);
            }
        }

        // ================== 카드 ==================
        private void handlePlay(String cardStr) {
            RoomInfo r = rooms.get(joinedRoom);
            if (r == null) return;

            synchronized (r.gameLock) {
                boolean ok = r.game.playCard(name, Card.fromString(cardStr));
                if (!ok) return;

                broadcast(r, "PLAY_OK " + name + " " + cardStr);
                broadcastCenter(r);

                if (r.game.isFinished())
                    broadcast(r, "GAME_END " + r.game.getWinner());
            }
        }

        // ================== 채팅 ==================
        private void handleChat(String msg, boolean teamOnly) {
            RoomInfo r = rooms.get(joinedRoom);
            if (r == null) return;

            String outMsg = "MSG [" + name + "][" + team + "] " + msg;
            if (teamOnly) broadcastTeam(r, team, outMsg);
            else broadcast(r, outMsg);
        }

        // ================== 유틸 ==================
        private void broadcast(RoomInfo r, String msg) {
            synchronized (r.users) {
                for (ClientHandler u : r.users)
                    u.out.println(msg);
            }
        }

        private void broadcastTeam(RoomInfo r, String team, String msg) {
            synchronized (r.users) {
                for (ClientHandler u : r.users)
                    if (team.equals(u.team))
                        u.out.println(msg);
            }
        }

        private void broadcastCenter(RoomInfo r) {
            Card c = r.game.getCenterTop();
            broadcast(r, "CENTER " + (c == null ? "NONE" : c));
        }

        private void cleanup() {
            try { socket.close(); } catch (Exception ignored) {}
            allHandlers.remove(this);
            usedNames.remove(name);
            if (joinedRoom != null) {
                RoomInfo r = rooms.get(joinedRoom);
                if (r != null) r.users.remove(this);
            }
        }
    }
}
