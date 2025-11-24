package network_game.src;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Lobby extends JFrame {

    private JPanel roomListPanel;
    private JTextField roomTitleInput;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<String> rooms = new ArrayList<>();
    private Socket socket;

    public Lobby(String userName) {
        super("방 로비 - " + userName);

        connectServer(userName);

        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JPanel bg = new JPanel(null);
        bg.setBounds(0, 0, 800, 500);
        bg.setBackground(new Color(60, 122, 65));
        add(bg);

        // 방 만들기 패널
        JPanel createPanel = new JPanel(null);
        createPanel.setBounds(40, 60, 240, 160);
        createPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("방 만들기", SwingConstants.CENTER);
        title.setBounds(0, 10, 240, 25);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        createPanel.add(title);

        JLabel rt = new JLabel("방 제목 :");
        rt.setBounds(20, 55, 80, 20);
        createPanel.add(rt);

        roomTitleInput = new JTextField();
        roomTitleInput.setBounds(80, 55, 130, 22);
        createPanel.add(roomTitleInput);

        JButton createBtn = new JButton("만들기");
        createBtn.setBounds(70, 100, 100, 30);
        createPanel.add(createBtn);
        bg.add(createPanel);

        // 방 목록 패널
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(new Color(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(roomListPanel);
        scrollPane.setBounds(330, 40, 430, 400);
        scrollPane.setBorder(null);
        bg.add(scrollPane);

        // 방 만들기 클릭
        createBtn.addActionListener(e -> {
            String titleText = roomTitleInput.getText().trim();
            if (!titleText.isEmpty()) {
                out.println("CREATE " + titleText);
                roomTitleInput.setText("");
            }
        });

        // 서버 메시지 수신 스레드
        new Thread(this::listenServer).start();

        // 요청해서 방 목록 갱신
        out.println("GET_ROOMS");

        setVisible(true);
    }

    private void connectServer(String name) {
        try {
            socket = new Socket("127.0.0.1", 5001);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            in.readLine(); // ENTER_NAME
            out.println(name);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패");
        }
    }

    private void listenServer() {
        String msg;
        try {
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("ROOM ")) {
                    String roomName = msg.substring(5);
                    if (!rooms.contains(roomName)) rooms.add(roomName);
                    updateRoomList();
                } else if (msg.equals("ROOM_END")) {
                    // 방 목록 끝
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                joinBtn.addActionListener(e -> {
                    out.println("JOIN " + r);
                    try {
                        new Room(r, socket); // 소켓 전달해서 Room 창 열기
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                roomBox.add(joinBtn);
                roomListPanel.add(roomBox);
            }
            roomListPanel.revalidate();
            roomListPanel.repaint();
        });
    }
}
