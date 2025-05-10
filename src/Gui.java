import javax.swing.*;

public class Gui {
    public static void main(String[] args) {
        int tileSize = 32;
        int rows = 16;
        int cols = 16;
        int boardWidth = tileSize * cols; // poderia ser 512, mas assim só altera 1 variável
        int boardHeight = tileSize * rows; // para aumentar o tamanho do tabuleiro

        JFrame frame = new JFrame("Space Invaders");
        frame.setSize(boardWidth, boardHeight); // precisa estar antes do setLocation, se não a ponta da janela que fica no centro
        frame.setLocationRelativeTo(null); // centraliza a janela na tela
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // fecha o programa ao fechar a janela
        frame.setResizable(false); // não permite mudar o tamanho

        SpaceInvaders game = new SpaceInvaders();
        frame.add(game);
        frame.pack();
        game.requestFocus();
        frame.setVisible(true); // deixa visível apenas depois de tudo ser inicializado
    }
}
