package com.nit.voicelibrarymvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phone: String) : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Holds the verification ID Firebase sends back after SMS is sent
    private var storedVerificationId = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Pending user data for registration
    private var pendingName = ""
    private var pendingPhone = ""
    private var pendingRole = ""

    // ─── SEND OTP ─────────────────────────────────────────────

    fun sendOtp(
        phone: String,
        name: String = "",
        role: String = "",
        isRegistration: Boolean,
        activity: android.app.Activity
    ) {
        // Validate phone number
        if (phone.isBlank() || phone.length < 10) {
            _authState.value = AuthState.Error("Please enter a valid 10-digit phone number")
            return
        }

        // Format to international format for India
        val formattedPhone = if (phone.startsWith("+")) phone else "+91$phone"

        _authState.value = AuthState.Loading

        // Save pending data
        pendingPhone = formattedPhone
        pendingName = name
        pendingRole = role

        viewModelScope.launch {
            try {
                if (isRegistration) {
                    // Check if already registered
                    val existing = firestore.collection("users")
                        .document(formattedPhone).get().await()
                    if (existing.exists()) {
                        _authState.value = AuthState.Error(
                            "Phone number already registered. Please login."
                        )
                        return@launch
                    }
                } else {
                    // Check if phone exists for login
                    val userDoc = firestore.collection("users")
                        .document(formattedPhone).get().await()
                    if (!userDoc.exists()) {
                        _authState.value = AuthState.Error(
                            "Phone number not registered. Please register first."
                        )
                        return@launch
                    }
                }

                // Build Firebase phone auth options
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedPhone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        // Called when OTP is auto-detected (on real device)
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            signInWithCredential(credential, isRegistration)
                        }

                        // Called when something goes wrong
                        override fun onVerificationFailed(e: FirebaseException) {
                            _authState.value = AuthState.Error(
                                e.message ?: "OTP sending failed. Check your phone number."
                            )
                        }

                        // Called when SMS is sent successfully
                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            storedVerificationId = verificationId
                            resendToken = token
                            _authState.value = AuthState.OtpSent(formattedPhone)
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    // ─── VERIFY OTP ────────────────────────────────────────────

    fun verifyOtp(enteredOtp: String, isRegistration: Boolean) {
        if (enteredOtp.length != 6) {
            _authState.value = AuthState.Error("Please enter the complete 6-digit OTP")
            return
        }

        if (storedVerificationId.isEmpty()) {
            _authState.value = AuthState.Error("Session expired. Please request OTP again.")
            return
        }

        _authState.value = AuthState.Loading

        // Create credential from verification ID + OTP user entered
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, enteredOtp)
        signInWithCredential(credential, isRegistration)
    }

    // ─── SIGN IN WITH CREDENTIAL ───────────────────────────────

    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        isRegistration: Boolean
    ) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()

                if (isRegistration) {
                    // Save new user to Firestore
                    firestore.collection("users")
                        .document(pendingPhone)
                        .set(mapOf(
                            "name" to pendingName,
                            "phone" to pendingPhone,
                            "role" to pendingRole,
                            "createdAt" to System.currentTimeMillis()
                        )).await()
                    _authState.value = AuthState.Success(pendingRole)
                } else {
                    // Get role from Firestore
                    val userDoc = firestore.collection("users")
                        .document(pendingPhone).get().await()
                    val role = userDoc.getString("role") ?: "USER"
                    _authState.value = AuthState.Success(role)
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "Invalid OTP. Please check and try again."
                )
            }
        }
    }

    // ─── RESEND OTP ────────────────────────────────────────────

    fun resendOtp(activity: android.app.Activity) {
        if (pendingPhone.isEmpty()) return

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(pendingPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setForceResendingToken(resendToken!!)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential, pendingName.isNotEmpty())
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.message ?: "Resend failed")
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    _authState.value = AuthState.OtpSent(pendingPhone)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ─── HELPERS ───────────────────────────────────────────────

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun getCurrentUserRole(onResult: (String?) -> Unit) {
        val phone = auth.currentUser?.phoneNumber ?: return onResult(null)
        firestore.collection("users").document(phone)
            .get()
            .addOnSuccessListener { doc -> onResult(doc.getString("role")) }
            .addOnFailureListener { onResult(null) }
    }
}