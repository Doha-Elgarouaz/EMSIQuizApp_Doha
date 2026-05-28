# EMSI Quiz App 🎓

Une application de Quiz intelligente et sécurisée développée pour Android, combinant l'intelligence artificielle et la détection de fraude en temps réel.

## 🚀 Fonctionnalités

- **Authentification Hybride** : Support de Firebase Auth et Supabase Auth pour une gestion robuste des utilisateurs.
- **Quiz Interactif** : Questions dynamiques chargées depuis Firebase Firestore.
- **Intelligence Artificielle (Gemini)** : Explications détaillées des réponses générées par l'IA (Google Generative AI).
- **Anti-Fraude (Face Detection)** : Surveillance par caméra frontale utilisant ML Kit pour détecter l'absence de l'utilisateur ou la présence de plusieurs personnes pendant le quiz.
- **Chat avec Agent** : Espace de discussion intégré pour l'assistance aux étudiants.
- **Reconnaissance Faciale** : Inscription sécurisée avec capture faciale.

## 🛠 Technologies utilisées

- **Langage** : Kotlin
- **Base de données** : Firebase Firestore & Supabase
- **Authentification** : Firebase Auth & Supabase Auth
- **IA** : Google Gemini Pro (Generative AI SDK)
- **Vision** : ML Kit Face Detection & CameraX
- **Chargement d'images** : Glide
- **Architecture** : MVVM / Clean Code principles

## 📦 Installation

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-username/EMSIQuizApp.git
   ```
2. Ajoutez votre fichier `google-services.json` dans le dossier `app/`.
3. Configurez votre clé API Gemini dans le fichier `app/build.gradle.kts` :
   ```kotlin
   buildConfigField("String", "GEMINI_API_KEY", "\"VOTRE_CLE_ICI\"")
   ```
4. Synchronisez le projet avec Gradle.
5. Lancez l'application sur un appareil Android ou un émulateur.

## 📸 Captures d'écran

*(Ajoutez vos captures d'écran ici)*

## 📄 Licence

Ce projet est réalisé dans le cadre d'un projet académique à l'EMSI.
