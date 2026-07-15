package com.example.fuel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fuel.data.local.entity.PrezzoCarburante
import com.example.fuel.data.model.ImpiantoConPrezzo

@Dao
interface PrezzoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prezzi: List<PrezzoCarburante>)

    @Query("SELECT COUNT(*) FROM prezzi_carburanti")
    suspend fun count(): Int

    @Query("DELETE FROM prezzi_carburanti")
    suspend fun deleteAll()

    /**
     * Ottiene i tipi di carburante principali (Benzina, Diesel, GPL, Metano).
     * Usa LIKE per catturare le varianti (es. "Diesel HVO", "Benzina 100 ottani").
     */
    @Query("""
        SELECT DISTINCT 
            CASE 
                WHEN desc_carburante LIKE 'Benzina%' THEN 'Benzina'
                WHEN desc_carburante LIKE '%Diesel%' OR desc_carburante = 'Gasolio' OR desc_carburante LIKE 'Blue%' THEN 'Diesel'
                WHEN desc_carburante = 'GPL' THEN 'GPL'
                WHEN desc_carburante = 'Metano' THEN 'Metano'
            END
        FROM prezzi_carburanti
        WHERE 
            desc_carburante LIKE 'Benzina%'
            OR desc_carburante LIKE '%Diesel%'
            OR desc_carburante = 'Gasolio'
            OR desc_carburante LIKE 'Blue%'
            OR desc_carburante = 'GPL'
            OR desc_carburante = 'Metano'
        ORDER BY 
            CASE 
                WHEN desc_carburante LIKE 'Benzina%' THEN 1
                WHEN desc_carburante LIKE '%Diesel%' OR desc_carburante = 'Gasolio' OR desc_carburante LIKE 'Blue%' THEN 2
                WHEN desc_carburante = 'GPL' THEN 3
                WHEN desc_carburante = 'Metano' THEN 4
            END
    """)
    suspend fun getTipiCarburante(): List<String>

    /**
     * Ottiene i LIKE pattern per un dato tipo di carburante.
     */
    fun getFuelPattern(tipo: String): String {
        return when (tipo) {
            "Benzina" -> "Benzina%"
            "Diesel" -> "%Diesel%"
            "Gasolio" -> "Gasolio"
            "GPL" -> "GPL"
            "Metano" -> "Metano"
            else -> "%$tipo%"
        }
    }

    /**
     * Cerca distributori per LIKE pattern sul carburante.
     * Prende il prezzo più basso per impianto (self se disponibile, altrimenti servito).
     * JOIN diretto senza filtro is_self (include GPL, Metano che sono solo servito).
     */
    @Query("""
        SELECT a.*, p.desc_carburante, p.prezzo, p.is_self, p.dt_comu
        FROM anagrafica_impianti a
        INNER JOIN (
            SELECT id_impianto, desc_carburante, MIN(prezzo) as prezzo, is_self, dt_comu
            FROM prezzi_carburanti
            WHERE (desc_carburante LIKE :pattern OR desc_carburante = :exact OR (:pattern2 IS NOT NULL AND desc_carburante LIKE :pattern2))
            GROUP BY id_impianto
        ) p ON a.id_impianto = p.id_impianto
        ORDER BY p.prezzo ASC
    """)
    suspend fun searchByFuelTypeSelf(
        pattern: String,
        exact: String = "",
        pattern2: String? = null
    ): List<ImpiantoConPrezzo>

    /**
     * Cerca distributori per LIKE pattern (self e servito) con JOIN diretto.
     * Prende il prezzo più basso per impianto.
     */
    @Query("""
        SELECT a.*, p.desc_carburante, p.prezzo, p.is_self, p.dt_comu
        FROM anagrafica_impianti a
        INNER JOIN (
            SELECT id_impianto, desc_carburante, MIN(prezzo) as prezzo, is_self, dt_comu
            FROM prezzi_carburanti
            WHERE (desc_carburante LIKE :pattern OR desc_carburante = :exact OR (:pattern2 IS NOT NULL AND desc_carburante LIKE :pattern2))
            GROUP BY id_impianto
        ) p ON a.id_impianto = p.id_impianto
        ORDER BY p.prezzo ASC
    """)
    suspend fun searchByFuelTypeAll(
        pattern: String,
        exact: String = "",
        pattern2: String? = null
    ): List<ImpiantoConPrezzo>
}
