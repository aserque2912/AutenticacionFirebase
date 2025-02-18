package com.adrianserranoquero.autenticacionfirebase.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.FirestoreManager
import com.adrianserranoquero.autenticacionfirebase.data.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(auth: AuthManager, navigateToLogin: () -> Unit, viewModel: HomeViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var showProductDialog by remember { mutableStateOf(false) }
    val user = auth.getCurrentUser()
    val notes by viewModel.notes.observeAsState(emptyList())
    val products by viewModel.products.observeAsState(emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadData() // Solo carga los datos UNA VEZ
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Notas y Productos") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ✅ Scroll en toda la pantalla
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botones de acción
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

            // Sección de Notas
            Text("Notas Guardadas:", style = MaterialTheme.typography.headlineSmall)
            Column {
                notes.distinctBy { it["id"] }.forEach { note -> // ✅ Evita duplicados
                    NoteItem(
                        noteId = note["id"].toString(),
                        title = note["title"].toString(),
                        content = note["content"].toString(),
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección de Productos
            Text("Productos Guardados:", style = MaterialTheme.typography.headlineSmall)
            Column {
                products.distinctBy { it["id"] }.forEach { product -> // ✅ Evita duplicados
                    ProductItem(
                        productId = product["id"].toString(),
                        name = product["name"].toString(),
                        price = product["price"].toString(),
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Espacio extra al final
        }
    }

    // Diálogo para añadir una nota
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Añadir Nota") },
            text = {
                Column {
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Título") })
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Contenido") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addNote(noteTitle, noteContent)
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

    // Diálogo para añadir un producto
    if (showProductDialog) {
        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = { Text("Añadir Producto") },
            text = {
                Column {
                    OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Nombre del Producto") })
                    OutlinedTextField(value = productPrice, onValueChange = { productPrice = it }, label = { Text("Precio") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addProduct(productName, productPrice.toDoubleOrNull() ?: 0.0)
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


@Composable
fun NoteItem(noteId: String, title: String, content: String, viewModel: HomeViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = content, fontSize = 14.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { showEditNoteDialog = true }) {
                    Text("Editar")
                }
                Button(onClick = { showDeleteDialog = true }) {
                    Text("Eliminar")
                }
            }
        }
    }

    if (showEditNoteDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Editar Nota") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") }
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Contenido") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.editNote(noteId, title, content)
                    showEditNoteDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showEditNoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Nota") },
            text = { Text("¿Estás seguro de que deseas eliminar esta nota?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteNote(noteId)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProductItem(productId: String, name: String, price: String, viewModel: HomeViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }
    var products by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Precio: $price €", fontSize = 14.sp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { showEditProductDialog = true }) {
                    Text("Editar")
                }
                Button(onClick = { showDeleteDialog = true }) {
                    Text("Eliminar")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar este producto?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProduct(productId)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showEditProductDialog) {
        var productName by remember { mutableStateOf("") }
        var productPrice by remember { mutableStateOf(0.0) }

        AlertDialog(
            onDismissRequest = { showEditProductDialog = false },
            title = { Text("Editar Producto") },
            text = {
                Column {
                    // Campo para el nombre del producto
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Nombre") }
                    )

                    // Campo para el precio del producto (convierte a Double)
                    OutlinedTextField(
                        value = productPrice.toString(),
                        onValueChange = {
                            // Convierte el valor ingresado a Double
                            productPrice = it.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text("Precio") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Actualiza el producto en Firestore
                    viewModel.editProduct(productId, productName, productPrice)
                    // Cierra el diálogo de edición
                    showEditProductDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showEditProductDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

}



