package com.nit.voicelibrarymvp

object ValidationUtils {
    fun isValidName(name: String): Boolean = name.matches(Regex("^[a-zA-Z\\s]+$"))
    
    fun isValidEmail(email: String): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    
    fun isValidPhone(phone: String): Boolean = phone.length == 10 && phone.all { it.isDigit() }
    
    data class PasswordRequirement(
        val label: String,
        val isMet: Boolean
    )

    fun getPasswordRequirements(password: String): List<PasswordRequirement> {
        return listOf(
            PasswordRequirement("At least 8 characters", password.length >= 8),
            PasswordRequirement("At least one uppercase letter", password.any { it.isUpperCase() }),
            PasswordRequirement("At least one lowercase letter", password.any { it.isLowerCase() }),
            PasswordRequirement("At least one digit", password.any { it.isDigit() }),
            PasswordRequirement("At least one special character (@#$%^&+=!)", password.any { "@#$%^&+=!".contains(it) })
        )
    }

    fun isValidPassword(password: String): Boolean {
        return getPasswordRequirements(password).all { it.isMet }
    }
}
