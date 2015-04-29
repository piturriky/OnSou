/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.Lluis.onSou.backend;

import com.Lluis.onSou.backend.model.AddFriendNotification;
import com.Lluis.onSou.backend.model.Device;
import com.Lluis.onSou.backend.model.Notification;
import com.Lluis.onSou.backend.model.Result;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import javafx.util.Pair;

import static com.Lluis.onSou.backend.OfyService.ofy;

/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 * <p/>
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * <p/>
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
@Api(name = "registration", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.onSou.Lluis.com", ownerName = "backend.onSou.Lluis.com", packagePath = ""))
public class RegistrationEndpoint {

    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Api Keys can be obtained from the google cloud console
     */
    private static final String API_KEY = System.getProperty("gcm.api.key");

    @ApiMethod(name = "login")
    public Result login(@Named("username") String username, @Named("pass") String pass) {
        Result res = new Result();
        Device device = findDevice(username);
        if (device == null) {
            log.info("Username " + username + " don't exist");
            res.setStatus(false);
            res.setErrorType(1);
            res.setMsg("Username " + username + " don't exist");
        }else if(!device.getPass().equals(pass)){
            res.setStatus(false);
            res.setErrorType(2);
            res.setMsg("This password is incorrect");
        }else{
            device.setOnline(true);
            ofy().save().entity(device).now();
            res.setStatus(true);
            res.setObj(device);
        }
        return res;
    }

    @ApiMethod(name = "register")
    public Result register(@Named("username") String username, @Named("pass") String pass) {
        Result res = new Result();
        if (findDevice(username) != null) {
            log.info("Device " + username + " already registered");
            res.setStatus(false);
            res.setErrorType(3);
            res.setMsg("Device " + username + " already registered");
        }else{
            Device device = new Device(username,pass);
            device.setOnline(true);
            ofy().save().entity(device).now();
            res.setStatus(true);
            res.setObj(device);
            if(device.hasPendingNotifications()){
                Thread t = new SendPendingNotificationsThread(device);
                t.run();
            }
        }

        return res;
    }

    @ApiMethod(name = "registerGCMId")
    public Result registerGCMId(@Named("username") String username, @Named("pass") String pass, @Named("regId") String regId) {
        Result res = new Result();
        Device device = findDevice(username);
        if (device == null) {
            log.info("Device " + username + " not registered");
            res.setStatus(false);
            res.setMsg("Device " + username + " not registered");
        }else if(!device.getPass().equals(pass)){
            res.setStatus(false);
            res.setErrorType(2);
            res.setMsg("This password is incorrect");
        }else{
            device.setGCMId(regId);
            ofy().save().entity(device).now();
            res.setStatus(true);
        }
        return res;
    }


    @ApiMethod(name = "unregister")
    public Result unregisterDevice(@Named("id") Long id) {
        Result result = new Result();
        Device device = findDevice(id);

        if (device == null) {
            log.info("Device " + id + " not registered, skipping unregister");
            result.setStatus(false);
            result.setErrorType(4);
        }else{
            device.setOnline(false);
            device.setGCMId("");
            ofy().save().entity(device).now();
            result.setStatus(true);
            //ofy().delete().entity().now();
        }
        return result;
    }

    @ApiMethod(name="updatePosition")
    public Result updatePosition(@Named("id") Long id, @Named("latitude") double latitude, @Named("longitude") double longitude){
        Result result = new Result();
        Device device = findDevice(id);

        if(device == null){
            result.setStatus(false);
            result.setErrorType(4);
            result.setMsg(Result.errorTypes.get(4));
        }else{
            device.setLatitude(latitude);
            device.setLongitude(longitude);
            ofy().save().entity(device).now();
            result.setStatus(true);
        }
        return result;
    }

    @ApiMethod(name="getDevices")
    public Result getDevices(@Named("id") Long id){
        Result result = new Result();
        Device device = findDevice(id);

        if(device == null){
            result.setStatus(false);
            result.setErrorType(4);
            result.setMsg(Result.errorTypes.get(4));
        }else{
            ArrayList <Pair<Device,Boolean>> devicesResult = new ArrayList<>();
            List<Device> allDevices = ofy().load().type(Device.class).list();

            for(Device d : allDevices){
                if(d.getId() != id){
                    if(device.isMyFriend(d.getId())){
                        devicesResult.add(new Pair<Device, Boolean>(d,true));
                    }else{
                        devicesResult.add(new Pair<Device, Boolean>(d,false));
                    }
                }
            }

            result.setStatus(true);
            result.setObj(devicesResult);
        }
        return result;
    }

    @ApiMethod(name="addDevice")
    public Result addDevice(@Named("id") Long id, @Named("friendUserName") String friendUserName){
        Result result = new Result();
        Device device = findDevice(id);
        Device friendDevice = findDevice(friendUserName);

        if(device == null){
            result.setStatus(false);
            result.setErrorType(4);
            result.setMsg(Result.errorTypes.get(4));
        }else if(friendDevice == null){
            result.setStatus(false);
            result.setErrorType(4);
            result.setMsg("This username doesn't exist");
        }else{
            AddFriendNotification notification = new AddFriendNotification(id,friendDevice.getId());
            if(friendDevice.isOnline()){
                sendAddFriendNotification(notification);
            }else{
                friendDevice.addNotification(notification);
                ofy().save().entity(friendDevice).now();
            }
        }
        return result;
    }

    /**
     * Return a collection of registered devices
     *
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<Device> listDevices() {
        List<Device> records = ofy().load().type(Device.class).list();
        return CollectionResponse.<Device>builder().setItems(records).build();
    }

    private boolean sendAddFriendNotification(@Named("Notification")AddFriendNotification notification){
        Device from = findDevice(notification.getSender());

        Message msg = new Message.Builder()
                .addData("type", "addFriendNotification")
                .addData("from", from.getUsername())
                .build();
        try {
            return sendMessage(msg,notification.getReceiver());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean sendMessage(Message msg,@Named("id") Long to) throws IOException{
        Device device = findDevice(to);
        Sender sender = new Sender(API_KEY);
        com.google.android.gcm.server.Result result = sender.send(msg, device.getGCMId(), 5);
        if (result.getMessageId() != null) {
            log.info("Message sent to " + to);
            String canonicalRegId = result.getCanonicalRegistrationId();

            if (canonicalRegId != null) {
                // if the regId changed, we have to update the datastore
                log.info("Registration Id changed for " + to + " updating to " + canonicalRegId);
                device.setGCMId(canonicalRegId);
                ofy().save().entity(device).now();
            }
            return true;
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                log.warning("Registration Id " + device.getGCMId() + " no longer registered with GCM, removing from datastore");
                // if the device is no longer registered with Gcm, remove it from the datastore
                // TODO ofy().delete().entity(device).now();
            } else {
                log.warning("Error when sending message : " + error);
            }
            return false;
        }
    }

    public void sendMessage1(@Named("message") String message) throws IOException {
        if (message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }
        // crop longer messages
        if (message.length() > 1000) {
            message = message.substring(0, 1000) + "[...]";
        }
        Sender sender = new Sender(API_KEY);
        Message msg = new Message.Builder().addData("message", message).build();
        List<Device> devices = ofy().load().type(Device.class).list();
        for (Device device : devices) {
            com.google.android.gcm.server.Result result = sender.send(msg, device.getGCMId(), 5);
            if (result.getMessageId() != null) {
                log.info("Message sent to " + device.getGCMId());
                String canonicalRegId = result.getCanonicalRegistrationId();
                if (canonicalRegId != null) {
                    // if the regId changed, we have to update the datastore
                    log.info("Registration Id changed for " + device.getGCMId() + " updating to " + canonicalRegId);
                    device.setGCMId(canonicalRegId);
                    ofy().save().entity(device).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    log.warning("Registration Id " + device.getGCMId() + " no longer registered with GCM, removing from datastore");
                    // if the device is no longer registered with Gcm, remove it from the datastore
                    ofy().delete().entity(device).now();
                } else {
                    log.warning("Error when sending message : " + error);
                }
            }
        }
    }


    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class).filter("regId", regId).first().now();
    }

    private Device findDevice(long id){
        return ofy().load().type(Device.class).filter("id", id).first().now();
    }
    private Device findDevice(String username){
        return ofy().load().type(Device.class).filter("username", username).first().now();
    }


    private class SendPendingNotificationsThread extends Thread{

        Device device;

        public SendPendingNotificationsThread(Device device){
            super();
            this.device = device;
        }

        @Override
        public void run() {
            super.run();
            for(Notification not : device.pendingNotifications()){
                if(not instanceof AddFriendNotification){
                    if(sendAddFriendNotification((AddFriendNotification)not)){
                        device.removeNotification(not);
                    }
                }
            }
        }
    }
}
