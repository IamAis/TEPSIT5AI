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
                    while (!gameEnded) {
                        DataTransfer data = (DataTransfer) in.readObject();
                        handleServerMessage(data);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!gameEnded) {
                        showMessage("Connessione persa con il server.");
                    }
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
            } else if ("/SECONDO".equals(message)) {
                myTurn = false;
                enableBoard(false);
                mySymbol = "X";
                opponentSymbol = "O";
            } else if ("/QUIT".equals(message)) {
                myTurn = false;
                enableBoard(false);
                showMessage("Il tuo avversario ha abbandonato, digita /QUIT per uscire anche tu!");
                gameEnded = true;
            } else if ("/WIN".equals(message)) {
                myTurn = false;
                gameEnded = true;
                enableBoard(false);
                showMessage("Hai vinto complimenti!");
            } else if ("/PATE".equals(message)) {
                myTurn = false;
                gameEnded = true;
                enableBoard(false);
                showMessage("Hai pareggiato. Digita /QUIT per uscire!");
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
                
                if(checkDraw()) {
                	showMessage("Hai pareggiato. Digita /QUIT per uscire!");
                	gameEnded = true;
                	try {
                	out.writeObject(new DataTransfer("/PATE"));
                	}catch (IOException e) {
                		disableBoard();

                    }
                }
                
                if (checkWin(opponentSymbol)) {
                    showMessage("Hai perso! L'avversario ha fatto tris. Digita /QUIT per uscire!");
                    gameEnded = true;
                    try {
                    	out.writeObject(new DataTransfer("/WIN"));
                    }catch (IOException e) {
 

                    }
                    disableBoard();
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
        }
    }

    private void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
            showMessage("Connessione chiusa.");
        } catch (IOException e) {
            showMessage("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }
    
    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) return;

        if (message.equalsIgnoreCase("/QUIT")) {
    	    disableBoard();
    	    try {
    	    out.writeObject(new DataTransfer("/QUIT"));
    	    Thread.sleep(3000);
    	    } catch(InterruptedException | IOException i) {
    	    	try {
    	    	Thread.sleep(3000);
    	    	goToStartScreen();
    	    	return;
    	    	} catch(InterruptedException h) {
    	    		
    	    	}
    	    }
    	    goToStartScreen();
    	    return;
    	}
        
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
    	    if (boardButtons == null) {
    	        System.err.println("Errore: boardButtons non è stata inizializzata!");
    	        return;
    	    }

    	    for (int i = 0; i < 3; i++) {
    	        for (int j = 0; j < 3; j++) {
    	            if (boardButtons[i][j] != null && boardButtons[i][j].getText().isEmpty()) {
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
    
    private boolean checkDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boardButtons[i][j].getText().isEmpty()) {
                    // Se trovi una casella vuota, il gioco non è ancora finito
                    return false;
                }
            }
        }
        // Se tutte le caselle sono occupate e nessuno ha vinto, è un pareggio
        return true;
    }

    
    private void goToStartScreen() {
        closeConnection();
        frame.setVisible(false); // Nascondi la finestra di gioco
        frame.dispose(); // Libera le risorse
        startFrame.setVisible(true); // Mostra la finestra iniziale
        resetGame(); // Resetta lo stato del gioco
    }
    
    private void resetGame() {
        myTurn = false;
        gameEnded = false;
        mySymbol = null;
        opponentSymbol = null;
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameClientGUI::new);
    }
}