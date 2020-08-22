package com.nisith.smartchat.Model;

import java.util.Objects;

public class Message {
    private String messageKey;//Key of this message. But this key is for clint side. so we don't store message key to the server. We set this key
                              //only in clint side to simply our messages operation...
    private String senderUid; //User id, Who send the message
    private String messageType;//type may be text_message or image_type or audio_type etc
    private String message;
    private boolean read; //message is read or not
    private String date;
    private String time;//time in 12 hour format

    public Message(){

    }

    public Message(String senderUid, String messageType, String message, boolean read, String date, String time) {
        this.senderUid = senderUid;
        this.messageType = messageType;
        this.message = message;
        this.read = read;
        this.date = date;
        this.time = time;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object != null) {
            Message message2 = (Message) object;
            if (this.getMessageKey().equals(message2.getMessageKey())){
                result = true;
            }
        }
        return result;
    }

}
