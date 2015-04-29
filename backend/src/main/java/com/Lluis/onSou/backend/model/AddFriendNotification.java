package com.Lluis.onSou.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Lluís on 28/04/2015.
 */
@Entity
public class AddFriendNotification extends Notification {

    @Index
    private Long sender;
    @Index
    private Long receiver;

    public AddFriendNotification(Long sender, Long receiver){
        this.sender = sender;
        this.receiver = receiver;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public Long getReceiver() {
        return receiver;
    }

    public void setReceiver(Long receiver) {
        this.receiver = receiver;
    }
}
