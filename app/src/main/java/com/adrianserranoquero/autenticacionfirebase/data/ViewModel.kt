package com.adrianserranoquero.autenticacionfirebase.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val firestoreManager = FirestoreManager()

    var notes = mutableStateOf<List<Map<String, Any>>>(emptyList())
    var products = mutableStateOf<List<Map<String, Any>>>(emptyList())

    fun loadData() {
        viewModelScope.launch {
            firestoreManager.getNotes { notes.value = it }
            firestoreManager.getProducts { products.value = it }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            firestoreManager.addNote(title, content)
            firestoreManager.getNotes { notes.value = it }
        }
    }

    fun addProduct(name: String, price: Double) {
        viewModelScope.launch {
            firestoreManager.addProduct("prod_${System.currentTimeMillis()}", name, price)
            firestoreManager.getProducts { products.value = it }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            firestoreManager.deleteNote(noteId)
            firestoreManager.getNotes { notes.value = it }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            firestoreManager.deleteProduct(productId)
            firestoreManager.getProducts { products.value = it }
        }
    }
}
