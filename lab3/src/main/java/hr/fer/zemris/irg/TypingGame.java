package hr.fer.zemris.irg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TypingGame extends JFrame implements ActionListener {
    private final String letters = "abcdefghijklmnopqrstuvwxyz";
    private final int minDistanceBetweenTwoLetters = 30;
    private final int nLetters = 5;
    private final int width = 800;
    private final int height = 800;
    private final JLabel scoreLabel;
    private final JTextField textField;
    private final Timer timer;
    private final ArrayList<FallingLetter> lettersList;
    private final Random rand = new Random();
    private final JButton newGameButton;
    private final JComboBox<String> difficultyComboBox;
    private int score = 0;
    private boolean gameOver = false;
    private int difficulty;


    public TypingGame() {
        JLabel label = new JLabel("Type the letters as they fall:");
        scoreLabel = new JLabel("Score: " + score);
        textField = new JTextField(5);
        JPanel panel = new JPanel();
        panel.add(label);
        panel.add(textField);
        panel.add(scoreLabel);

        // add new game button
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> resetGame());
        newGameButton.setVisible(false);
        panel.add(newGameButton);

        // add difficulty level combo box
        String[] difficultyLevels = {"Easy", "Medium", "Hard"};
        difficultyComboBox = new JComboBox<>(difficultyLevels);
        difficultyComboBox.addActionListener(e -> {
            difficulty = difficultyComboBox.getSelectedIndex() * 2;
            resetGame();
        });
        panel.add(difficultyComboBox);

        add(BorderLayout.SOUTH, panel);
        timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (FallingLetter fl : lettersList) {
                    fl.update();
                    if (fl.getY() > getHeight()) {
                        gameOver = true;
                        newGameButton.setVisible(true);
                        timer.stop();
                    }
                }
                repaint();
            }
        });
        timer.start();
        lettersList = new ArrayList<>();
        for (int i = 0; i < nLetters; i++) {
            lettersList.add(new FallingLetter());
        }
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameOver) return;
                boolean hit = false;
                for (FallingLetter fl : lettersList) {
                    if (e.getKeyChar() == fl.getLetter()) {
                        score++;
                        fl.reset();
                        hit = true;
                        break;
                    }
                }
                if (!hit) score--;

                textField.setText("");
                scoreLabel.setText("Score: " + score);
            }
        });
    }

    public static void main(String[] args) {
        TypingGame frame = new TypingGame();
        frame.setTitle("Typing Game");
        frame.setSize(frame.width, frame.height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void resetGame() {
        score = 0;
        textField.setText("");
        scoreLabel.setText("Score: " + score);
        lettersList.clear();
        for (int i = 0; i < nLetters; i++) {
            lettersList.add(new FallingLetter());
        }
        gameOver = false;
        newGameButton.setVisible(false);
        timer.start();
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (!gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            for (FallingLetter fl : lettersList) {
                g.drawString(String.valueOf(fl.getLetter()), fl.getX(), fl.getY());
            }
        } else {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over!", getWidth() / 2 - 75, getHeight() / 2);
            g.drawString("Score: " + score, getWidth() / 2 - 25, getHeight() / 2 + 25);
        }
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private class FallingLetter {
        private char letter;
        private int x;
        private int y;
        private int speed;

        public FallingLetter() {
            generateNewLetterAndItsLocation();
            speed = rand.nextInt(3) + 2;
        }

        public void update() {
            y += speed;
        }

        public void reset() {
            generateNewLetterAndItsLocation();
            speed += difficulty;
        }

        private void generateNewLetterAndItsLocation() {
            List<Character> currentLetters = new ArrayList<>();
            List<Integer> currentXLocations = new ArrayList<>();
            for (FallingLetter l : lettersList) {
                if (l.equals(this)) continue;
                currentLetters.add(l.letter);
                currentXLocations.add(l.getX());
            }

            letter = letters.charAt(rand.nextInt(letters.length()));
            while (currentLetters.contains(letter)) {
                letter = letters.charAt(rand.nextInt(letters.length()));
            }

            int minDistance;
            do {
                minDistance = Integer.MAX_VALUE;
                x = 20 + rand.nextInt(width - 50);
                for (int otherX : currentXLocations) {
                    int distance = Math.abs(otherX - x);
                    if (distance < minDistance) {
                        minDistance = distance;
                    }
                }
            } while (minDistance < minDistanceBetweenTwoLetters);
            y = 30;
        }

        public char getLetter() {
            return letter;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}