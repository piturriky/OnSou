package com.Lluis.onSou.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by Lluís on 28/04/2015.
 */
@Entity
public class Notification {

    @Id
    private Long Id;


//    private static HashMap<Integer,String> notificationTypes = new HashMap<Integer,String>(){{
//        put(1,"AddFriend");
//    }};


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;

        Notification that = (Notification) o;

        if (!Id.equals(that.Id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Id.hashCode();
    }
}
