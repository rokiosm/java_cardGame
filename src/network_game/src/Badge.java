package network_game.src;
import javax.swing.*;
import java.awt.*;

public class Badge extends JDialog {

    // 선택된 배지 저장 (나중에 활용 가능)
    private String selectedBadge = null;

    public Badge(JFrame parent) {
        super(parent, "배지 선택", true);

        setSize(350, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 중앙: 이미지 버튼 그리드
        JPanel imagePanel = new JPanel(new GridLayout(3, 3, 10, 10));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // images 폴더 안의 배지 이름들
        String[] badgeNames = {
                "bronze.png", "badge2.png", "badge3.png",
                "badge4.png", "badge5.png", "badge6.png",
                "badge7.png", "badge8.png", "badge9.png"
        };

        for (String badge : badgeNames) {
            JButton btn = new JButton();

            // 이미지 로드
            ImageIcon icon = new ImageIcon("images/" + badge);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));

            btn.addActionListener(e -> {
                selectedBadge = badge;
                JOptionPane.showMessageDialog(this, badge + " 배지를 선택했습니다.");
            });

            imagePanel.add(btn);
        }

        add(imagePanel, BorderLayout.CENTER);

        // 하단 버튼 영역
        JPanel bottom = new JPanel();

        JButton okBtn = new JButton("확인");
        JButton cancelBtn = new JButton("닫기");

        okBtn.addActionListener(e -> {
            // 나중에 저장 로직 연결 가능
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        bottom.add(cancelBtn);
        bottom.add(okBtn);
        add(bottom, BorderLayout.SOUTH);
    }
}
