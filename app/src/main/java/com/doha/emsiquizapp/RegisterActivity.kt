package com.doha.emsiquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginTextView = findViewById<TextView>(R.id.loginTextView)

        registerButton.setOnClickListener {
            val emailStr = emailEditText.text.toString().trim()
            val passwordStr = passwordEditText.text.toString().trim()

            if (emailStr.isNotEmpty() && passwordStr.isNotEmpty()) {
                // Tentative de création de compte Firebase
                auth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Succès Firebase -> Inscription Supabase
                            registerInSupabase(emailStr, passwordStr)
                        } else {
                            // Si l'e-mail existe déjà dans Firebase, on tente quand même Supabase
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Firebase account exists, syncing with Supabase...", Toast.LENGTH_SHORT).show()
                                registerInSupabase(emailStr, passwordStr)
                            } else {
                                Toast.makeText(this, "Firebase Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loginTextView.setOnClickListener {
            finish()
        }
    }

    private fun registerInSupabase(emailStr: String, passwordStr: String) {
        lifecycleScope.launch {
            try {
                // Tentative d'inscription Supabase
                supabase.auth.signUpWith(Email) {
                    email = emailStr
                    password = passwordStr
                }

                Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, FaceRegistrationActivity::class.java))
                finish()

            } catch (e: Exception) {
                // C'est ici que ça bloquera si votre SUPABASE_URL dans build.gradle n'est pas la bonne !
                Toast.makeText(this@RegisterActivity, "Supabase Error: ${e.message}. Check your URL in build.gradle!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
