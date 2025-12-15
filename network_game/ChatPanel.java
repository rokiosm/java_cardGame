package network_game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.BiConsumer;

/**
 * 채팅 패널
 * - 전체 / 팀 채팅 선택
 * - 채팅 입력
 * - 채팅 로그 표시
 * - 유저 리스트 표시
 */
public class ChatPanel extends JPanel {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn;

    private JRadioButton allBtn;
    private JRadioButton teamBtn;

    private DefaultListModel<String> userModel;
    private JList<String> userList;

    private final BiConsumer<String, String> sendHandler;

    public ChatPanel(BiConsumer<String, String> sendHandler) {
        this.sendHandler = sendHandler;
        buildUI();
    }

    // ==========================
    // UI 구성
    // ==========================
    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        // ───────── 채팅 로그 ─────────
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(300, 380));
        add(chatScroll, BorderLayout.CENTER);

        // ───────── 하단 입력 영역 ─────────
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(new Color(60, 60, 60));

        inputField = new JTextField();
        sendBtn = new JButton("전송");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        // ───────── 채팅 채널 선택 ─────────
        JPanel channelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        channelPanel.setBackground(new Color(60, 60, 60));

        allBtn = new JRadioButton("전체", true);
        teamBtn = new JRadioButton("팀");

        ButtonGroup group = new ButtonGroup();
        group.add(allBtn);
        group.add(teamBtn);

        allBtn.setForeground(Color.WHITE);
        teamBtn.setForeground(Color.WHITE);
        allBtn.setBackground(new Color(60, 60, 60));
        teamBtn.setBackground(new Color(60, 60, 60));

        channelPanel.add(allBtn);
        channelPanel.add(teamBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(channelPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // ───────── 유저 리스트 ─────────
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        userList.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(300, 120));

        add(userScroll, BorderLayout.NORTH);

        // ───────── 이벤트 ─────────
        ActionListener sendAction = e -> sendChat();
        sendBtn.addActionListener(sendAction);
        inputField.addActionListener(sendAction);
    }

    // ==========================
    // 채팅 송신
    // ==========================
    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        String channel = teamBtn.isSelected() ? "TEAM" : "ALL";
        sendHandler.accept(channel, text);

        inputField.setText("");
    }

    // ==========================
    // 외부 호출용 메서드
    // ==========================
    public void addChatMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void clearUsers() {
        SwingUtilities.invokeLater(userModel::clear);
    }

    public void addUser(String name) {
        SwingUtilities.invokeLater(() -> userModel.addElement(name));
    }
}
