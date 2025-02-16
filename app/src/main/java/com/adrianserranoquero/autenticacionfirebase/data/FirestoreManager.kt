package com.adrianserranoquero.autenticacionfirebase.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    // Agregar una nota a la colección "notas"
    fun addNote(noteId: String, title: String, content: String) {
        val note = hashMapOf(
            "title" to title,
            "content" to content
        )

        db.collection("notas").document(noteId)
            .set(note)
            .addOnSuccessListener { println("Nota agregada correctamente") }
            .addOnFailureListener { e -> println("Error al agregar la nota: $e") }
    }

    // Obtener todas las notas y evitar valores nulos
    fun getNotes(callback: (List<Map<String, String>>) -> Unit) {
        db.collection("notas")
            .get()
            .addOnSuccessListener { result ->
                val noteList = result.documents.mapNotNull { document ->
                    val title = document.getString("title") ?: "Sin título"
                    val content = document.getString("content") ?: "Sin contenido"

                    if (title != "Sin título" || content != "Sin contenido") {
                        mapOf("title" to title, "content" to content)
                    } else {
                        null // Ignora notas vacías
                    }
                }
                callback(noteList)
            }
            .addOnFailureListener { e -> println("Error al obtener las notas: $e") }
    }

    // Agregar un producto a la colección "productos"
    fun addProduct(productId: String, name: String, price: Double) {
        val product = hashMapOf(
            "name" to name,
            "price" to price
        )

        db.collection("productos").document(productId)
            .set(product)
            .addOnSuccessListener { println("Producto agregado correctamente") }
            .addOnFailureListener { e -> println("Error al agregar producto: $e") }
    }

    // Obtener todos los productos y evitar valores nulos
    fun getProducts(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("productos")
            .get()
            .addOnSuccessListener { result ->
                val productList = result.documents.mapNotNull { document ->
                    val name = document.getString("name") ?: "Sin nombre"
                    val price = document.getDouble("price") ?: 0.0

                    if (name != "Sin nombre" || price != 0.0) {
                        mapOf("name" to name, "price" to price)
                    } else {
                        null // Ignora productos vacíos
                    }
                }
                callback(productList)
            }
            .addOnFailureListener { e -> println("Error al obtener productos: $e") }
    }
}
