# Mozhi

Voice-Based Smart Library Management System

A modern Android application that simplifies library operations using voice interaction, OCR, Firebase, and Jetpack Compose.

---

## Overview

Mozhi is an Android-based Smart Library Management System developed as a B.Tech Final Year Project. The application modernizes conventional library operations by integrating voice interaction, Optical Character Recognition (OCR), cloud-based data management, and secure authentication.

The system enables librarians to efficiently manage books, members, borrowing activities, and digital records while providing readers with an intuitive and accessible platform.

---

## Features

### Voice Assistant

- Voice-based book search
- Voice-guided book addition
- Voice-enabled book information retrieval
- Library statistics through voice
- Voice-based review support
- Malayalam and English voice commands

### Authentication

- Email & Password Authentication
- Google Sign-In
- Firebase Authentication
- Role-Based Access Control (Admin/User)

### Library Management

- Add, edit and delete books
- Automatic accession number generation
- Automatic call number generation
- Multiple copy management
- Category management
- Shelf location management

### OCR

- English book cover recognition
- Title page recognition
- Library register scanning
- Automatic extraction of book information

### QR Code

- Digital member identification
- QR code scanning
- Member verification

### Borrow Management

- Borrow request workflow
- Approval and rejection
- Book return management
- Borrow history
- Availability tracking

### Excel Management

- Import books from Excel
- Export library catalog
- Export reports
- Export library statistics

### Search

- Search by title
- Search by author
- Search by category
- Search by accession number
- Search by shelf location

### Reviews

- Book reviews
- Rating system

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Design | Material 3 |
| Authentication | Firebase Authentication |
| Database | Cloud Firestore |
| OCR | Google ML Kit |
| Barcode Scanning | ML Kit Barcode Scanner |
| Voice Recognition | Android SpeechRecognizer |
| Text-to-Speech | Android TTS |
| Excel Processing | Apache POI |

---

## Architecture

```
UI
│
├── Jetpack Compose
│
ViewModel
│
Repository
│
Firebase Authentication
Cloud Firestore
Google ML Kit
Apache POI
```

---

## Project Structure

```
app
├── ui
├── screens
├── models
├── repository
├── firebase
├── ocr
├── voice
├── excel
└── utils
```

---

## Getting Started

### Prerequisites

- Android Studio
- JDK 17 or later
- Firebase Project
- Android SDK

### Installation

```bash
git clone https://github.com/<username>/Mozhi.git
```

Open the project in Android Studio.

---

## Firebase Configuration

This repository does not include Firebase credentials.

1. Create a Firebase project.
2. Register your Android application.
3. Enable:
   - Email & Password Authentication
   - Google Sign-In
   - Cloud Firestore
4. Download `google-services.json`.
5. Place it inside:

```
app/google-services.json
```

6. Add your SHA-1 fingerprint in Firebase.
7. Sync Gradle.
8. Run the application.

---

## Screenshots

Add screenshots of:

- Login
- Dashboard
- Voice Assistant
- OCR
- Book Management
- Borrow Requests
  
 ## Screenshots

<table align="center">
  <tr>
    <td align="center">
      <img src="## Screenshots

<table align="center">
  <tr>
    <td align="center">
      <img width="1080" height="1080" alt="image" src="https://github.com/user-attachments/assets/f9dc40ad-d44e-42fe-b164-8bd07e826119" />
<br>
      <b>Login Screen</b>
    </td>
    <td align="center">
      <img width="1080" height="1080" alt="image" src="https://github.com/user-attachments/assets/357553e8-d59e-42eb-89a8-d95af13d46ef" />
<br>
      <b>Register</b>
    </td>
  </tr>

  <tr>
    <td align="center">
     <img width="707" height="4160" alt="image" src="https://github.com/user-attachments/assets/c5bb3bf7-816f-4431-a680-198488cd397a" />
>
<br>
      <b>admin dashboard</b>
    </td>
    <td align="center">
     <img width="1045" height="4160" alt="image" src="https://github.com/user-attachments/assets/a32e8580-a070-4d28-8296-85df68d11909" />
<br>
      <b>user dashboard</b>
    </td>
  </tr>


  <tr>
    <td align="center">
     <img width="720" height="1470" alt="image" src="https://github.com/user-attachments/assets/f27f5daf-7d77-4bae-82b6-899556ebf053" />
<br>
      <b>OCR Scanner</b>
    </td>
  </tr>


  
---

## Future Enhancements

- Malayalam OCR support
- AI-based book recommendations
- Push notifications
- Fine management
- Analytics dashboard

---

## License

This project is intended for educational and academic purposes.
