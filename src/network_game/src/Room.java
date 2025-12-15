package network_game.src;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.text.*;
import java.util.*;

public class Room extends JFrame {

    private JTextPane chatArea;
    private JTextField chatInput;
    private JComboBox<String> chatType;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Thread receiveThread;
    
    private String badge;
    private Map<String, String> userBadges = new HashMap<>();

    public Room(String roomName, Socket socket, PrintWriter out, BufferedReader in, String badge) throws IOException {
        super("방: " + roomName);
        this.socket = socket;
        this.out = out; //추가
        this.in = in; //추가
        this.badge = badge;

        //out = new PrintWriter(socket.getOutputStream(), true);
        //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        buildGUI();
        startReceiveThread();
        setVisible(true);
    }

    private void buildGUI() {
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatArea = new JTextPane();
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
    
    //추가함
    private void appendMessage(String nickname, String badgeFile, String text) {
        StyledDocument doc = chatArea.getStyledDocument();

        try {
            // 배지 이미지 붙이기
            if (badgeFile != null && !badgeFile.equals("null")) {
                ImageIcon icon = new ImageIcon("images/" + badgeFile);
                chatArea.insertIcon(icon);
                doc.insertString(doc.getLength(), " ", null);
            }

            // 닉네임
            doc.insertString(doc.getLength(), "[" + nickname + "] ", null);

            // 메시지
            doc.insertString(doc.getLength(), text + "\n", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Chat
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
                //if (line.startsWith("MSG ")) chatArea.append(line.substring(4) + "\n");
                /*else if (line.startsWith("USERLIST ")) {
                    String[] users = line.substring(9).split(",");
                    for (String u : users) {
                        String nickname = u.split(":")[0];
                        chatArea.append("[" + nickname + "] 입장하였습니다.\n");
                    }
                }*/
            	if (line.startsWith("MSG ")) {
            	    String raw = line.substring(4); // "[닉네임] : 내용"

            	    int start = raw.indexOf("[") + 1;
            	    int end = raw.indexOf("]");

            	    if (start < 0 || end < 0) return;

            	    String nickname = raw.substring(start, end);
            	    String text = raw.substring(end + 1).trim(); // ": 내용"

            	    String badgeFile = userBadges.get(nickname);

            	    appendMessage(nickname, badgeFile, text);
            	}
                else if (line.startsWith("USERLIST ")) {
                    String[] users = line.substring(9).split(",");

                    for (String u : users) {
                        if (u.isEmpty()) continue;
                        String[] parts = u.split(":");
                        String nickname = parts[0];
                        String badgeFile = parts.length > 2 ? parts[2] : null;

                        userBadges.put(nickname, badgeFile);

                        appendMessage(nickname, badgeFile, "입장하였습니다.");
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
