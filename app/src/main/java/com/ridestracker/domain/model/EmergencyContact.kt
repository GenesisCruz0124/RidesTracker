package com.ridestracker.domain.model

data class EmergencyContact(
    val name: String = "",
    val phoneNumber: String = ""
) {
    val isSet: Boolean get() = phoneNumber.isNotBlank()
}
