package network_game.src;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Lobby extends JFrame {

    private JTextField roomTitleInput;
    private JPanel roomListPanel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private ArrayList<String> rooms = new ArrayList<>();
    private String userName;

    private Thread receiveThread;
    
    private String badge;

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

    // 서버 연결
    private void connectServer() {
        try {
            socket = new Socket("127.0.0.1", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 서버 이름 요청 처리
            in.readLine(); // ENTER_NAME
            System.out.println("보내는 값 = " + userName + "|" + badge);
            sendMessage(userName + "|" + badge);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        }
    }

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

    private void createRoom() {
        String titleText = roomTitleInput.getText().trim();
        if (!titleText.isEmpty()) {
            sendMessage("CREATE " + titleText);
            roomTitleInput.setText("");
        }
    }

    private void joinRoom(String roomName) {
        sendMessage("JOIN " + roomName);
        
        /*try {
            new Room(roomName, socket, badge); // 소켓 전달
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }

        try {
        	new Room(roomName, socket, out, in, badge);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    private void startReceiveThread() {
        receiveThread = new Thread(this::receiveLoop);
        receiveThread.start();
    }

    private void receiveLoop() {
        String msg;
        try {
        	while (!Thread.currentThread().isInterrupted() &&
                    (msg = in.readLine()) != null) { //추가
                if (msg.startsWith("ROOM ")) {
                    String roomName = msg.substring(5);
                    if (!rooms.contains(roomName)) rooms.add(roomName);
                    updateRoomList();
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

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
                joinBtn.addActionListener(e -> joinRoom(r));
                roomBox.add(joinBtn);

                roomListPanel.add(roomBox);
            }
            roomListPanel.revalidate();
            roomListPanel.repaint();
        });
    }
}
