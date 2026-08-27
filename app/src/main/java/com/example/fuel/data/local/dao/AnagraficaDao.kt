package com.example.fuel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fuel.data.local.entity.AnagraficaImpianto

@Dao
interface AnagraficaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(impianti: List<AnagraficaImpianto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(impianto: AnagraficaImpianto)

    @Query("SELECT COUNT(*) FROM anagrafica_impianti")
    suspend fun count(): Int

    @Query("DELETE FROM anagrafica_impianti")
    suspend fun deleteAll()

    @Query("SELECT * FROM anagrafica_impianti WHERE id_impianto = :id")
    suspend fun getById(id: Int): AnagraficaImpianto?

    @Query("SELECT * FROM anagrafica_impianti WHERE comune LIKE '%' || :comune || '%' OR indirizzo LIKE '%' || :indirizzo || '%'")
    suspend fun searchByAddress(comune: String, indirizzo: String): List<AnagraficaImpianto>

    /**
     * Cerca impianti solo per comune (es. per ricerca città).
     * Più preciso di [searchByAddress] perché non cerca nell'indirizzo stradale.
     */
    @Query("SELECT * FROM anagrafica_impianti WHERE comune LIKE '%' || :comune || '%'")
    suspend fun searchByComune(comune: String): List<AnagraficaImpianto>

    @Query("SELECT * FROM anagrafica_impianti WHERE id_impianto IN (SELECT DISTINCT id_impianto FROM prezzi_carburanti)")
    suspend fun getAllWithPrices(): List<AnagraficaImpianto>
}
