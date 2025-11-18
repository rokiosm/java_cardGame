import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class clientUI extends JFrame {

	public clientUI() {
        setTitle("1vs1 카드게임 - 대기 화면 + 채팅");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // CENTER - 게임 화면
       gamePanel gamePanel = new gamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // EAST - 채팅 화면
        chatPanel chatPanel = new chatPanel();
        add(chatPanel, BorderLayout.EAST);

        setVisible(true);
    }

    public static void main(String[] args) {
       new clientUI();
    }
}
