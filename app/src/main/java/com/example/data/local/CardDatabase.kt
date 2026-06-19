package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BankCard
import com.example.data.model.CardTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BankCard::class, CardTransaction::class], version = 1, exportSchema = false)
abstract class CardDatabase : RoomDatabase() {
    abstract fun bankCardDao(): BankCardDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getDatabase(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.bankCardDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: BankCardDao) {
                // Pre-populate with 2 template cards for immediate aesthetic satisfaction
                // Card 1: Chase Sapphire Reserve style
                val card1Id = dao.insertCard(
                    BankCard(
                        bankName = "Chase Bank",
                        cardHolder = "ALEX MERCER",
                        cardNumber = "•••• •••• •••• 4892",
                        cardType = "Visa",
                        balance = 5420.50,
                        currencySymbol = "$",
                        expiryDate = "09/30",
                        colorHex = "#0C2340", // Deep Navy
                        styleIndex = 0
                    )
                ).toInt()

                // Transactions for Card 1
                dao.insertTransaction(
                    CardTransaction(
                        cardId = card1Id,
                        title = "Apple Store purchase",
                        amount = 1299.00,
                        type = "EXPENSE",
                        category = "Shopping"
                    )
                )
                dao.insertTransaction(
                    CardTransaction(
                        cardId = card1Id,
                        title = "Salary Deposit",
                        amount = 3500.00,
                        type = "INCOME",
                        category = "Salary"
                    )
                )
                dao.insertTransaction(
                    CardTransaction(
                        cardId = card1Id,
                        title = "Starbucks Coffee",
                        amount = 6.75,
                        type = "EXPENSE",
                        category = "Food"
                    )
                )

                // Card 2: Apple Card style / Premium silver glass
                val card2Id = dao.insertCard(
                    BankCard(
                        bankName = "Goldman Sachs",
                        cardHolder = "ALEX MERCER",
                        cardNumber = "•••• •••• •••• 9201",
                        cardType = "Mastercard",
                        balance = 12480.12,
                        currencySymbol = "$",
                        expiryDate = "11/29",
                        colorHex = "#CCCCCC", // Silver/Gray Gradient
                        styleIndex = 1
                    )
                ).toInt()

                // Transactions for Card 2
                dao.insertTransaction(
                    CardTransaction(
                        cardId = card2Id,
                        title = "Netflix Subscription",
                        amount = 15.49,
                        type = "EXPENSE",
                        category = "Entertainment"
                    )
                )
                dao.insertTransaction(
                    CardTransaction(
                        cardId = card2Id,
                        title = "Freelance payment",
                        amount = 850.00,
                        type = "INCOME",
                        category = "Salary"
                    )
                )
            }
        }
    }
}
