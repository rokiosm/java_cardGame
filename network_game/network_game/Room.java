package network_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Room extends JFrame {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    private GamePanel gamePanel;
    private ChatPanel chatPanel;

    private Thread receiveThread;
    private volatile boolean running = true;

    public Room(String roomName, Socket socket) throws IOException {
        super("게임방 - " + roomName);
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        buildGUI();
        addCloseHandler();
        startReceiveThread();

        setVisible(true);
    }

    // GUI 구성
    private void buildGUI() {
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(new Color(60, 122, 65));
        setContentPane(bg);

        //gamePanel = new GamePanel(msg -> out.println(msg));
        //bg.add(gamePanel, BorderLayout.CENTER);

        chatPanel = new ChatPanel(this::sendChat);
        chatPanel.setPreferredSize(new Dimension(330, 600));
        bg.add(chatPanel, BorderLayout.EAST);
    }

    // 채팅 송신
    private void sendChat(String channel, String text) {
        if (text == null || text.trim().isEmpty()) return;
        out.println(("TEAM".equals(channel) ? "TEAM " : "ALL ") + text);
    }

    // 서버 수신 쓰레드
    private void startReceiveThread() {
        receiveThread = new Thread(this::receiveLoop, "Room-ReceiveThread");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void receiveLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                final String msg = line;
                SwingUtilities.invokeLater(() -> handleMessage(msg));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> chatPanel.addChatMessage("[SYSTEM] 서버 연결 끊김"));
        }
    }

    // ==========================
    // 메시지 처리
    // ==========================
    private void handleMessage(String line) {
        if (line.startsWith("MSG ")) {
            chatPanel.addChatMessage(line);
        } else if (line.startsWith("ALL ")) {
            chatPanel.addChatMessage(line.substring(4));
        } else if (line.startsWith("TEAM ")) {
            chatPanel.addChatMessage("[TEAM] " + line.substring(5));
        } else if (line.equals("GAME_START")) {
            chatPanel.addChatMessage("[SYSTEM] 게임 시작!");
        } else if (line.startsWith("HAND ")) {
            gamePanel.setHand(line.substring(5));
        } else if (line.startsWith("CENTER ")) {
            gamePanel.setCenter(line.substring(7));
        } else if (line.startsWith("PLAY_OK ")) {
            String[] p = line.split(" ");
            if (p.length >= 3) {
                //gamePanel.removeCard(p[2]);
            }
        } else if (line.startsWith("GAME_OVER ")) {
            JOptionPane.showMessageDialog(
                this,
                line.substring(10),
                "게임 종료",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else if (line.startsWith("GAME_END ")) {
            JOptionPane.showMessageDialog(
                this,
                line.substring(9),
                "게임 종료",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else if (line.startsWith("USERLIST ")) {
            System.out.println("[USERLIST RAW] " + line);
            //chatPanel.clearUsers();
            String[] users = line.substring(9).split(",");
            for (String u : users) {
                String[] p = u.split(":");
                String nickname = p[0];
                String badge = p.length > 2 ? p[2] : null;
                chatPanel.addUser(nickname, badge);
            }
        } else {
            //System.out.println("ROOM MSG: " + line);
        }
    }

    // 종료 처리
    private void cleanup() {
        if (!running) return;
        running = false;
        SwingUtilities.invokeLater(this::dispose);
    }

    private void addCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
            }
        });
    }
}
