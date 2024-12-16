package tris;

import java.io.Serializable;

// Deve implementare Serializable per poter essere inviata tramite ObjectOutputStream
public class DataTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message; // Per i messaggi di chat
    private int x; // Coordinata X per una mossa
    private int y; // Coordinata Y per una mossa
    private boolean isChat; // Indica se è un messaggio di chat o una mossa di gioco

    // Costruttore per un messaggio di chat
    public DataTransfer(String message) {
        this.message = message;
        this.isChat = true;
    }

    // Costruttore per una mossa di gioco
    public DataTransfer(int x, int y) {
        this.x = x;
        this.y = y;
        this.isChat = false;
    }

    // Getter per i messaggi di chat
    public String getMsg() {
        return message;
    }

    // Getter per le coordinate della mossa
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Metodo per sapere se è un messaggio di chat
    public boolean isChat() {
        return isChat;
    }
}
