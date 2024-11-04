package jvm.quesadilla.file

import kotlinx.serialization.Serializable

@Serializable
data class File(
    val id: Id,
    val path: String,
    val type: String,
)