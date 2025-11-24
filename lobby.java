import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class lobby extends JFrame {

    private JPanel roomListPanel;         // 방 목록이 들어가는 패널
    private ArrayList<String> rooms;      // 방 목록 저장 리스트
    private JTextField roomTitleInput;    // 방 제목 입력

    public lobby() {
        super("방 로비");

        rooms = new ArrayList<>();

        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // ===== 배경 =====
        JPanel bg = new JPanel(null);
        bg.setBounds(0, 0, 800, 500);
        bg.setBackground(new Color(60, 122, 65));
        add(bg);

        // ============================================================
        //     왼쪽 - 방 만들기 박스
        // ============================================================
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

        // ============================================================
        //     오른쪽 - 방 목록 영역 (Scroll + Grid Layout)
        // ============================================================
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new GridLayout(0, 1, 0, 10));
        roomListPanel.setBackground(new Color(0, 0, 0, 0));

        // ScrollPane에 붙이기
        JScrollPane scrollPane = new JScrollPane(roomListPanel);
        scrollPane.setBounds(330, 40, 430, 400);
        scrollPane.setBorder(null);
        bg.add(scrollPane);

        // ============================================================
        //     방 만들기 버튼 동작
        // ============================================================
        createBtn.addActionListener(e -> {
            String titleText = roomTitleInput.getText().trim();
            if (titleText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "방 제목을 입력하세요!");
                return;
            }

            rooms.add(titleText);
            updateRoomList();
            roomTitleInput.setText("");
        });

        setVisible(true);
    }

    // =============================================================
    //     방 목록 갱신 함수
    // =============================================================
    private void updateRoomList() {

        roomListPanel.removeAll();

        for (String r : rooms) {

            JPanel roomBox = new JPanel(null);
            roomBox.setPreferredSize(new Dimension(400, 90));
            roomBox.setBackground(Color.WHITE);

            JLabel name = new JLabel("방 제목: " + r);
            name.setBounds(20, 10, 300, 20);
            roomBox.add(name);

            JButton joinBtn = new JButton("참여하기 (1/4)");
            joinBtn.setBounds(140, 45, 130, 30);
            roomBox.add(joinBtn);

            roomListPanel.add(roomBox);
        }

        roomListPanel.revalidate();
        roomListPanel.repaint();
    }
}