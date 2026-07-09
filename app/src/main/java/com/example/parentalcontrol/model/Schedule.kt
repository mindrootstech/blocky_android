package com.example.parentalcontrol.model

import com.example.parentalcontrol.utils.Mode
import java.util.UUID

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val mode: Mode,
    val days: List<String>,
    val isEnabled: Boolean = false
)
