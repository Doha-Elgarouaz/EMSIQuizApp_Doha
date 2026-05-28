package com.doha.emsiquizapp

data class Question(
    var question: String = "",
    var rep1: String = "",
    var rep2: String = "",
    var rep3: String = "",
    var rep4: String = "",
    var repCorrect: String = "",
    var imgUrl: String = ""
)