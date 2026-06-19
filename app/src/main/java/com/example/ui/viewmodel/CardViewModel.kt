package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankCard
import com.example.data.model.CardTransaction
import com.example.data.repository.BankCardRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CardViewModel(private val repository: BankCardRepository) : ViewModel() {

    // All available bank accounts / cards
    val cards: StateFlow<List<BankCard>> = repository.allCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Currently selected card ID
    private val _selectedCardId = MutableStateFlow<Int?>(null)
    val selectedCardId: StateFlow<Int?> = _selectedCardId.asStateFlow()

    // Currently selected card object
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedCard: StateFlow<BankCard?> = _selectedCardId
        .flatMapLatest { id ->
            if (id == null) {
                cards.map { it.firstOrNull() }
            } else {
                repository.getCardById(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Historical transactions of the currently selected card
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTransactions: StateFlow<List<CardTransaction>> = selectedCard
        .flatMapLatest { card ->
            if (card == null) {
                flowOf(emptyList())
            } else {
                repository.getTransactionsForCard(card.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Automatically select the first card when list is loaded
        viewModelScope.launch {
            cards.collectLatest { cardList ->
                if (_selectedCardId.value == null && cardList.isNotEmpty()) {
                    _selectedCardId.value = cardList.first().id
                }
            }
        }
    }

    fun selectCard(cardId: Int) {
        _selectedCardId.value = cardId
    }

    fun deleteSelectedCard() {
        val currentCard = selectedCard.value ?: return
        viewModelScope.launch {
            repository.deleteCard(currentCard)
            // Reset selection to the first available card, or null
            val remaining = cards.value.filter { it.id != currentCard.id }
            _selectedCardId.value = remaining.firstOrNull()?.id
        }
    }

    fun addCard(
        bankName: String,
        cardHolder: String,
        cardNumber: String,
        cardType: String,
        balance: Double,
        expiryDate: String,
        styleIndex: Int,
        colorHex: String
    ) {
        viewModelScope.launch {
            // Mask or format the card number beautifully
            val formattedNum = formatCardNumber(cardNumber)
            val newCard = BankCard(
                bankName = bankName.trim(),
                cardHolder = cardHolder.trim().uppercase(),
                cardNumber = formattedNum,
                cardType = cardType,
                balance = balance,
                expiryDate = expiryDate,
                styleIndex = styleIndex,
                colorHex = colorHex
            )
            val newId = repository.insertCard(newCard)
            // Auto select the newly added card
            _selectedCardId.value = newId.toInt()
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String, // "INCOME" or "EXPENSE"
        category: String
    ) {
        val currentCard = selectedCard.value ?: return
        viewModelScope.launch {
            val transaction = CardTransaction(
                cardId = currentCard.id,
                title = title.trim(),
                amount = amount,
                type = type,
                category = category
            )
            repository.insertTransactionUpdateCard(transaction, currentCard)
        }
    }

    fun deleteTransaction(transaction: CardTransaction) {
        val currentCard = selectedCard.value ?: return
        viewModelScope.launch {
            repository.deleteTransactionUpdateCard(transaction, currentCard)
        }
    }

    private fun formatCardNumber(raw: String): String {
        // Clean all non-digits
        val cleaned = raw.replace(Regex("\\D"), "")
        if (cleaned.length < 4) {
            return "•••• •••• •••• 1234"
        }
        val last4 = cleaned.takeLast(4)
        return "•••• •••• •••• $last4"
    }

    // Factory Class pattern
    class Factory(private val repository: BankCardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
