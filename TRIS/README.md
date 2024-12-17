# Documentazione del Programma di Tris - Brachini Faraoni Buselli

## Panoramica

Questo programma consente di giocare a Tris (Tic-Tac-Toe) in modalità multiplayer tra due client. 
Il gioco si basa su un server che gestisce la connessione tra i client, che si trovano in attesa di una partita. 
I dettagli di implementazione sono descritti di seguito.

## Come Avviare il Server

Il server è avviato dalla classe `GameServer`. Quando il programma è eseguito, il server ascolta sulla 
porta predefinita *12345* (o su una porta specificata se si modifica la costante `PORT` nel sorgente del server).
Quando è avviato esso stamperà "Server TRIS in ascolto sulla porta 12345", successivamente attenderà che i client si connettino
e quando avverrà stamperà "Nuovo client connesso: /*ip*"

### Come Avviare il Client
Per avviare il client sarà sufficiente avviarlo dalla classe GameClientGUI, che aprirà un interfaccia grafica; una volta
impostato l'ip, la porta e il nome utente, basta premere "TROVA PARTITA", e il programma attenderà in background fino alla connessione di un altro client

### Come Giocare
Per giocare è necessario cliccare sulla casella, nella quale si vuole segnare il proprio simbolo, ed attendere la mossa dell'avversario.
Se si vuole uscire anticipatamente bastarà scrivere in Chat "/QUIT", ed attendere 3 secondi per disconessione. Per scrivere un qualcunque
messaggio basterà scriverlo nella "chatInput" e premere il bottone "invia". Quando qualcuno vincerà oppure quando verrà raggiunto un pareggio
la chat guiderà su come continuare (scrivendo /QUIT). NOTA BENE il client si riaprirà automaticamente dopo la partita solo se si esce attraverso /QUIT.
