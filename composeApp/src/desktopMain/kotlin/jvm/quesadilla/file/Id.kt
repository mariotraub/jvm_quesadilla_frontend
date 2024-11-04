package jvm.quesadilla.file

import kotlinx.serialization.Serializable

@Serializable
data class Id(
    val timestamp: String,
    val date: String
)