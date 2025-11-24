import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class setting extends JDialog {

	private JLabel l_nickname, l_bgm, l_effect, l_badge;
	private JTextField t_nickname;
	private JSlider s_bgm, s_effect;
	private JButton b_badge, b_save, b_close;
	
	public setting(JFrame parent) {
		super(parent, "설정", true);
		
		buildGUI();
		
		setBounds(100, 200, 300, 400);
		
		setVisible(true);
	}
	
	private void buildGUI() {
		setLayout(new BorderLayout());

        // 메인 패널 (home처럼)
        JPanel mainPanel = createSettingPanel();
        add(mainPanel, BorderLayout.CENTER);
	}
	
	private JPanel createSettingPanel() {
		JPanel panel = new JPanel(null);

        // 닉네임 레이블
        l_nickname = new JLabel("닉네임 변경:");
        l_nickname.setBounds(20, 20, 100, 30);
        panel.add(l_nickname);

        // 닉네임 입력
        t_nickname = new JTextField();
        t_nickname.setBounds(130, 20, 150, 30);
        panel.add(t_nickname);

        // 배경음악 슬라이더
        l_bgm = new JLabel("배경음악:");
        l_bgm.setBounds(20, 80, 100, 30);
        panel.add(l_bgm);

        s_bgm = new JSlider(0, 100, 50);
        s_bgm.setBounds(130, 80, 150, 40);
        panel.add(s_bgm);

        // 효과음 슬라이더
        l_effect = new JLabel("효과음:");
        l_effect.setBounds(20, 130, 100, 30);
        panel.add(l_effect);

        s_effect = new JSlider(0, 100, 50);
        s_effect.setBounds(130, 130, 150, 40);
        panel.add(s_effect);

        // 배지 선택
        l_badge = new JLabel("배지:");
        l_badge.setBounds(20, 180, 100, 30);
        panel.add(l_badge);

        b_badge = new JButton("배지 선택");
        b_badge.setBounds(130, 180, 150, 30);
        panel.add(b_badge);

        b_badge.addActionListener(e -> {
            badge dialog = new badge((JFrame)getParent());
            dialog.setVisible(true);
        });

        // 저장 버튼
        b_save = new JButton("저장");
        b_save.setBounds(70, 240, 90, 35);
        panel.add(b_save);

        // 닫기 버튼
        b_close = new JButton("닫기");
        b_close.setBounds(180, 240, 90, 35);
        panel.add(b_close);

        // 버튼 이벤트
        b_close.addActionListener(e -> dispose());
        b_save.addActionListener(e -> {
            // 값 저장 처리 넣을 곳
            dispose();
        });

        return panel;
	}
}