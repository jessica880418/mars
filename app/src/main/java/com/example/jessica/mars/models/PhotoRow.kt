package com.example.jessica.mars.models

enum class RowType {
    PHOTO,
    HEADER
}

data class PhotoRow(var type: RowType, var photo: Photo?, var header: String?)