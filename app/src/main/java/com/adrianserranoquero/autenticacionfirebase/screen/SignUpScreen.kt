package com.adrianserranoquero.autenticacionfirebase.screen


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.AuthRes
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


@Composable
fun SignUpScreen(auth: AuthManager, navigateToLogin: () -> Unit, navigateToHome: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                signUp(navigateToHome, auth, email, password, context)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Registrarse")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrarse".uppercase())
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿Ya tienes cuenta? Inicia sesión",
                modifier = Modifier.clickable { navigateToLogin() },
                style = TextStyle(
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}


suspend fun signUp(navigateToHome: () -> Unit, auth: AuthManager, email: String, password: String, context: Context) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        // Validar la contraseña
        if (password.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        when (
            val result = withContext(Dispatchers.IO){
                auth.createUserWithEmailAndPassword(email, password)
            }
        ) {
            is AuthRes.Success -> {
                val userId = auth.getCurrentUser()?.uid
                if (userId != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            val db = FirebaseFirestore.getInstance()
                            val userDoc = db.collection("users").document(userId)
                            
                            userDoc.set(
                                mapOf(
                                    "email" to email,
                                    "notes" to emptyList<Map<String, Any>>(),
                                    "products" to emptyList<Map<String, Any>>()
                                )
                            ).await()
                        }
                        
                        Toast.makeText(context, "Usuario creado correctamente", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al inicializar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Error al crear usuario", Toast.LENGTH_SHORT).show()
                }
            }
            is AuthRes.Error -> {
                val errorMessage = when {
                    result.errorMessage.contains("The email address is already in use") -> 
                        "Este correo electrónico ya está registrado"
                    result.errorMessage.contains("The email address is badly formatted") -> 
                        "El formato del correo electrónico no es válido"
                    result.errorMessage.contains("The given password is invalid") -> 
                        "La contraseña debe tener al menos 6 caracteres"
                    else -> "Error al crear usuario: ${result.errorMessage}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
    }
}

