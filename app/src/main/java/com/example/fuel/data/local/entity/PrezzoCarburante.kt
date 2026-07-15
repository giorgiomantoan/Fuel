package com.example.fuel.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity Room per i prezzi dei carburanti.
 * Corrisponde al file prezzo_alle_8.csv (separatore |)
 *
 * id univoco auto-generato perché il CSV può avere più righe per lo stesso impianto
 * (diversi carburanti, self/servito)
 */
@Entity(
    tableName = "prezzi_carburanti",
    indices = [
        Index(value = ["id_impianto"]),
        Index(value = ["desc_carburante"]),
        Index(value = ["id_impianto", "desc_carburante", "is_self"], unique = false)
    ]
)
data class PrezzoCarburante(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "id_impianto")
    val idImpianto: Int,

    @ColumnInfo(name = "desc_carburante")
    val descCarburante: String,

    @ColumnInfo(name = "prezzo")
    val prezzo: Double,

    @ColumnInfo(name = "is_self")
    val isSelf: Boolean,   // true = self service, false = servito

    @ColumnInfo(name = "dt_comu")
    val dtComu: String
)
