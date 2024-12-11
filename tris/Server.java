package tris;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameServer {
    private static final int PORT = 12345;
    private static BlockingQueue<Socket> waitingQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server TRIS in ascolto sulla porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuovo client connesso: " + clientSocket.getInetAddress());

                waitingQueue.add(clientSocket);

                // Avvia una partita quando ci sono due giocatori
                if (waitingQueue.size() >= 2) {
                    Socket player1 = waitingQueue.poll();
                    Socket player2 = waitingQueue.poll();

                    System.out.println("Match trovato! Player 1: " + player1.getInetAddress() +
                            " | Player 2: " + player2.getInetAddress());

                    new Thread(new MatchHandler(player1, player2)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MatchHandler gestisce la logica della partita tra due giocatori
    static class MatchHandler implements Runnable {
        private Socket player1;
        private Socket player2;

        public MatchHandler(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try (
                ObjectInputStream in1 = new ObjectInputStream(player1.getInputStream());
                ObjectOutputStream out1 = new ObjectOutputStream(player1.getOutputStream());

                ObjectInputStream in2 = new ObjectInputStream(player2.getInputStream());
                ObjectOutputStream out2 = new ObjectOutputStream(player2.getOutputStream())
            ) {
                System.out.println("Partita iniziata tra " + player1.getInetAddress() + " e " + player2.getInetAddress());

                // Messaggio di avvio della partita
                out1.writeObject(new DataTransfer("La partita è iniziata!"));
                out2.writeObject(new DataTransfer("La partita è iniziata!"));

                boolean player1Starts = new java.util.Random().nextBoolean();

                if (player1Starts) {
                    out1.writeObject(new DataTransfer("/EGOISTA"));
                    out2.writeObject(new DataTransfer("/SECONDO"));
                } else {
                    out2.writeObject(new DataTransfer("/EGOISTA"));
                    out1.writeObject(new DataTransfer("/SECONDO"));
                }

                // Avvia i thread per ciascun giocatore
                new Thread(new PlayerHandler(player1, out2, in1)).start();
                new Thread(new PlayerHandler(player2, out1, in2)).start();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    player1.close();
                    player2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connessioni chiuse. Partita terminata.");
            }
        }
    }

    // PlayerHandler gestisce la comunicazione con un singolo giocatore
    static class PlayerHandler implements Runnable {
        private Socket playerSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public PlayerHandler(Socket playerSocket, ObjectOutputStream out, ObjectInputStream in) {
            this.playerSocket = playerSocket;
            this.out = out;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DataTransfer data = (DataTransfer) in.readObject();

                    if (data.isChat()) {
                        if ("/WIN".equalsIgnoreCase(data.getMsg())) {
                            // Gestione della vittoria
                            System.out.println("Player ha vinto.");
                            out.writeObject(new DataTransfer("Hai vinto"));
                            break;
                        }
                        if ("/QUIT".equalsIgnoreCase(data.getMsg())) {
                            // Gestione dell'uscita dalla partita
                            System.out.println("Player ha abbandonato.");
                            break;
                        }
                        System.out.println("Player ha inviato un messaggio: " + data.getMsg());
                        // Invia il messaggio all'altro giocatore
                    } else {
                        System.out.println("Player ha fatto una mossa: (" + data.getX() + ", " + data.getY() + ")");
                        // Invia la mossa all'altro giocatore
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
