package com.adrianserranoquero.autenticacionfirebase.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val firestoreManager = FirestoreManager()

    private val _notes = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val notes: LiveData<List<Map<String, Any>>> = _notes
    private val _products = MutableLiveData<List<Map<String, Any>>>(emptyList())
    val products: LiveData<List<Map<String, Any>>> = _products

    fun loadData() {
        viewModelScope.launch {
            firestoreManager.getNotes { _notes.value = it }
            firestoreManager.getProducts { _products.value = it }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            firestoreManager.addNote(title, content)
            firestoreManager.getNotes { _notes.value = it }
        }
    }

    fun addProduct(name: String, price: Double) {
        viewModelScope.launch {
            firestoreManager.addProduct("prod_${System.currentTimeMillis()}", name, price)
            firestoreManager.getProducts { _products.value = it }
        }
    }

    fun editNote(noteId: String, title: String, content: String){
        viewModelScope.launch {
            firestoreManager.updateNote(noteId, title, content)
            firestoreManager.getNotes { _notes.value = it }
        }
    }

    fun editProduct(productId: String, name: String, price: Double){
        viewModelScope.launch {
            firestoreManager.updateProduct(productId, name, price)
            firestoreManager.getProducts { _products.value = it }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            firestoreManager.deleteNote(noteId)
            firestoreManager.getNotes { _notes.value = it }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            firestoreManager.deleteProduct(productId)
            firestoreManager.getProducts { _products.value = it }
        }
    }
}
