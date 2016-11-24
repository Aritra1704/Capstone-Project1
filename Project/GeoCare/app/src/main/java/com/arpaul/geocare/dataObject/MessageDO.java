package com.arpaul.geocare.dataObject;

import java.io.Serializable;

/**
 * Created by Aritra on 21-10-2016.
 */

public class MessageDO implements Serializable {
    public String messageSender = "";
    public String messageBody = "";
    public double senderLat = 0.0;
    public double senderLong = 0.0;

    public MessageDO() {
    }

    public MessageDO(String Sender,String Body){
        this.messageSender = Sender;
        this.messageBody = Body;
    }

    public static final String SENDER = "SENDER";
    public static final String BODY = "BODY";
}
