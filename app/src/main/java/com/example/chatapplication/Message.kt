package com.example.chatapplication

class Message {
    lateinit var emotion: String
    var message: String? = null
    var senderId: String? = null
    constructor(){}
    constructor(message: String?, senderId: String?){
        this.message = message
        this.senderId = senderId
    }
}