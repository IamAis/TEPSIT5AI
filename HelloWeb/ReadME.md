# Progetto Hello REST - Java Web e Maven - *Francesco Brachini*

## Descrizione

Questo progetto è stato sviluppato come parte dell'attività di laboratorio per sperimentare la creazione di applicazioni Web, ed utilizzare API Rest

## Frameworks e Librerie

1. **JAX-RS (Jersey)**: Framework per la creazione di servizi RESTful in Java.
   - **jersey-container-servlet**: Gestisce l'integrazione di Jersey con i servlet in un'applicazione web.
   - **jersey-media-moxy**: Fornisce il supporto per il marshalling e unmarshalling di dati in formato JSON/XML.

3. **Jetty**: Server web leggero per eseguire applicazioni web in Java.
   - **jetty-maven-plugin**: Consente di eseguire il server Jetty direttamente tramite Maven per testare l'applicazione in locale.


###Come utilizzarlo
1. Caricare il progetto su Eclipse
2. eseguire il comando maven mvn jetty;run oppure maven build -> jetty:run

