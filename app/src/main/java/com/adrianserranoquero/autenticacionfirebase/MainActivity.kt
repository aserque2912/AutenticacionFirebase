package com.adrianserranoquero.autenticacionfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.adrianserranoquero.autenticacionfirebase.data.AuthManager
import com.adrianserranoquero.autenticacionfirebase.navegacion.Navegacion
import com.adrianserranoquero.autenticacionfirebase.ui.theme.FirebaseCurso2425Theme
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    val auth = AuthManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Firebase.analytics
        setContent {
            FirebaseCurso2425Theme {
                Navegacion(auth)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
    }
}