package com.example.data.repository

import com.example.data.local.BankCardDao
import com.example.data.model.BankCard
import com.example.data.model.CardTransaction
import kotlinx.coroutines.flow.Flow

class BankCardRepository(private val bankCardDao: BankCardDao) {
    val allCards: Flow<List<BankCard>> = bankCardDao.getAllCards()

    fun getCardById(id: Int): Flow<BankCard?> = bankCardDao.getCardById(id)

    fun getTransactionsForCard(cardId: Int): Flow<List<CardTransaction>> = 
        bankCardDao.getTransactionsForCard(cardId)

    suspend fun insertCard(card: BankCard): Long = bankCardDao.insertCard(card)

    suspend fun updateCard(card: BankCard) = bankCardDao.updateCard(card)

    suspend fun deleteCard(card: BankCard) {
        bankCardDao.deleteTransactionsForCard(card.id)
        bankCardDao.deleteCard(card)
    }

    suspend fun insertTransactionUpdateCard(transaction: CardTransaction, card: BankCard) {
        bankCardDao.insertTransaction(transaction)
        val newBalance = if (transaction.type == "INCOME") {
            card.balance + transaction.amount
        } else {
            card.balance - transaction.amount
        }
        bankCardDao.updateCard(card.copy(balance = newBalance))
    }

    suspend fun deleteTransactionUpdateCard(transaction: CardTransaction, card: BankCard) {
        bankCardDao.deleteTransaction(transaction)
        val newBalance = if (transaction.type == "INCOME") {
            card.balance - transaction.amount
        } else {
            card.balance + transaction.amount
        }
        bankCardDao.updateCard(card.copy(balance = newBalance))
    }
}
