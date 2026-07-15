package com.example.fuel.data.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

/**
 * Modello che combina anagrafica impianto + prezzo per un dato carburante.
 * Usato per mostrare i risultati della ricerca.
 * Mappato da una query JOIN di Room (PrezzoDao).
 */
data class ImpiantoConPrezzo(
    // Campi da anagrafica_impianti
    @ColumnInfo(name = "id_impianto")
    val idImpianto: Int,

    @ColumnInfo(name = "gestore")
    val gestore: String,

    @ColumnInfo(name = "bandiera")
    val bandiera: String,

    @ColumnInfo(name = "tipo_impianto")
    val tipoImpianto: String,

    @ColumnInfo(name = "nome_impianto")
    val nomeImpianto: String,

    @ColumnInfo(name = "indirizzo")
    val indirizzo: String,

    @ColumnInfo(name = "comune")
    val comune: String,

    @ColumnInfo(name = "provincia")
    val provincia: String,

    @ColumnInfo(name = "latitudine")
    val latitudine: Double,

    @ColumnInfo(name = "longitudine")
    val longitudine: Double,

    // Campi da prezzi_carburanti
    @ColumnInfo(name = "desc_carburante")
    val descCarburante: String,

    @ColumnInfo(name = "prezzo")
    val prezzo: Double,

    @ColumnInfo(name = "is_self")
    val isSelf: Boolean,

    @ColumnInfo(name = "dt_comu")
    val dtComu: String
) {
    /** Prezzo formattato con valuta */
    val prezzoFormattato: String
        get() = String.format("%.3f €/L", prezzo)

    /** Distanza approssimativa dalla posizione (km) - NON mappata da Room */
    @Ignore
    var distanzaKm: Double? = null

    /** Label per self/servito */
    val modalita: String
        get() = if (isSelf) "Self" else "Servito"
}
