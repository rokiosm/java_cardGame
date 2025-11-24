import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class home extends JFrame {

    private JButton b_enter, b_setting, b_rule;
    private JTextField t_nickname;
    private JLabel l_nickname;

    public home() {
        super("Home");

        buildGUI();

        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildGUI() {
        // 메인 패널: BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(60, 122, 65));

        // 상단 (설정 버튼, 룰 버튼)
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);

        // 중앙 (흰색 박스)
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    // 상단 패널
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // 설정
        b_setting = new JButton(new ImageIcon("images/setting.png"));
        makeTransparentButton(b_setting);
        b_setting.addActionListener(e -> new setting(home.this));
        topPanel.add(b_setting, BorderLayout.WEST);

        // 룰
        b_rule = new JButton(new ImageIcon("images/rule.png"));
        makeTransparentButton(b_rule);
        b_rule.addActionListener(e -> new rule());
        topPanel.add(b_rule, BorderLayout.EAST);

        return topPanel;
    }

    // 공통 – 투명 버튼 설정
    private void makeTransparentButton(JButton btn) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
    }

    // 중앙 패널
    private JPanel createCenterPanel() {

        // FlowLayout: 가운데 정렬
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 70));
        centerPanel.setOpaque(false);

        // 흰색 박스
        JPanel whiteBox = new JPanel();
        whiteBox.setBackground(Color.WHITE);
        whiteBox.setPreferredSize(new Dimension(240, 130));

        // 내부 구성: GridLayout 3행
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
            new lobby();
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
        SwingUtilities.invokeLater(home::new);
    }
}
