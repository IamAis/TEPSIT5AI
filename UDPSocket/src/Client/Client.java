package Client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
    	
    	
    	Boolean exit = false;
    	
    	//settings Socket Datagram
        DatagramSocket socket = new DatagramSocket();
        InetAddress indirizzoServer = InetAddress.getByName("localhost");
        int portaServer = 9876;
        
        //Settings Scanner
        Scanner scanner = new Scanner(System.in);
        
        while(!exit) { //Comunicazione

        	
        //Invio del MSG
        System.out.print("Scrivi un messaggio da inviare al server: ");
        String messaggio = scanner.nextLine();
        
        
        if(messaggio.equals("exit")) {
        	exit = true;
        	System.out.println("Sei uscito!");
        }
        byte[] bufferInvio = messaggio.getBytes();
        DatagramPacket pacchetto = new DatagramPacket(bufferInvio, bufferInvio.length, indirizzoServer, portaServer);
        
        //Il metodo send
        socket.send(pacchetto);

        
        //Ricezione del MSG
        byte[] bufferRicezione = new byte[1024];
        DatagramPacket pacchettoRisposta = new DatagramPacket(bufferRicezione, bufferRicezione.length);
        
        //Il metodo Receive
        socket.receive(pacchettoRisposta);

        String risposta = new String(
        		pacchettoRisposta.getData(),           // buffer intero  
        	    0,                             // offset: da dove iniziare
        	    pacchettoRisposta.getLength()          // quanti byte leggere (esatti)
        	);
        System.out.println("Risposta dal server: " + risposta);

        }
        
        socket.close();
        scanner.close();
    }
}
