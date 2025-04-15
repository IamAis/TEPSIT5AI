package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(9876); // Porta su cui ascoltare
        byte[] bufferRicezione = new byte[1024]; //buffer di ricezione

        DatagramPacket pacchettoRicevuto = new DatagramPacket(bufferRicezione, bufferRicezione.length);
        
        System.out.println("Server in ascolto sulla porta 9876...");

        while (true) {
            
            socket.receive(pacchettoRicevuto); // Attende dati dal client

            String messaggio = new String(pacchettoRicevuto.getData(), 0, pacchettoRicevuto.getLength());
            if(messaggio.equals("exit")) //Quando mi viene inviato Exit esco dal while
            	break;
            System.out.println("Ricevuto: " + messaggio);

            String risposta = "Ciao client, ho ricevuto: " + messaggio.toUpperCase();
            byte[] bufferRisposta = risposta.getBytes();

            InetAddress indirizzoClient = pacchettoRicevuto.getAddress(); 
            int portaClient = pacchettoRicevuto.getPort();
            //Ottengo l'indirizzo e la porta del Client per rendirizzare la risposta.
            DatagramPacket pacchettoRisposta = new DatagramPacket(bufferRisposta, bufferRisposta.length, indirizzoClient, portaClient);
            socket.send(pacchettoRisposta);
        }
        socket.close();
    }
}
