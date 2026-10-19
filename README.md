Zyro è un'applicazione Android nativa essenziale, veloce e ad alte prestazioni progettata per una sola identica cosa: andare in live streaming instantaneo sulla rete locale (P2P).

Zero configurazioni complesse, zero server cloud esterni e latenza ridotta al minimo. Apri l'app, premi il pulsante e trasmetti subito il flusso video/audio della fotocamera su qualsiasi dispositivo connesso alla tua stessa rete Wi-Fi o Hotspot.

 Caratteristiche Principali
Start in 1 Tap: Avvio immediato dello streaming locale senza passaggi intermedi.

Server Locale Integrato: Generazione automatica dell'indirizzo IP locale (es. [http://192.168.1.50:8080](http://192.168.1.50:8080)) per la connessione immediata dei viewer.

A Bassa Latenza: Flusso video/audio fluido trasmesso direttamente in P2P.

Controlli Fotocamera: Switch rapido tra fotocamera frontale e posteriore e gestione della torcia.

Interfaccia Minimal Cyberpunk: Design scuro ed essenziale per non sprecare risorse e batteria.

🛠️ Requisiti & Tecnologie
Linguaggio: Kotlin

UI Framework: Jetpack Compose

Camera Engine: CameraX

Server HTTP/P2P: Ktor / Netty

Compatibilità: Android 8.0 (API 26) o superiore

Come Utilizzarlo
Apri Zyro sul tuo smartphone Android.

Assicurati che il telefono e i dispositivi di destinazione siano sulla stessa rete Wi-Fi o Hotspot.

Premi Avvia Live.

Inserisci l'IP mostrato a schermo nel browser o nell'app viewer di qualsiasi PC, TV o smartphone per guardare la diretta.

L'idea nasce dal desiderio di sperimentare con le tecnologie di rete mobile e lo streaming peer-to-peer (P2P), creando un'alternativa minimalista e diretta ai grandi servizi di streaming. Invece di dipendere da piattaforme esterne, Zyro permette di sfruttare unicamente la propria rete Wi-Fi o l'Hotspot dello smartphone per trasmettere in tempo reale, unendo la programmazione alla praticità d'uso quotidiano.

Come e stata creata (Architettura e Tecnologie)
L'applicazione e stata scritta interamente da zero utilizzando le tecnologie piu moderne per lo sviluppo Android:

Linguaggio: Kotlin puro, per garantire prestazioni elevate, sicurezza del codice e massima compatibilita con l'ecosistema Android.

Interfaccia Grafica: Jetpack Compose, utilizzato per costruire un'interfaccia utente reattiva, moderna e pulita, caratterizzata da un tema scuro ed essenziale ottimizzato per ridurre il consumo energetico.

Gestione della Fotocamera: La libreria CameraX di Android, che permette di catturare il flusso video e audio della fotocamera con una latenza minima e un controllo diretto sull'anteprima.

Server P2P Locale: Un server HTTP integrato (basato su Ktor) che gira direttamente sul telefono sulla porta 8080. Questo server trasforma lo smartphone in una vera e propria stazione di trasmissione, generando un indirizzo IP locale (es. http://192.168.1.50:8080) utilizzabile da qualsiasi browser o dispositivo connesso alla stessa rete per seguire la diretta.

Come funziona l'utilizzo
L'esperienza utente e stata ridotta all'essenziale per rispettare la filosofia di Zyro: si apre l'applicazione, si preme un unico pulsante per avviare la trasmissione e il flusso video viene immediatamente condiviso in rete locale senza la necessita di registrazioni, account o configurazioni complesse.
