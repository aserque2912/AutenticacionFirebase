package com.adrianserranoquero.autenticacionfirebase.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adrianserranoquero.autenticacionfirebase.R
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.FirestoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(auth: AuthManager, navigateToLogin: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    val user = auth.getCurrentUser()

    val firestoreManager = FirestoreManager()

    var notes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var products by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        firestoreManager.getNotes { notes = it }
        firestoreManager.getProducts { products = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Notas y Productos") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { showNoteDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Nota")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Nota")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { showProductDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Producto")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir Producto")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Notas Guardadas:", style = MaterialTheme.typography.headlineSmall)
            notes.forEach { note ->
                Text("Título: ${note["title"]}, Contenido: ${note["content"]}")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Productos Guardados:", style = MaterialTheme.typography.headlineSmall)
            products.forEach { product ->
                Text("Nombre: ${product["name"]}, Precio: ${product["price"]}")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    auth.signOut()
                    navigateToLogin()
                    showDialog = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Añadir Nota") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título") }
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Contenido") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    firestoreManager.addNote(noteTitle, noteContent)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Nota añadida")
                        firestoreManager.getNotes { notes = it }
                    }
                    showNoteDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showNoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showProductDialog) {
        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = { Text("Añadir Producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Nombre del Producto") }
                    )
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Precio") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    firestoreManager.addProduct("prod_${System.currentTimeMillis()}", productName, productPrice.toDoubleOrNull() ?: 0.0)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Producto añadido")
                        firestoreManager.getProducts { products = it }
                    }
                    showProductDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showProductDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
