package com.example.fuel.service

import com.example.fuel.data.local.entity.AnagraficaImpianto
import com.example.fuel.data.local.entity.PrezzoCarburante

/**
 * Parser per i file CSV del MIMIT (separatore |).
 *
 * Formato file:
 *   Riga 1: "Estrazione del YYYY-MM-DD" (metadata, ignorata)
 *   Riga 2: header
 *   Righe successive: dati
 */
object CsvParser {

    /**
     * Parsing del file anagrafica_impianti_attivi.csv
     * Colonne: idImpianto|Gestore|Bandiera|Tipo Impianto|Nome Impianto|Indirizzo|Comune|Provincia|Latitudine|Longitudine
     */
    fun parseAnagrafica(content: String): List<AnagraficaImpianto> {
        val lines = content.lines().filter { it.isNotBlank() }
        // Salta riga metadata e header
        val dataLines = lines.dropWhile { it.startsWith("Estrazi") || it.startsWith("idImpianto") }
            .filter { it.isNotBlank() }

        return dataLines.mapNotNull { line ->
            try {
                val parts = line.split("|")
                if (parts.size < 10) return@mapNotNull null

                AnagraficaImpianto(
                    idImpianto = parts[0].trim().toInt(),
                    gestore = parts[1].trim(),
                    bandiera = parts[2].trim(),
                    tipoImpianto = parts[3].trim(),
                    nomeImpianto = parts[4].trim(),
                    indirizzo = parts[5].trim(),
                    comune = parts[6].trim(),
                    provincia = parts[7].trim(),
                    latitudine = parts[8].trim().replace(",", ".").toDouble(),
                    longitudine = parts[9].trim().replace(",", ".").toDouble()
                )
            } catch (e: Exception) {
                null // skip righe malformate
            }
        }
    }

    /**
     * Parsing del file prezzo_alle_8.csv
     * Colonne: idImpianto|descCarburante|prezzo|isSelf|dtComu
     */
    fun parsePrezzi(content: String): List<PrezzoCarburante> {
        val lines = content.lines().filter { it.isNotBlank() }
        // Salta riga metadata e header
        val dataLines = lines.dropWhile { it.startsWith("Estrazi") || it.startsWith("idImpianto") }
            .filter { it.isNotBlank() }

        return dataLines.mapNotNull { line ->
            try {
                val parts = line.split("|")
                if (parts.size < 5) return@mapNotNull null

                PrezzoCarburante(
                    idImpianto = parts[0].trim().toInt(),
                    descCarburante = parts[1].trim(),
                    prezzo = parts[2].trim().replace(",", ".").toDouble(),
                    isSelf = parts[3].trim() == "1",
                    dtComu = parts[4].trim()
                )
            } catch (e: Exception) {
                null // skip righe malformate
            }
        }
    }
}
