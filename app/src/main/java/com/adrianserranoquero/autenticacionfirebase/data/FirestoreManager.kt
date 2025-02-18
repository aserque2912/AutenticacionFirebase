package com.adrianserranoquero.autenticacionfirebase.data

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()

    // Agregar una nota a la colección "notas"
    fun addNote(title: String, content: String) {
        val note = hashMapOf(
            "title" to title,
            "content" to content
        )

        db.collection("notas")
            .add(note)  // Esto genera un ID automático en Firestore
            .addOnSuccessListener { println("Nota agregada correctamente") }
            .addOnFailureListener { e -> println("Error al agregar la nota: $e") }
    }

    // Obtener todas las notas y evitar valores nulos
    fun getNotes(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("notas")
            .get()
            .addOnSuccessListener { result ->
                val noteList = result.documents.mapNotNull { document ->
                    val noteId = document.id  // Agregar el ID del documento
                    val title = document.getString("title") ?: "Sin título"
                    val content = document.getString("content") ?: "Sin contenido"

                    mapOf("id" to noteId, "title" to title, "content" to content)
                }
                callback(noteList)
            }
            .addOnFailureListener { e ->
                println("Error al obtener notas: $e")
                callback(emptyList())  // Evita que la UI se quede sin actualizar en caso de error
            }
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
                    val productId = document.id
                    val name = document.getString("name") ?: "Sin nombre"
                    val price = document.getDouble("price") ?: 0.0

                    mapOf("id" to productId, "name" to name, "price" to price)
                }
                callback(productList)
            }
            .addOnFailureListener { e -> println("Error al obtener productos: $e") }
    }

    // Modificar una nota en Firestore
    fun updateNote(noteId: String, newTitle: String, newContent: String) {
        val updatedNote = mapOf(
            "title" to newTitle,
            "content" to newContent
        )

        db.collection("notas").document(noteId)
            .update(updatedNote)
            .addOnSuccessListener { println("Nota actualizada correctamente") }
            .addOnFailureListener { e -> println("Error al actualizar la nota: $e") }
    }

    // Eliminar una nota en Firestore
    fun deleteNote(noteId: String) {
        db.collection("notas")
            .document(noteId)
            .delete()
            .addOnSuccessListener { println("Nota eliminada correctamente") }
            .addOnFailureListener { e -> println("Error al eliminar la nota: $e") }
    }


    // Modificar un producto en Firestore
    fun updateProduct(productId: String, newName: String, newPrice: Double) {
        val updatedProduct = mapOf(
            "name" to newName,
            "price" to newPrice
        )

        db.collection("productos").document(productId)
            .update(updatedProduct)
            .addOnSuccessListener { println("Producto actualizado correctamente") }
            .addOnFailureListener { e -> println("Error al actualizar el producto: $e") }
    }

    // Eliminar un producto en Firestore
    fun deleteProduct(productId: String) {
        db.collection("productos").document(productId)
            .delete()
            .addOnSuccessListener { println("Producto eliminado correctamente") }
            .addOnFailureListener { e -> println("Error al eliminar el producto: $e") }
    }
}
