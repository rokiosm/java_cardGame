package network_game.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Speed 카드 게임 GamePanel
 * - 카드 클릭 시 서버로 PLAY 전송
 * - 서버 메시지 기반으로만 화면 갱신
 */
public class GamePanel extends JPanel {

    // ===== 서버 송신 =====
    private final Consumer<String> sender;

    // ===== 카드 상태 =====
    private final List<String> myHand = new ArrayList<>();
    private final Deque<String> center = new ArrayDeque<>();

    // ===== UI =====
    private static final int CARD_W = 80;
    private static final int CARD_H = 120;

    public GamePanel(Consumer<String> sender) {
        this.sender = sender;
        setBackground(new Color(40, 120, 40));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // ==========================
    // 서버 → 클라이언트 명령 처리
    // ==========================

    // HAND 7S,1D,13H,...
    public void setHand(String data) {
        myHand.clear();
        if (!data.isEmpty()) {
            for (String c : data.split(",")) {
                if (!c.isEmpty()) myHand.add(c);
            }
        }
        repaint();
    }

    // CENTER 5H,6S,...
    public void setCenter(String data) {
        center.clear();
        if (!data.isEmpty()) {
            for (String c : data.split(",")) {
                if (!c.isEmpty()) center.push(c);
            }
        }
        repaint();
    }

    // PLAY_OK → 내 카드 제거
    public void removeCard(String card) {
        myHand.remove(card);
        repaint();
    }

    // ==========================
    // 카드 클릭 처리
    // ==========================
    private void handleClick(int x, int y) {
        int startX = 50;
        int yPos = getHeight() - CARD_H - 30;

        for (int i = 0; i < myHand.size(); i++) {
            int cx = startX + i * 30;
            Rectangle r = new Rectangle(cx, yPos, CARD_W, CARD_H);

            if (r.contains(x, y)) {
                String card = myHand.get(i);
                sender.accept("PLAY " + card);
                return;
            }
        }
    }

    // ==========================
    // 렌더링
    // ==========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCenter(g);
        drawHand(g);
    }

    private void drawCenter(Graphics g) {
        int cx = getWidth() / 2 - CARD_W / 2;
        int cy = getHeight() / 2 - CARD_H / 2;

        int offset = 0;
        for (String c : center) {
            drawCard(g, c, cx + offset, cy);
            offset += 15;
            if (offset > 30) break; // 상위 몇 장만
        }
    }

    private void drawHand(Graphics g) {
        int x = 50;
        int y = getHeight() - CARD_H - 30;

        for (String c : myHand) {
            drawCard(g, c, x, y);
            x += 30;
        }
    }

    // ==========================
    // 카드 그리기
    // ==========================
    private void drawCard(Graphics g, String card, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x, y, CARD_W, CARD_H, 10, 10);

        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, CARD_W, CARD_H, 10, 10);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(card, x + 10, y + 20);
    }
}
