package src;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class MoleBuster {
    int boardWidth = 600;
    int boardHeight = 700;

    JFrame frame = new JFrame("GAME: MOGURA TAIJI");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel controlPanel = new JPanel();
    JLabel timerLabel = new JLabel();

    JButton[] board = new JButton[9];
    ImageIcon moleIcon;
    ImageIcon plantIcon;

    JButton currMoleTile;
    ArrayList<JButton> currPlantTiles = new ArrayList<>();

    Random random = new Random();
    Timer setMoleTimer;
    Timer setPlantTimer;
    Timer countdownTimer;
    int score = 0;
    int highScore = 0;
    int timeLeft = 60; 

    JButton restartButton = new JButton("Restart");
    JButton stopButton = new JButton("Stop");
    JButton muteButton = new JButton("Mute");

    boolean isMuted = false;
    Clip backgroundMusic;

    MoleBuster() {
        loadSounds();
        playBackgroundMusic();

        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 30));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Score: " + score + " | High Score: " + highScore);
        textLabel.setOpaque(true);
        textLabel.setBackground(new Color(60, 63, 65));
        textLabel.setForeground(Color.WHITE);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setForeground(Color.RED);
        timerLabel.setText("Time Left: " + timeLeft);
        textPanel.add(timerLabel, BorderLayout.EAST);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3, 3));
        boardPanel.setBackground(new Color(43, 43, 43));
        frame.add(boardPanel);

        Image plantImg = new ImageIcon(getClass().getResource("/resources/piranha.png")).getImage();
        plantIcon = new ImageIcon(plantImg.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH));

        Image moleImg = new ImageIcon(getClass().getResource("/resources/monty.png")).getImage();
        moleIcon = new ImageIcon(moleImg.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH));

        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            board[i] = tile;
            boardPanel.add(tile);
            tile.setFocusable(false);
            tile.setBackground(new Color(83, 104, 114));
            tile.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            tile.setContentAreaFilled(false);
            tile.setOpaque(true);

            tile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    tile.setBackground(new Color(100, 150, 200));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tile.setBackground(new Color(83, 104, 114));
                }
            });

            tile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton tile = (JButton) e.getSource();
                    if (tile == currMoleTile) {
                        score += 10;
                        textLabel.setText("Score: " + score + " | High Score: " + highScore);
                        playSound("/resources/hit.wav");
                        showScorePopup(tile, "+10");
                    } else if (currPlantTiles.contains(tile)) {
                        if (score > highScore) highScore = score;
                        textLabel.setText("Game Over! Score: " + score + " | High Score: " + highScore);
                        playSound("/resources/game_over.wav");
                        setMoleTimer.stop();
                        setPlantTimer.stop();
                        countdownTimer.stop();
                        for (int i = 0; i < 9; i++) {
                            board[i].setEnabled(false);
                        }
                        stopButton.setEnabled(false);
                        restartButton.setEnabled(true);
                        showGameOverAnimation();
                    }
                }
            });
        }

        setMoleTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currMoleTile != null) {
                    currMoleTile.setIcon(null);
                }

                int num = random.nextInt(9);
                JButton tile = board[num];

                if (currPlantTiles.contains(tile)) return;

                currMoleTile = tile;
                currMoleTile.setIcon(moleIcon);
                tile.setBackground(new Color(200, 100, 100)); 
            }
        });

        setPlantTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JButton tile : currPlantTiles) {
                    tile.setIcon(null);
                }
                currPlantTiles.clear();

                int plantCount = 2 + random.nextInt(2);
                for (int i = 0; i < plantCount; i++) {
                    int num = random.nextInt(9);
                    JButton tile = board[num];

                    if (tile == currMoleTile || currPlantTiles.contains(tile)) continue;

                    currPlantTiles.add(tile);
                    tile.setIcon(plantIcon);
                }
            }
        });

        countdownTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time Left: " + timeLeft);
                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    setMoleTimer.stop();
                    setPlantTimer.stop();
                    if (score > highScore) highScore = score;
                    textLabel.setText("Time Up! Score: " + score + " | High Score: " + highScore);
                    for (int i = 0; i < 9; i++) {
                        board[i].setEnabled(false);
                    }
                    stopButton.setEnabled(false);
                    restartButton.setEnabled(true);
                }
            }
        });

        restartButton.setFont(new Font("Arial", Font.BOLD, 30));
        restartButton.setEnabled(false);
        restartButton.setBackground(new Color(0, 128, 0));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                score = 0;
                timeLeft = 60;
                textLabel.setText("Score: " + score + " | High Score: " + highScore);
                timerLabel.setText("Time Left: " + timeLeft);
                for (int i = 0; i < 9; i++) {
                    board[i].setIcon(null);
                    board[i].setEnabled(true);
                }
                setMoleTimer.start();
                setPlantTimer.start();
                countdownTimer.start();
                restartButton.setEnabled(false);
                stopButton.setEnabled(true);
                playBackgroundMusic();
            }
        });

        stopButton.setFont(new Font("Arial", Font.BOLD, 30));
        stopButton.setBackground(new Color(220, 20, 60));
        stopButton.setForeground(Color.WHITE);
        stopButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMoleTimer.stop();
                setPlantTimer.stop();
                countdownTimer.stop();
                for (int i = 0; i < 9; i++) {
                    board[i].setEnabled(false);
                }
                stopButton.setEnabled(false);
                restartButton.setEnabled(true);
                stopBackgroundMusic();
            }
        });

        muteButton.setFont(new Font("Arial", Font.BOLD, 30));
        muteButton.setBackground(new Color(75, 75, 75));
        muteButton.setForeground(Color.WHITE);
        muteButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        muteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isMuted = !isMuted;
                if (isMuted) {
                    stopBackgroundMusic();
                    muteButton.setText("Unmute");
                } else {
                    playBackgroundMusic();
                    muteButton.setText("Mute");
                }
            }
        });

        controlPanel.setLayout(new GridLayout(1, 3, 10, 0));
        controlPanel.add(restartButton);
        controlPanel.add(stopButton);
        controlPanel.add(muteButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        setMoleTimer.start();
        setPlantTimer.start();
        countdownTimer.start();
        stopButton.setEnabled(true);
        frame.setVisible(true);
    }

    private void showScorePopup(JButton tile, String scoreText) {
        final JDialog popup = new JDialog(frame, false);
        popup.setUndecorated(true);
        popup.setLayout(new GridBagLayout());
        JLabel scoreLabel = new JLabel(scoreText);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        scoreLabel.setForeground(Color.YELLOW);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(0, 0, 0, 150)); 
        popup.add(scoreLabel);
        popup.pack();
    
        Point location = tile.getLocationOnScreen();
        popup.setLocation(location.x + tile.getWidth() / 2 - popup.getWidth() / 2, location.y - popup.getHeight());
        popup.setVisible(true);
    
        Timer timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popup.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    

    private void showGameOverAnimation() {
        final JDialog popup = new JDialog(frame, false);
        popup.setUndecorated(true);
        popup.setSize(300, 200);
        popup.setLocationRelativeTo(frame);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 0, 0, 0));
        JLabel label = new JLabel("Game Over!");
        label.setForeground(Color.RED);
        label.setFont(new Font("Arial", Font.BOLD, 50));
        panel.add(label);

        popup.add(panel);
        popup.setVisible(true);

        Timer timer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popup.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void loadSounds() {
        try {
            URL backgroundMusicUrl = getClass().getResource("/resources/background.wav");
            if (backgroundMusicUrl != null) {
                backgroundMusic = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(backgroundMusicUrl);
                backgroundMusic.open(inputStream);
            } else {
                System.err.println("Background music file not found.");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playSound(String soundFile) {
        if (isMuted) return;
        try {
            URL soundUrl = getClass().getResource(soundFile);
            if (soundUrl != null) {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundUrl);
                clip.open(inputStream);
                clip.start();
            } else {
                System.err.println("Sound file not found: " + soundFile);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void playBackgroundMusic() {
        if (!isMuted && backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MoleBuster());
    }
}



