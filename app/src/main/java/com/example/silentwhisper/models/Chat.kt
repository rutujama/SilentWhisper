package com.example.silentwhisper.models

class Chat {
    private var sender:String=""
    private var message:String=""
    private var receiver:String=""
    private var isseen:Boolean=false
    private var url:String=""
    private var messageId:String=""

    constructor()
    constructor(
        sender: String,
        message: String,
        receiver: String,
        isseen: Boolean,
        url: String,
        messageId: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isseen = isseen
        this.url = url
        this.messageId = messageId
    }

    fun getsender():String{
        return sender
    }

    fun setsender(sender: String){
        this.sender=sender
    }


    fun getmessage():String{
        return message
    }

    fun setmessage(message: String){
        this.message=message
    }

    fun getreceiver():String{
        return receiver
    }

    fun setreceiver(receiver: String){
        this.receiver=receiver
    }

    fun getisSeen():Boolean{
        return isseen
    }

    fun setisSeen(isseen: Boolean){
        this.isseen=isseen
    }

    fun geturl():String{
        return url
    }

    fun seturl(url: String){
        this.url=url
    }

    fun getmessageId():String{
        return messageId
    }

    fun setmessageId(messageId: String){
        this.messageId=messageId
    }

}