package com.example.data.local

import androidx.room.*
import com.example.data.model.BankCard
import com.example.data.model.CardTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BankCardDao {
    @Query("SELECT * FROM bank_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<BankCard>>

    @Query("SELECT * FROM bank_cards WHERE id = :id")
    fun getCardById(id: Int): Flow<BankCard?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: BankCard): Long

    @Update
    suspend fun updateCard(card: BankCard)

    @Delete
    suspend fun deleteCard(card: BankCard)

    @Query("SELECT * FROM card_transactions WHERE cardId = :cardId ORDER BY timestamp DESC")
    fun getTransactionsForCard(cardId: Int): Flow<List<CardTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CardTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: CardTransaction)

    @Query("DELETE FROM card_transactions WHERE cardId = :cardId")
    suspend fun deleteTransactionsForCard(cardId: Int)
}
