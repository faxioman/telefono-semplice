# Pubblicazione sul Play Store

Tutto ciò che si può automatizzare è nel repository:
- la scheda in 10 lingue (testi, icona, immagine in evidenza, screenshot) è in `app/src/play/play/`;
- il workflow **Play Store** (`.github/workflows/play.yml`) carica il bundle e la scheda.

I passaggi qui sotto vanno fatti **una volta sola**, a mano, perché Google non li permette via API.

## 1. Account sviluppatore (organizzazione)

1. Numero **D-U-N-S** dell'azienda: si verifica o si richiede su https://www.dnb.com/duns-number/lookup.html (gratuito, di solito alcuni giorni).
2. Su https://play.google.com/console/signup scegli **Un'organizzazione** e paga i 25 $. Servono il D-U-N-S, il sito web, un'email e un telefono di contatto.
3. Attendi la verifica di Google.

Con un account organizzazione **non** è richiesto il test chiuso con 12 tester per 14 giorni.

## 2. Creare l'app e caricare il primo bundle

1. Play Console → **Crea app**:
   - nome: `Telefono Semplice per anziani`;
   - lingua predefinita: Italiano;
   - App, Gratuita; accetta le dichiarazioni.
2. Scarica `TelefonoSemplice-play.aab` dall'ultima Release su GitHub.
3. **Test → Test interno → Crea nuova release**:
   - alla domanda sulla firma accetta **Firma dell'app di Google Play**: la nostra chiave diventa la chiave di caricamento;
   - carica l'`.aab`;
   - salva e pubblica nel test interno.
4. **Test interno → Tester**: crea un elenco con le email di chi proverà l'app (anche solo la tua).

Il primo caricamento deve essere manuale: Google non lo consente via API.

## 3. Accesso per la pubblicazione automatica

1. https://console.cloud.google.com → crea un progetto, per esempio `telefono-semplice`.
2. **API e servizi → Libreria** → abilita **Google Play Android Developer API**.
3. **IAM → Account di servizio → Crea account di servizio**:
   - nome `play-publisher`, nessun ruolo;
   - poi **Chiavi → Aggiungi chiave → JSON**, che scarica un file.
4. Play Console → **Utenti e autorizzazioni → Invita nuovi utenti**:
   - email: quella dell'account di servizio (`…@….iam.gserviceaccount.com`);
   - **Autorizzazioni app**: aggiungi l'app e spunta *Rilasciare app nei canali di test*, *Rilasciare in produzione…* e *Gestire la presenza sullo Store*.
5. GitHub → **Settings → Secrets and variables → Actions → New repository secret**:
   - nome `PLAY_SERVICE_ACCOUNT`;
   - valore: l'intero contenuto del file JSON.

   In alternativa: `gh secret set PLAY_SERVICE_ACCOUNT -R faxioman/telefono-semplice < file.json`

## 4. Sezione "Contenuti dell'app" (risposte pronte)

| Voce | Risposta |
|---|---|
| Norme sulla privacy | URL pubblico di `docs/privacy-policy.html`, per esempio `https://www.rapidosoft.it/telefono-semplice/privacy.html` (il repository è privato, quindi il file va caricato sul sito) |
| Accesso all'app | Tutte le funzionalità sono disponibili senza accesso speciale |
| Annunci | No, l'app non contiene annunci |
| Classificazione dei contenuti | Categoria: *Tutti gli altri tipi di app*. Tutte le risposte **No**, tranne *"Gli utenti possono interagire o scambiarsi contenuti"*: **Sì**, perché fa telefonate. Risultato atteso: PEGI 3 / Tutti |
| Pubblico di destinazione | Solo **18 anni e oltre**: evita le regole per le app per bambini |
| App di notizie | No |
| Sicurezza dei dati | *"La tua app raccoglie o condivide dati utente?"* → **No**: tutto resta sul dispositivo; i pagamenti delle donazioni li gestisce Google Play |
| App governativa | No |
| Funzionalità finanziarie | Nessuna |
| App per la salute | Nessuna funzionalità per la salute |
| Intent a schermo intero (`USE_FULL_SCREEN_INTENT`) | L'app è un'app per chiamate (app Telefono predefinita): mostra le chiamate in arrivo |

Non servono dichiarazioni per SMS e registro chiamate: la versione per il Play Store non usa quei permessi.

## 5. Donazioni (prodotti in-app)

Serve un **profilo pagamenti** collegato all'account (*Impostazioni → Profilo pagamenti*: dati fiscali e IBAN).

Poi **Monetizza → Prodotti → Prodotti in-app → Crea prodotto**, tre volte:

| ID prodotto | Nome | Prezzo |
|---|---|---|
| `coffee_small` | Un caffè | 1,00 € |
| `coffee_medium` | Caffè e brioche | 3,00 € |
| `coffee_large` | Colazione al bar | 5,00 € |

Gli ID devono essere esattamente questi. Finché i prodotti non esistono, l'app non mostra la sezione donazioni.

## 6. Scheda dello store

- **Categoria**: Comunicazione.
- Titolo, descrizioni, icona, immagine in evidenza, screenshot, email (`play/contact-email.txt`) e sito (`play/contact-website.txt`) li carica il workflow. Prima di pubblicare controlla email e sito.
- Per rigenerare le immagini dopo una modifica all'interfaccia:
  ```
  ./gradlew -PscreenshotSdk=35 recordPaparazziFullDebug --tests '*StoreAssetsTest*'
  python3 tools/copy_store_assets.py
  ```

## 7. Pubblicare

GitHub → **Actions → Play Store → Run workflow**:
- **track**:
  - `internal` (test interno, immediato);
  - `alpha` (test chiuso);
  - `beta` (test aperto);
  - `production`;
- **listing**: aggiorna anche testi e immagini.

Il `versionCode` viene calcolato da solo: è l'ultimo presente sul Play Store più uno. La prima uscita in produzione passa dalla revisione di Google, che di solito richiede da qualche ora a qualche giorno.
