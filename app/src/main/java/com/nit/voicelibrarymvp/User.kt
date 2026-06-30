package com.nit.voicelibrarymvp

import com.google.firebase.firestore.PropertyName

data class User(
    @get:PropertyName("user_id") @set:PropertyName("user_id") var uid: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("mobile_phone") @set:PropertyName("mobile_phone") var phoneNumber: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("library_id") @set:PropertyName("library_id") var libraryId: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "USER",
    // These are kept for potential future use or to avoid breaking other parts of the app that might still reference them,
    // though they won't be filled in the new registration flow.
    @get:PropertyName("rollNumber") @set:PropertyName("rollNumber") var rollNumber: String = "",
    @get:PropertyName("department") @set:PropertyName("department") var department: String = "",
    @get:PropertyName("admin_id") @set:PropertyName("admin_id") var adminId: String = ""
)
