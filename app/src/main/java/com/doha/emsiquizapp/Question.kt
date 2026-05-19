package com.doha.emsiquizapp

import com.google.firebase.firestore.PropertyName

data class Question(
    @get:PropertyName("question") @set:PropertyName("question") var question: String = "",
    @get:PropertyName("option1") @set:PropertyName("option1") var rep1: String = "",
    @get:PropertyName("option2") @set:PropertyName("option2") var rep2: String = "",
    @get:PropertyName("option3") @set:PropertyName("option3") var rep3: String = "",
    @get:PropertyName("option4") @set:PropertyName("option4") var rep4: String = "",
    @get:PropertyName("correctAnswer") @set:PropertyName("correctAnswer") var repCorrect: String = "",
    @get:PropertyName("imgUrl") @set:PropertyName("imgUrl") var imgUrl: String = ""
)
