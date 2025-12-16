package network_game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Lobby extends JFrame {

    private JTextField roomTitleInput;
    private JPanel roomListPanel;
    
    private String badge;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final ArrayList<String> rooms = new ArrayList<>();
    private final String userName;

    private Thread receiveThread;
    private volatile boolean enteringRoom = false;
    
    private String pendingRoomName;

    public Lobby(String userName, String badge) {
        super("방 로비 - " + userName);
        this.userName = userName;
        this.badge = badge;

        connectServer();
        buildGUI();
        startReceiveThread();

        sendMessage("GET_ROOMS");
        setVisible(true);
    }

    // ================= 서버 연결 =================
    private void connectServer() {
        try {
            socket = new Socket("127.0.0.1", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 1️⃣ 서버가 ENTER_NAME 보낼 때까지 대기
            String req = in.readLine();
            System.out.println("서버 첫 메시지: " + req);

            if (!"ENTER_NAME".equals(req)) {
                throw new IOException("Invalid handshake: " + req);
            }

            // 2️⃣ 닉네임 + 배지 단 한 번만 전송
            String payload = userName + "|" + badge;
            System.out.println("닉네임 전송: " + payload);
            out.println(payload);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage());
            cleanup();
        }
    }

    // ================= UI =================
    private void buildGUI() {
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JPanel bg = new JPanel(null);
        bg.setBounds(0, 0, 800, 500);
        bg.setBackground(new Color(60, 122, 65));
        add(bg);

        bg.add(createCreateRoomPanel());
        bg.add(createRoomListPanel());
    }

    private JPanel createCreateRoomPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(40, 60, 240, 160);
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("방 만들기", SwingConstants.CENTER);
        title.setBounds(0, 10, 240, 25);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        panel.add(title);

        JLabel rt = new JLabel("방 제목 :");
        rt.setBounds(20, 55, 80, 20);
        panel.add(rt);

        roomTitleInput = new JTextField();
        roomTitleInput.setBounds(80, 55, 130, 22);
        panel.add(roomTitleInput);

        JButton createBtn = new JButton("만들기");
        createBtn.setBounds(70, 100, 100, 30);
        createBtn.addActionListener(e -> createRoom());
        panel.add(createBtn);

        return panel;
    }

    private JScrollPane createRoomListPanel() {
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(new Color(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(roomListPanel);
        scrollPane.setBounds(330, 40, 430, 400);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    // ================= 로비 동작 =================
    private void createRoom() {
        String title = roomTitleInput.getText().trim();
        if (!title.isEmpty()) {
            sendMessage("CREATE " + title);
            roomTitleInput.setText("");
        }
    }

    private void requestJoinRoom(String roomName) {
        if (enteringRoom) return;

        enteringRoom = true;
        pendingRoomName = roomName;
        sendMessage("ENTER_ROOM " + roomName);

        // 🔴 중요: Lobby 수신 스레드 종료
        if (receiveThread != null) {
            receiveThread.interrupt();
        }

        SwingUtilities.invokeLater(() -> {
            dispose();
            try {
                new Room(pendingRoomName, socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    // ================= 수신 스레드 =================
    private void startReceiveThread() {
        receiveThread = new Thread(this::receiveLoop, "Lobby-Receive");
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void receiveLoop() {
        try {
            String msg;
            while (!enteringRoom && (msg = in.readLine()) != null) {
                System.out.println("서버 수신: " + msg);
                if (msg.startsWith("ROOM ")) {
                    String roomName = msg.substring(5);
                    if (!rooms.contains(roomName)) rooms.add(roomName);
                    updateRoomList();
                } else if (msg.equals("ROOM_END")) {
                    // ignore
                } else if (msg.startsWith("MSG 이미 사용 중인 닉네임")) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "이미 사용 중인 닉네임입니다.\n프로그램을 다시 실행하세요.",
                                "닉네임 중복",
                                JOptionPane.ERROR_MESSAGE
                        );
                        cleanup();
                    });
                    return;
                } else if (msg.startsWith("MSG 방 입장 실패") || msg.startsWith("MSG 이미 방에 입장")) {
                    enteringRoom = false;
                } else if (msg.startsWith("MSG ")) {
                    System.out.println(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= 방 목록 UI =================
    private void updateRoomList() {
        SwingUtilities.invokeLater(() -> {
            roomListPanel.removeAll();

            for (String r : rooms) {
                JPanel roomBox = new JPanel(null);
                roomBox.setPreferredSize(new Dimension(400, 90));
                roomBox.setBackground(Color.WHITE);

                JLabel name = new JLabel("방 제목: " + r);
                name.setBounds(20, 10, 300, 20);
                roomBox.add(name);

                JButton joinBtn = new JButton("참여하기");
                joinBtn.setBounds(140, 45, 130, 30);
                joinBtn.addActionListener(e -> requestJoinRoom(r));
                roomBox.add(joinBtn);

                roomListPanel.add(roomBox);
            }

            roomListPanel.revalidate();
            roomListPanel.repaint();
        });
    }

    private void cleanup() {
        try { socket.close(); } catch (Exception ignored) {}
        dispose();
    }
}
