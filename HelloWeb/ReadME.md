# Progetto Hello Web v2

## Autore: *Francesco Brachini*

## Descrizione

Questo progetto è stato sviluppato come parte dell'attività di laboratorio per sperimentare la creazione di applicazioni Web, ed utilizzare API Restful, sviluppato in *Java* con un progetto *Maven*, collegandosi alla porta 8080 é possibile visualizzare una pagina jsp, e collegandosi ad /api/test é possibile sfruttare un API che permette la stampa per nome nella richiesta http. (nell'url)

## Frameworks e Librerie

1. **JAX-RS (Jersey)**: Framework per la creazione di servizi RESTful in Java.
2. **Jetty**: Server web leggero per eseguire applicazioni web in Java.


## Come utilizzarlo
1. Caricare il progetto su Eclipse
2. eseguire il comando maven mvn jetty;run oppure maven build -> jetty:run
3. collegandosi a localhost:8080/api/test/{nome} si accede all'API, e mentre per accedere alla pagine jsp a localhost:8080.

