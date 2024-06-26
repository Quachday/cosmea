package com.example.model

class MessageData(
    var author: String,
    var receiver: String,
    var content: String,
    var timestamp: String,
    var image: Int? = null,
    var file: Int? = null,
    var authorImage: Int? = null,
    var id: String = generateId(author + receiver + content + timestamp),
)