package tris;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class GameClientGUI {

    private JFrame frame;
    private JButton[][] boardButtons;
    private JTextArea chatArea;
    private JTextField chatInput;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean myTurn = false;
    private boolean gameEnded = false;
    private String mySymbol;
    private String opponentSymbol;

    // Variabili per la schermata iniziale
    private JFrame startFrame;
    private JTextField ipField;
    private JTextField portField;
    private JLabel errorLabel;

    public GameClientGUI() {
        // Schermata di inizio
        startFrame = new JFrame("Tris - Inizia Partita");
        startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startFrame.setSize(400, 250);
        startFrame.setLayout(new BorderLayout());

        // Panel di input per IP e porta
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        panel.add(new JLabel("Indirizzo IP:"));
        ipField = new JTextField("localhost");
        panel.add(ipField);

        panel.add(new JLabel("Porta:"));
        portField = new JTextField("12345");
        panel.add(portField);

        JButton findGameButton = new JButton("Trova Partita");
        findGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        findGameButton.addActionListener(e -> startGame());

        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        
        panel.add(findGameButton);
        startFrame.add(panel, BorderLayout.CENTER);
        startFrame.add(errorLabel, BorderLayout.SOUTH);

        startFrame.setVisible(true);
    }

    private void startGame() {
        String ip = ipField.getText().trim();
        String portStr = portField.getText().trim();

        // Validazione input
        if (ip.isEmpty() || portStr.isEmpty()) {
            showError("IP o porta non validi!");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            showError("La porta deve essere un numero!");
            return;
        }

        // Chiudiamo la schermata di inizio
        startFrame.setVisible(false);

        // Avvio della connessione al server
        connectToServer(ip, port);
    }

    private void connectToServer(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Thread per ricevere messaggi dal server
            Thread receiverThread = new Thread(() -> {
                try {
                    while (true) {
                        DataTransfer data = (DataTransfer) in.readObject();
                        handleServerMessage(data);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    showMessage("Connessione persa con il server o MATCH ANNULATO.");
                }
            });
            receiverThread.start();

            // Finestra di gioco
            createGameWindow();

        } catch (IOException e) {
            showError("Impossibile connettersi al server: " + e.getMessage());
        }
    }

    private void createGameWindow() {
        frame = new JFrame("Tris - Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Scacchiera
        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        boardButtons = new JButton[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton button = new JButton("");
                button.setFont(new Font("Arial", Font.BOLD, 30));
                button.setBackground(Color.WHITE);
                button.setFocusPainted(false);
                button.setEnabled(false);
                final int x = i, y = j;
                button.addActionListener(e -> handleMove(x, y));
                boardButtons[i][j] = button;
                boardPanel.add(button);
            }
        }

        // Area chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Input chat
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendButton = new JButton("Invia");
        sendButton.addActionListener(e -> sendMessage());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(chatScrollPane, BorderLayout.EAST);
        frame.add(chatInputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void handleServerMessage(DataTransfer data) {
        if (data.getMsg() != null) {
            String message = data.getMsg();

            if ("/EGOISTA".equals(message)) {
                myTurn = true;
                mySymbol = "O";
                opponentSymbol = "X";
                enableBoard(true);
                showMessage("Inizia tu! Il tuo simbolo è O.");
            } else if ("/SECONDO".equals(message)) {
                myTurn = false;
                enableBoard(false);
                mySymbol = "X";
                opponentSymbol = "O";
                showMessage("Attendi il turno dell'avversario. Il tuo simbolo è X.");
            } else if ("/QUIT".equalsIgnoreCase(message)) {
                showMessage("L'avversario ha abbandonato la partita. Partita terminata.");
                disableBoard();
            } else if ("/WIN".equalsIgnoreCase(message)) {
                showMessage("Hai vinto! Complimenti!");
                disableBoard();
                gameEnded = true;
            } else {
                showMessage("Chat: " + message);
            }
        } else {
            // Gestione della mossa dell'avversario
            int x = data.getX();
            int y = data.getY();

                boardButtons[x][y].setText(opponentSymbol);
                boardButtons[x][y].setEnabled(false);
                myTurn = true;
                enableBoard(true);
                showMessage("È il tuo turno.");

                // Controlla se l'avversario ha vinto
                if (checkWin(opponentSymbol)) {
                    showMessage("Hai perso! L'avversario ha fatto tris.");
                    disableBoard();
                    gameEnded = true;
                }

        }
    }

    private void handleMove(int x, int y) {
    	if (gameEnded) return;

    	if (!boardButtons[x][y].getText().isEmpty()) {
    	    showMessage("Mossa non valida!");
    	    return;
    	}


        boardButtons[x][y].setText(mySymbol);
        boardButtons[x][y].setEnabled(false);
        myTurn = false;
        enableBoard(false);

        try {
            out.writeObject(new DataTransfer(x, y));
        } catch (IOException e) {
            showMessage("Errore durante l'invio della mossa.");
        }

        if (checkWin(mySymbol)) {
            //showMessage("Complimenti! Hai fatto tris!");
            disableBoard();
            gameEnded = true;
            try {
                out.writeObject(new DataTransfer("/WIN"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) return;

        try {
            out.writeObject(new DataTransfer(message));
            chatInput.setText("");
        } catch (IOException e) {
            showMessage("Errore durante l'invio del messaggio.");
        }
    }

    private void showMessage(String message) {
        chatArea.append(message + "\n");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void enableBoard(boolean enable) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boardButtons[i][j].getText().isEmpty()) {
                    boardButtons[i][j].setEnabled(enable && !gameEnded);
                }
            }
        }
    }

    private void disableBoard() {
        enableBoard(false);
    }

    private boolean checkWin(String symbol) {
        for (int i = 0; i < 3; i++) {
            if (boardButtons[i][0].getText().equals(symbol) &&
                boardButtons[i][1].getText().equals(symbol) &&
                boardButtons[i][2].getText().equals(symbol)) return true;

            if (boardButtons[0][i].getText().equals(symbol) &&
                boardButtons[1][i].getText().equals(symbol) &&
                boardButtons[2][i].getText().equals(symbol)) return true;
        }

        if (boardButtons[0][0].getText().equals(symbol) &&
            boardButtons[1][1].getText().equals(symbol) &&
            boardButtons[2][2].getText().equals(symbol)) return true;

        if (boardButtons[0][2].getText().equals(symbol) &&
            boardButtons[1][1].getText().equals(symbol) &&
            boardButtons[2][0].getText().equals(symbol)) return true;

        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClientGUI::new);
    }
}