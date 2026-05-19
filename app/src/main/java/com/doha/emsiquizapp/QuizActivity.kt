package com.doha.emsiquizapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class QuizActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var questionsList = mutableListOf<Question>()
    private var index = 0
    private var score = 0
    private var isAnswered = false

    private lateinit var tvQuestion: TextView
    private lateinit var radio1: RadioButton
    private lateinit var radio2: RadioButton
    private lateinit var radio3: RadioButton
    private lateinit var radio4: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnNext: Button
    private lateinit var questionImage: ImageView
    private lateinit var viewFinder: PreviewView
    
    private lateinit var tvAiExplanation: TextView
    private lateinit var btnExplainIA: Button
    private lateinit var agentAvatar: ImageView

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )

    private var lastAnalysisTime = 0L
    private var lastFraudAlertTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        db = FirebaseFirestore.getInstance()
        initViews()
        checkCameraPermission()
        loadQuestions()

        btnNext.setOnClickListener {
            if (!isAnswered) {
                if (radioGroup.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "Sélectionnez une réponse", Toast.LENGTH_SHORT).show()
                } else {
                    checkAnswer()
                }
            } else {
                nextQuestion()
            }
        }

        btnExplainIA.setOnClickListener {
            if (index < questionsList.size) {
                val q = questionsList[index]
                explainWithAI(q.question, q.repCorrect)
            }
        }

        agentAvatar.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun initViews() {
        tvQuestion = findViewById(R.id.tvQuestion)
        radio1 = findViewById(R.id.radio1)
        radio2 = findViewById(R.id.radio2)
        radio3 = findViewById(R.id.radio3)
        radio4 = findViewById(R.id.radio4)
        radioGroup = findViewById(R.id.radioGroup)
        btnNext = findViewById(R.id.btnNext)
        questionImage = findViewById(R.id.questionImage)
        viewFinder = findViewById(R.id.viewFinder)
        tvAiExplanation = findViewById(R.id.tvAiExplanation)
        btnExplainIA = findViewById(R.id.btnExplainIA)
        agentAvatar = findViewById(R.id.agentAvatar)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy -> processImageProxy(imageProxy) }
                }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("QUIZ", "Camera error", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < 1000) {
            imageProxy.close()
            return
        }
        lastAnalysisTime = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            faceDetector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()) detectFraud("Reste devant l'écran ! 🕵️‍♂️")
                    else if (faces.size > 1) detectFraud("Pas de triche ! 🚫")
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun detectFraud(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFraudAlertTime > 5000) {
            lastFraudAlertTime = currentTime
            runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun loadQuestions() {
        tvQuestion.text = "Chargement..."
        db.collection("questions").get()
            .addOnSuccessListener { result ->
                questionsList.clear()
                for (doc in result) {
                    val q = doc.toObject(Question::class.java)
                    questionsList.add(q)
                }
                if (questionsList.isNotEmpty()) {
                    showQuestion()
                } else {
                    tvQuestion.text = "Firestore: Collection 'questions' vide."
                }
            }
            .addOnFailureListener { e ->
                tvQuestion.text = "Firestore Error: ${e.message}"
            }
    }

    private fun showQuestion() {
        isAnswered = false
        btnNext.text = "Validate"
        tvAiExplanation.visibility = View.GONE
        btnExplainIA.visibility = View.GONE
        resetOptionsStyle()
        
        val q = questionsList[index]
        tvQuestion.text = q.question
        
        if (q.imgUrl.isNotEmpty()) {
            questionImage.visibility = View.VISIBLE
            Glide.with(this).load(q.imgUrl).into(questionImage)
        } else {
            questionImage.visibility = View.GONE
        }

        setOption(radio1, q.rep1)
        setOption(radio2, q.rep2)
        setOption(radio3, q.rep3)
        setOption(radio4, q.rep4)
        radioGroup.clearCheck()
        enableOptions(true)
    }

    private fun checkAnswer() {
        isAnswered = true
        btnNext.text = "Next"
        enableOptions(false)
        btnExplainIA.visibility = View.VISIBLE
        
        val selectedId = radioGroup.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedId)
        val selectedAnswer = selectedRadioButton.text.toString().trim()
        val correctAnswer = questionsList[index].repCorrect.trim()
        
        highlightCorrectAnswer(correctAnswer)
        
        if (selectedAnswer.equals(correctAnswer, ignoreCase = true)) {
            score++
        } else {
            selectedRadioButton.setBackgroundResource(R.drawable.option_wrong_bg)
            selectedRadioButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }
    }

    private fun highlightCorrectAnswer(correct: String) {
        val radios = listOf(radio1, radio2, radio3, radio4)
        for (radio in radios) {
            if (radio.text.toString().trim().equals(correct, ignoreCase = true)) {
                radio.setBackgroundResource(R.drawable.option_correct_bg)
                radio.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }
    }

    private fun nextQuestion() {
        index++
        if (index < questionsList.size) {
            showQuestion()
        } else {
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("score", score)
            intent.putExtra("total", questionsList.size)
            startActivity(intent)
            finish()
        }
    }

    private fun resetOptionsStyle() {
        val radios = listOf(radio1, radio2, radio3, radio4)
        for (radio in radios) {
            radio.setBackgroundResource(R.drawable.option_selector)
            radio.setTextColor(ContextCompat.getColorStateList(this, R.drawable.option_text_selector))
        }
    }

    private fun enableOptions(enable: Boolean) {
        radio1.isEnabled = enable
        radio2.isEnabled = enable
        radio3.isEnabled = enable
        radio4.isEnabled = enable
    }

    private fun setOption(radio: RadioButton, text: String) {
        if (text.isEmpty()) {
            radio.visibility = View.GONE
        } else {
            radio.visibility = View.VISIBLE
            radio.text = text
        }
    }

    private fun explainWithAI(question: String, correctAnswer: String) {
        tvAiExplanation.visibility = View.VISIBLE
        tvAiExplanation.text = "L'IA réfléchit... ⏳"
        
        lifecycleScope.launch {
            try {
                // Initialisation locale pour isoler le crash
                val model = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                
                val prompt = "Explique brièvement pourquoi la réponse à '$question' est '$correctAnswer'."
                val response = model.generateContent(prompt)
                
                runOnUiThread {
                    tvAiExplanation.text = response.text ?: "Pas de réponse."
                }
            } catch (e: Throwable) {
                Log.e("AI_CRASH", "Détail du crash", e)
                runOnUiThread {
                    // Affiche l'erreur au lieu de fermer l'app
                    tvAiExplanation.text = "Erreur IA: ${e.localizedMessage}"
                    Toast.makeText(this@QuizActivity, "Détail: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector.close()
    }
}
