package com.example.flexifitapp.onboarding

interface ValidatablePage {
    fun validateBeforeNext(): String?
}