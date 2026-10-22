package com.example.p3moon

import java.time.LocalDate

object MoonPhase {
    private const val SYNODIC = 29.530588853
    private val REF = LocalDate.of(2000, 1, 6).toEpochDay()

    /** 0.0 = nuova, 0.25 = primo quarto, 0.5 = piena, 0.75 = ultimo quarto. */
    fun phase(date: LocalDate): Float {
        val days = (date.toEpochDay() - REF) + 0.5 - 0.76
        val cycles = ((days % SYNODIC) + SYNODIC) % SYNODIC
        return (cycles / SYNODIC).toFloat()
    }
}
