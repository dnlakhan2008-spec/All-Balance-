package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "bank_cards")
data class BankCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val cardHolder: String,
    val cardNumber: String,
    val cardType: String,
    val balance: Double,
    val currencySymbol: String = "$",
    val expiryDate: String = "12/28",
    val colorHex: String = "#1A1A24",
    val styleIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "card_transactions")
data class CardTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // "Food", "Entertainment", "Utilities", "Salary", etc.
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
