# Fuel

App Android per la ricerca di distributori di carburante in Italia,
basata sui dati ufficiali **MIMIT** (Ministero delle Imprese e del Made in Italy).

## Funzionalità

- **Download automatico** dei dati MIMIT (prezzi e anagrafica impianti)
- **Ricerca per carburante**: Benzina, Diesel, GPL, Metano
- **Ricerca per posizione** o per indirizzo con raggio configurabile (1–25 km)
- **Mappa OpenStreetMap** con marker colorati
- **Risultati ordinati per prezzo** crescente
- **Navigazione** verso il distributore via Google Maps
- **Material Design 3 / Material You** con colori dinamici
- **Tema selezionabile**: Sistema / Chiaro / Scuro (persistito)

## Stack Tecnologico

| Componente | Tecnologia |
|---|---|
| Linguaggio | **Kotlin** |
| UI | **Jetpack Compose** + Material Design 3 |
| Architettura | MVVM (ViewModel + StateFlow) |
| Database | **Room** (SQLite) con KSP |
| Mappe | **osmdroid** (OpenStreetMap, tile Thunderforest) |
| Download | OkHttp |
| Posizione | Google Play Services Location |
| Preferenze | DataStore (tema e ultimo download) |

## Requisiti

- Android Studio (o una recente distribuzione di Android SDK)
- JDK 17
- Android SDK con API 35
- Una chiave API **Thunderforest** gratuita (vedi sezione Configurazione)

## Configurazione: chiave API Thunderforest

La mappa usa i tile di [Thunderforest](https://www.thunderforest.com/) (basati su OpenStreetMap),
che richiedono una chiave API personale. **La chiave non è inclusa nel repository** per motivi di sicurezza.

Per ottenere una chiave gratuita:

1. Registrati su https://thunderforest.com/maps/apis/
2. Crea una chiave per il prodotto landscape

Per configurarla, aggiungi la seguente riga al file **`local.properties`** (non versionato),
nel percorso radice del progetto:

```properties
FUEL_THUNDERFOREST_API_KEY=la_tua_chiave
```

In alternativa, la chiave può essere fornita anche tramite:

- una proprietà di progetto in `gradle.properties`
  (`FUEL_THUNDERFOREST_API_KEY=...`)
- una variabile d'ambiente `FUEL_THUNDERFOREST_API_KEY`

La precedenza è: `gradle.properties` -> `local.properties` -> variabile d'ambiente.
Senza la chiave l'app compila correttamente ma la **mappa resta vuota** (nessun tile scaricato).

## Build e installazione

Dal terminale, nella radice del progetto:

```bash
# Esegue l'APK di debug su un dispositivo/emulatore collegato
./gradlew :app:installDebug

# Oppure genera solo l'APK di debug
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release
./gradlew :app:assembleRelease
```

Dopo ogni modifica al codice o alle risorse, ricompila con lo stesso comando.

> Nota: la firma della build `release` è configurabile via `gradle.properties`
> (`FUEL_KEYSTORE_PATH`, `FUEL_KEYSTORE_PASSWORD`, `FUEL_KEY_ALIAS`, `FUEL_KEY_PASSWORD`)
> e viene applicata solo se il file keystore esiste. In assenza di keystore la build
> release viene generata non firmata.

## Struttura del progetto

```
app/src/main/java/com/example/fuel/
├── data/            # Room (entity, DAO, database) e repository
├── service/         # Download e parsing dei CSV MIMIT
├── ui/              # Compose (screens, viewmodel, theme)
└── util/            # Preferenze (DataStore) e posizione
```

## Dati

I dati provengono dal Ministero delle Imprese e del Made in Italy (MIMIT):
- [Prezzi carburanti](https://www.mimit.gov.it/images/exportCSV/prezzo_alle_8.csv)
- [Anagrafica impianti](https://www.mimit.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv)

I dati vengono aggiornati quotidianamente (pubblicazione dopo le 8:00).

## Licenza

MIT © 2026 [ravem](https://github.com/ravem)

```
MIT License

Copyright (c) 2026 ravem

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Componenti utilizzati

- **Jetpack Compose / Room / OkHttp** — Apache License 2.0
- **osmdroid** — Apache License 2.0
- **Google Play Services** — Apache License 2.0
- **OpenStreetMap** — © OpenStreetMap contributors (ODbL)
- **Dati MIMIT** — CC BY 4.0
