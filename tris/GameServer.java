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
            BlockingQueue<DataTransfer> queue1to2 = new LinkedBlockingQueue<>();
            BlockingQueue<DataTransfer> queue2to1 = new LinkedBlockingQueue<>();
            
            boolean player1Starts = new java.util.Random().nextBoolean();
            
            Thread player1Handler;
            Thread player2Handler;
            
            if(player1Starts) {
            player1Handler = new Thread(new PlayerHandler(player1, queue1to2, queue2to1, true));
            player2Handler = new Thread(new PlayerHandler(player2, queue2to1, queue1to2, false));
            } else {
            player1Handler = new Thread(new PlayerHandler(player1, queue1to2, queue2to1, false));
            player2Handler = new Thread(new PlayerHandler(player2, queue2to1, queue1to2, true));
            }
            	

            player1Handler.start();
            player2Handler.start();

            try {
                player1Handler.join();
                player2Handler.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Connessioni chiuse. Partita terminata.");
        }
    }

    // PlayerHandler gestisce la comunicazione di un singolo giocatore
    static class PlayerHandler implements Runnable {
    	private boolean First;
        private Socket playerSocket;
        private BlockingQueue<DataTransfer> outgoingQueue;
        private BlockingQueue<DataTransfer> incomingQueue;

        public PlayerHandler(Socket playerSocket, BlockingQueue<DataTransfer> outgoingQueue, BlockingQueue<DataTransfer> incomingQueue, boolean first) {
            this.playerSocket = playerSocket;
            this.outgoingQueue = outgoingQueue;
            this.incomingQueue = incomingQueue;
            this.First = first;
        }

        @Override
        public void run() {
            try (
                ObjectInputStream in = new ObjectInputStream(playerSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(playerSocket.getOutputStream())
            ) {
                // Messaggio di avvio della partita
                out.writeObject(new DataTransfer("La partita è iniziata!"));
                if(First == true)
                out.writeObject(new DataTransfer("/EGOISTA"));
                else
                out.writeObject(new DataTransfer("/SECONDO"));
                // Thread per gestire i messaggi in uscita
                Thread sender = new Thread(() -> {
                    try {
                        while (true) {
                            DataTransfer data = incomingQueue.take();
                            out.writeObject(data);
                        }
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Errore nel thread sender: " + e.getMessage());
                    }
                });

                sender.start();

                // Ciclo per ricevere i messaggi
                while (true) {
                    DataTransfer data = (DataTransfer) in.readObject();

                    if (data.isChat()) {
                        if ("/WIN".equalsIgnoreCase(data.getMsg())) {
                            outgoingQueue.put(new DataTransfer("Hai perso"));
                            incomingQueue.put(new DataTransfer("Hai vinto"));
                            break;
                        }
                        if ("/QUIT".equalsIgnoreCase(data.getMsg())) {
                            incomingQueue.put(new DataTransfer("L'avversario ha abbandonato la partita. La partita è terminata."));
                            break;
                        }
                    }

                    outgoingQueue.put(data);
                }

                sender.interrupt();

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    playerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
