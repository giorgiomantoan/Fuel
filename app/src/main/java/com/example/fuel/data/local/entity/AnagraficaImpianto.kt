package com.example.fuel.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Room per l'anagrafica degli impianti di distribuzione carburanti.
 * Corrisponde al file anagrafica_impianti_attivi.csv (separatore |)
 */
@Entity(
    tableName = "anagrafica_impianti",
    indices = [
        Index(value = ["comune"]),
        Index(value = ["provincia"]),
        Index(value = ["bandiera"])
    ]
)
data class AnagraficaImpianto(
    @PrimaryKey
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
    val longitudine: Double
)
