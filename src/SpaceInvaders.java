import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList; // vai guardar os inimigos e os tiros em um array
import java.util.Objects;
import java.util.Random; // vai gerar inimigos de cores aleatórias
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    public static class Block {
        // o ideal é criar classes diferentes para cada objeto do jogo, mas assim serve
        int x;
        int y;
        int width;
        int height;
        Image img;
        boolean alive = true;
        boolean used = false;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    int tileSize = 32;
    int rows = 16;
    int cols = 16;
    int boardWidth = tileSize * cols; // as declarações em GUI servem apenas para a janela
    int boardHeight = tileSize * rows; // essas servem para o jogo em si

    Image shipImg;
    Image alienImg;
    Image alienCyanImg;
    Image alienMagentaImg;
    Image alienYellowImg;
    ArrayList<Image> alienImages = new ArrayList<>();

    int shipWidth = tileSize*2;
    int shipHeight = tileSize;
    int shipX = tileSize*cols/2 - tileSize; // no grid 16x16, a nave ficará no meio
    int shipY = boardHeight - tileSize*2; // na penúltima linha do tabuleiro
    int shipVelocityX = tileSize;
    Block ship;

    ArrayList<Block> alienFleet;
    int alienWidth = tileSize*2;
    int alienHeight = tileSize;
    int alienX = tileSize;
    int alienY = 2*tileSize;

    int alienRows = 2;
    int alienCols = 3;
    int alienCount = 0;
    int alienVelocityX = 1;

    ArrayList<Block> ammunition;
    int bulletWidth = tileSize/8;
    int bulletHeight = tileSize/2;
    int bulletVelocityY = -10;
    // não precisa de imagem

    Timer gameLoop;
    int score = 0;
    boolean gameOver = false;
    int highScore = 0;
    int wave = 1;

    SpaceInvaders() {
        setPreferredSize(new Dimension(boardWidth, boardHeight)); // cria um painel nas dimensões desejadas
        setBackground(Color.darkGray); // reduzir eyestrain
        setFocusable(true);
        addKeyListener(this);

        shipImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./ship.png"))).getImage();
        alienImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./alien.png"))).getImage();
        alienCyanImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./alien-cyan.png"))).getImage();
        alienMagentaImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./alien-magenta.png"))).getImage();
        alienYellowImg = new ImageIcon(Objects.requireNonNull(getClass().getResource("./alien-yellow.png"))).getImage();

        alienImages = new ArrayList<>();
        alienImages.add(alienImg);
        alienImages.add(alienCyanImg);
        alienImages.add(alienMagentaImg);
        alienImages.add(alienYellowImg);

        ship = new Block(shipX, shipY, shipWidth, shipHeight, shipImg);
        alienFleet = new ArrayList<>();
        ammunition = new ArrayList<>();

        gameLoop = new Timer(1000/60, this);
        createAliens();
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); // paintComponent faz parte de JPanel, herdamos ela
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        for (Block alien : alienFleet) {
            if (alien.alive) {
                g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
            }
        }

        g.setColor(Color.green);
        for (Block bullet : ammunition) {
            if (!bullet.used) {
                g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
            }
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));

        String waveText = "Wave: " + wave; // declaro fora do escopo do if para poder usar no else
        int waveWidth = g.getFontMetrics().stringWidth(waveText);

        if (gameOver) {
            g.setColor(Color.red);

            String newHighScoreText = "New high score! " + score;
            String highScoreText = "High Score: " + highScore;
            String gameOverText = "Game Over.";
            String gameOverScoreText = "Game Over: " + score;
            String restartText = "Press any key to restart";

            int newHighScoreWidth = g.getFontMetrics().stringWidth(newHighScoreText);
            int highScoreWidth = g.getFontMetrics().stringWidth(highScoreText);
            int gameOverWidth = g.getFontMetrics().stringWidth(gameOverText);
            int gameOverScoreWidth = g.getFontMetrics().stringWidth(gameOverScoreText);
            int restartWidth = g.getFontMetrics().stringWidth(restartText);

            int centerX = boardWidth / 2;

            if (score > highScore) {
                g.drawString(newHighScoreText, centerX - newHighScoreWidth / 2, boardHeight / 2 - 80);
                g.drawString(waveText, centerX - waveWidth / 2, boardHeight / 2 - 40);
                g.drawString(gameOverText, centerX - gameOverWidth / 2, boardHeight / 2);
            } else {
                g.drawString(highScoreText, centerX - highScoreWidth / 2, boardHeight / 2 - 80);
                g.drawString(waveText, centerX - waveWidth / 2, boardHeight / 2 - 40);
                g.drawString(gameOverScoreText, centerX - gameOverScoreWidth / 2, boardHeight / 2);
            }
            g.drawString(restartText, centerX - restartWidth / 2, boardHeight / 2 + 40);
        } else {
            g.drawString("Score: " + score, 10, 35);
            g.drawString("Wave: " + wave, boardWidth - waveWidth - 10, 35);
        }
    }

    public void move() {
        for (int i = 0; i < alienFleet.size(); i++) {
            Block alien = alienFleet.get(i);
            if (alien.alive) {
                alien.x += alienVelocityX;

                if(alien.x + alien.width >= boardWidth || alien.x <= 0) { // se o alien bater na borda
                    alienVelocityX *= -1; // sua velocidade inverte, e vai pro outro lado
                    alien.x += alienVelocityX*2;

                    for (Block block : alienFleet) {
                        block.y += alienHeight; // faz todos os aliens descerem uma linha
                    }
                }

                if(alien.y >= ship.y) {
                    gameOver = true;
                }
            }
        }

        for (Block bullet : ammunition) {
            bullet.y += bulletVelocityY;

            for (Block alien : alienFleet) {
                if (!bullet.used && alien.alive && collision(alien, bullet)) {
                    alien.alive = false;
                    bullet.used = true;
                    alienCount--;
                    score++; // score aumenta a cada alien morto
                }
            }
        }

        // não é o método mais eficiente, talvez trocar ArrayList por LinkedList
        while (!ammunition.isEmpty() && (ammunition.getFirst().used || ammunition.getFirst().y < 0)) {
            ammunition.removeFirst();
        }

        if (alienCount == 0) {
            score += alienCols * alienRows; // bônus para nível limpo
            wave++;
            alienCols = Math.min(alienCols + 1, cols/2 - 2); // limita a quantidade máxima de colunas para 6
            alienRows = Math.min(alienRows + 1, rows - 6); // limita a quantidade máxima de linhas para 10
            alienFleet.clear();
            ammunition.clear();
            alienVelocityX = 1; // sempre que gerar novos, vão para a direita
            createAliens();
        }
    }

    public void createAliens() {
        Random rand = new Random();

        for (int r = 0; r < alienRows; r++) {
            for (int c = 0; c < alienCols; c++) {
                int randomImageIndex = rand.nextInt(alienImages.size()); // devolve valor entre 0 e 3
                Block alien = new Block(
                        alienX + c * alienWidth, // para cada coluna, adiciona um novo inimigo
                        alienY + r * alienHeight, // após a primeira linha, cria novos aliens em baixo
                        alienWidth,
                        alienHeight,
                        alienImages.get(randomImageIndex)
                );
                alienFleet.add(alien);
            }
        }
        alienCount = alienFleet.size();
    }

    public boolean collision (Block alien, Block bullet) {
        return alien.x < bullet.x + bullet.width &&
               alien.x + alien.width > bullet.x &&
               alien.y < bullet.y + bullet.height &&
               alien.y + alien.height > bullet.y;
    }

    @Override // método obrigatório pelo uso da interface ActionListener
    public void actionPerformed(ActionEvent e) {
        move();
        repaint(); // "built-in function" - seu funcionamento está intrinseco ao JPanel, chama o paintComponent durante o gameLoop
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            if (score > highScore) {
                highScore = score;
            }

            ship.x = shipX;
            alienFleet.clear();
            ammunition.clear();

            score = 0;
            wave = 1;

            alienVelocityX = 1;
            alienCols = 3;
            alienRows = 2;

            gameOver = false;
            createAliens();
            gameLoop.start();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship.x + ship.width + shipVelocityX <= boardWidth) {
            ship.x += shipVelocityX;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX;
        } else if (e.getExtendedKeyCode() == KeyEvent.VK_SPACE) { // offset de 15/32 deixa no centro (tile 16px, bala tem 2px de largura)
            Block bullet = new Block(ship.x + shipWidth * 15 / 32, ship.y, bulletWidth, bulletHeight, null); // (x, y, width, height, img)/
            ammunition.add(bullet);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}