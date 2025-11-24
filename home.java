package network_game.src;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Home extends JFrame {

    private JButton b_enter, b_setting, b_rule;
    private JTextField t_nickname;
    private JLabel l_nickname;

    public Home() {
        super("Home");

        buildGUI();

        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(60, 122, 65));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        b_setting = new JButton(new ImageIcon("images/setting.png"));
        makeTransparentButton(b_setting);
        b_setting.addActionListener(e -> new Setting(Home.this));
        topPanel.add(b_setting, BorderLayout.WEST);

        // 룰
        b_rule = new JButton(new ImageIcon("images/rule.png"));
        makeTransparentButton(b_rule);
        b_rule.addActionListener(e -> new Rule());
        topPanel.add(b_rule, BorderLayout.EAST);

        return topPanel;
    }

    private void makeTransparentButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        centerPanel.setOpaque(false);

        JPanel whiteBox = new JPanel();
        whiteBox.setBackground(Color.WHITE);
        whiteBox.setPreferredSize(new Dimension(240, 130));
        whiteBox.setLayout(new GridLayout(3, 1));

        // 닉네임 라벨
        l_nickname = new JLabel("닉네임 :");
        JPanel nicknamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        nicknamePanel.setOpaque(false);
        nicknamePanel.add(l_nickname);
        whiteBox.add(nicknamePanel);

        // 텍스트필드
        t_nickname = new JTextField(10);
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        textPanel.setOpaque(false);
        textPanel.add(t_nickname);
        whiteBox.add(textPanel);

        // 로비 버튼
        b_enter = new JButton("로비 가기");
        b_enter.setBackground(new Color(255, 190, 0));
        b_enter.setFocusPainted(false);
        b_enter.addActionListener(e -> {
            String nickname = t_nickname.getText().trim();
            if (nickname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "닉네임을 입력하세요!");
                return;
            }
            new Lobby(nickname); // Lobby 클래스에 닉네임 전달
            dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(b_enter);
        whiteBox.add(buttonPanel);

        centerPanel.add(whiteBox);

        return centerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Home::new);
    }
}
