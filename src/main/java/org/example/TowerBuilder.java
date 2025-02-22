package org.example;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class TowerBuilder extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private ArrayList<Block> blocks;
    private int baseX = 300, baseY = 500, blockWidth = 100, blockHeight = 30;
    private boolean falling = true;
    private int currentX, currentY;
    private Random rand = new Random();

    public TowerBuilder() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.BLACK);
        blocks = new ArrayList<>();
        currentX = baseX;
        currentY = 50;

        timer = new Timer(10, this);
        timer.start();

        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        for (Block b : blocks) {
            b.draw(g);
        }

        g.setColor(Color.RED);
        g.fillRect(currentX, currentY, blockWidth, blockHeight);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (falling) {
            currentY += 2;
            if (currentY >= baseY - blocks.size() * blockHeight) {
                falling = false;
                blocks.add(new Block(currentX, currentY, blockWidth, blockHeight));
                currentX = rand.nextInt(500);
                currentY = 50;
                falling = true;
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !falling) {
            falling = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tower Builder");
        TowerBuilder game = new TowerBuilder();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
}

class Block {
    int x, y, width, height;

    public Block(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}