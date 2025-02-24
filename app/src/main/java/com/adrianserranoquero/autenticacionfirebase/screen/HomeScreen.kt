package com.adrianserranoquero.autenticacionfirebase.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.HomeViewModel
import kotlinx.coroutines.launch
import androidx.compose.animation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar notas y productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, "Limpiar búsqueda")
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

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

    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf("none") }

    var showTotalValue by remember { mutableStateOf(false) }

    val totalValue = products.sumOf {
        it["price"].toString().toDoubleOrNull() ?: 0.0
    }

    val filteredNotes = notes.filter { note ->
        note["title"].toString().contains(searchQuery, ignoreCase = true) ||
                note["content"].toString().contains(searchQuery, ignoreCase = true)
    }

    val filteredProducts = when (currentSort) {
        "price_asc" -> products
            .filter { product ->
                product["name"].toString().contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it["price"].toString().toDoubleOrNull() ?: 0.0 }

        "price_desc" -> products
            .filter { product ->
                product["name"].toString().contains(searchQuery, ignoreCase = true)
            }
            .sortedByDescending { it["price"].toString().toDoubleOrNull() ?: 0.0 }

        else -> products
            .filter { product ->
                product["name"].toString().contains(searchQuery, ignoreCase = true)
            }
    }

    LaunchedEffect(Unit) {
        try {
            // Asegurarse de que el usuario está autenticado antes de cargar datos
            auth.getCurrentUser()?.let {
                viewModel.loadData()
            } ?: run {
                // Si no hay usuario, navegar al login
                navigateToLogin()
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error al cargar los datos: ${e.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    auth.signOut()
                    navigateToLogin()
                    showDialog = false
                }) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                title = "Gestión de Notas y Productos",
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onLogoutClick = { showDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = { showTotalValue = !showTotalValue },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ver total"
                    )
                    if (!showTotalValue) {
                        Text("€", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Mostrar el valor total si está activado
            AnimatedVisibility(
                visible = showTotalValue,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Resumen de Productos",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Total de productos: ${products.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Importe total: ${String.format("%.2f", totalValue)}€",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showNoteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Nota")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Nota")
                }

                Button(
                    onClick = { showProductDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Producto")
                }
            }

            // Botón de ordenación para productos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showSortMenu = true }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Ordenar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ordenar")
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin ordenar") },
                        onClick = {
                            currentSort = "none"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Precio: Menor a mayor") },
                        onClick = {
                            currentSort = "price_asc"
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Precio: Mayor a menor") },
                        onClick = {
                            currentSort = "price_desc"
                            showSortMenu = false
                        }
                    )
                }
            }

            // Mostrar resultados filtrados
            Text(
                "Notas Guardadas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            if (filteredNotes.isEmpty() && searchQuery.isNotEmpty()) {
                Text(
                    "No se encontraron notas",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Column {
                filteredNotes.forEach { note ->
                    NoteItem(
                        noteId = note["id"].toString(),
                        title = note["title"].toString(),
                        content = note["content"].toString(),
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Productos Guardados",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            if (filteredProducts.isEmpty() && searchQuery.isNotEmpty()) {
                Text(
                    "No se encontraron productos",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Vista de productos en grid o lista
            Column {
                filteredProducts.forEach { product ->
                    val productId = product["id"].toString()
                    val productName = product["name"].toString()
                    val productPrice = product["price"].toString()

                    ProductItem(
                        productId = productId,
                        name = productName,
                        price = productPrice,
                        viewModel = viewModel
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Diálogo para añadir una nota
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                // Limpiar campos al cerrar
                noteTitle = ""
                noteContent = ""
                showNoteDialog = false
            },
            title = { Text("Añadir Nota") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título") })
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Contenido") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (noteTitle.isNotEmpty() && noteContent.isNotEmpty()) {
                        viewModel.addNote(noteTitle, noteContent)
                        // Limpiar campos después de guardar
                        noteTitle = ""
                        noteContent = ""
                        showNoteDialog = false
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Por favor, rellena todos los campos")
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // Limpiar campos al cancelar
                    noteTitle = ""
                    noteContent = ""
                    showNoteDialog = false
                }) {
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
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Nombre del Producto") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Precio") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (productName.isNotEmpty() && productPrice.isNotEmpty()) {
                        val price = productPrice.toDoubleOrNull() ?: 0.0
                        viewModel.addProduct(productName, price)
                        productName = ""
                        productPrice = ""
                        showProductDialog = false
                    }
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

    // Inicializar los estados con los valores actuales usando remember(title, content)
    var editedTitle by remember(title) { mutableStateOf(title) }
    var editedContent by remember(content) { mutableStateOf(content) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showEditNoteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }

    if (showEditNoteDialog) {
        AlertDialog(
            onDismissRequest = { showEditNoteDialog = false },
            title = { Text("Editar Nota") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Título") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        label = { Text("Contenido") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editedTitle.isNotEmpty() && editedContent.isNotEmpty()) {
                        viewModel.editNote(noteId, editedTitle, editedContent)
                        showEditNoteDialog = false
                    }
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
fun ProductItem(
    productId: String,
    name: String,
    price: String,
    viewModel: HomeViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf(false) }

    // Inicializar los estados con los valores actuales
    var editedProductName by remember(name) { mutableStateOf(name) }
    var editedProductPrice by remember(price) { mutableStateOf(price) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Precio: ${String.format("%.2f", price.toDoubleOrNull() ?: 0.0)} €",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showEditProductDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
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
        AlertDialog(
            onDismissRequest = { showEditProductDialog = false },
            title = { Text("Editar Producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedProductName,
                        onValueChange = { editedProductName = it },
                        label = { Text("Nombre") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedProductPrice,
                        onValueChange = { editedProductPrice = it },
                        label = { Text("Precio") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val price = editedProductPrice.toDoubleOrNull() ?: 0.0
                    viewModel.editProduct(productId, editedProductName, price)
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



