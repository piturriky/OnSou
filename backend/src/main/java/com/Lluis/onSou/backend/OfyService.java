package com.Lluis.onSou.backend;

import com.Lluis.onSou.backend.model.AddFriendNotification;
import com.Lluis.onSou.backend.model.Device;
import com.Lluis.onSou.backend.model.Notification;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Objectify service wrapper so we can statically register our persistence classes
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public class OfyService {

    static {
        ObjectifyService.register(RegistrationRecord.class);
        ObjectifyService.register(Device.class);
        ObjectifyService.register(Notification.class);
        ObjectifyService.register(AddFriendNotification.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}