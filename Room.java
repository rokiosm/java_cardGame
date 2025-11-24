package network_game.src;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Room extends JFrame {

    // ===== UI =====
    private JTextArea chatArea;
    private JTextField chatInput;
    private JComboBox<String> chatType;

    // ===== Network =====
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // ===== Thread =====
    private Thread receiveThread;

    public Room(String roomName, Socket socket) throws IOException {
        super("방: " + roomName);
        this.socket = socket;

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        buildGUI();
        startReceiveThread();
        setVisible(true);
    }

    private void buildGUI() {
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendBtn = new JButton("전송");
        chatType = new JComboBox<>(new String[]{"전체", "팀"});

        inputPanel.add(chatType, BorderLayout.WEST);
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());
    }

    // ===== Chat =====
    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;
        if (chatType.getSelectedItem().equals("팀")) out.println("TEAM " + msg);
        else out.println("ALL " + msg);
        chatInput.setText("");
    }

    private void startReceiveThread() {
        receiveThread = new Thread(this::receiveLoop);
        receiveThread.start();
    }

    private void receiveLoop() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                if (line.startsWith("MSG ")) chatArea.append(line.substring(4) + "\n");
                else if (line.startsWith("USERLIST ")) {
                    String[] users = line.substring(9).split(",");
                    for (String u : users) {
                        String nickname = u.split(":")[0];
                        chatArea.append("[" + nickname + "] 입장하였습니다.\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
