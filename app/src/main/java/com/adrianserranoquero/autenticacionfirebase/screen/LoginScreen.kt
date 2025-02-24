package com.adrianserranoquero.autenticacionfirebase.screen


import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrianserranoquero.autenticacionfirebase.R
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.data.AuthRes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    auth: AuthManager,
    navigateToSignUp: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleSignLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (val account =
            auth.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))) {
            is AuthRes.Success -> {
                val credential = GoogleAuthProvider.getCredential(account.data?.idToken, null)
                scope.launch {
                    val firebaseUser = auth.googleSignInCredential(credential)
                    when (firebaseUser) {
                        is AuthRes.Success -> {
                            Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                        }
                        is AuthRes.Error -> {
                            Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            is AuthRes.Error -> {
                Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Image(
                painter = painterResource(id = R.drawable.diario),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Gestión de Notas y Productos",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                signIn(email, password, context, auth, navigateToHome)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Iniciar Sesión")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Sesión".uppercase())
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        modifier = Modifier.clickable { navigateToForgotPassword() },
                        style = TextStyle(
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(20.dp))

                    SocialMediaButton(
                        onClick = {
                            scope.launch {
                                signAnonimous(auth, navigateToHome, context)
                            }
                        },
                        text = "Continuar como invitado",
                        icon = R.drawable.ic_incognito,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    SocialMediaButton(
                        onClick = {
                            auth.signInWithGoogle(googleSignLauncher)
                        },
                        text = "Continuar con Google",
                        icon = R.drawable.ic_google,
                        color = Color(0xFFF1F1F1),
                        textColor = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿No tienes cuenta? Regístrate",
                modifier = Modifier.clickable { navigateToSignUp() },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}


suspend fun signAnonimous(auth: AuthManager, navigateToHome: () -> Unit, context: Context) {
    val res = withContext(Dispatchers.IO) {
        auth.signInAnonymously()
    }
    when (res) {
        is AuthRes.Success -> {
            Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
        is AuthRes.Error -> {
            Toast.makeText(context, res.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun signIn(email: String, password: String, context: Context, auth: AuthManager, navigateToHome: () -> Unit) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        val result =
            withContext(Dispatchers.IO) {
                auth.signInWithEmailAndPassword(email, password)
            }
        when (result) {
            is AuthRes.Success -> {
                Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            is AuthRes.Error -> {
                Toast.makeText(context, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

    } else{
        Toast.makeText(context, "Email y password tienen que estar rellenos", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SocialMediaButton(onClick: () -> Unit, text: String, icon: Int, color: Color, textColor: Color) {
    var click by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(start = 40.dp, end = 40.dp).clickable { click = !click },
        shape = RoundedCornerShape(50),
        border = BorderStroke(width = 1.dp, color = if(icon == R.drawable.ic_incognito) color else Color.Gray),
        color = color
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                modifier = Modifier.size(24.dp),
                contentDescription = text,
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "$text", color = textColor)
            click = true
        }
    }
}
