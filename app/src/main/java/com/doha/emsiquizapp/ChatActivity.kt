package com.doha.emsiquizapp

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var rvChat: RecyclerView

    // On initialise le modèle de façon 'lazy' pour éviter un crash immédiat au lancement
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rvChat)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnSend)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        if (messages.isEmpty()) {
            addMessage("Bonjour ! Je suis ton assistant EMSI. Comment puis-je t'aider aujourd'hui ?", false)
        }

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                addMessage(userText, true)
                etMessage.text.clear()
                getResponseFromAI(userText)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }

    private fun getResponseFromAI(userPrompt: String) {
        lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(userPrompt)
                val responseText = response.text
                if (responseText != null) {
                    addMessage(responseText, false)
                } else {
                    addMessage("Désolé, je n'ai pas pu générer de réponse.", false)
                }
            } catch (e: Throwable) {
                Log.e("ChatActivity", "Erreur Gemini AI", e)
                addMessage("Erreur technique de l'IA : ${e.localizedMessage}", false)
                Toast.makeText(this@ChatActivity, "Vérifiez votre clé API et votre connexion", Toast.LENGTH_LONG).show()
            }
        }
    }
}
