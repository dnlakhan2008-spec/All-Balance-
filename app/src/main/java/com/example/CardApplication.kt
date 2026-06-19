package com.example

import android.app.Application
import com.example.data.local.CardDatabase
import com.example.data.repository.BankCardRepository

class CardApplication : Application() {
    val database by lazy { CardDatabase.getDatabase(this) }
    val repository by lazy { BankCardRepository(database.bankCardDao()) }
}
