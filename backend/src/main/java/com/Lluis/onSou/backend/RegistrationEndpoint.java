/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.Lluis.onSou.backend;

import com.Lluis.onSou.backend.model.Device;
import com.Lluis.onSou.backend.model.Result;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

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


    @ApiMethod(name = "login")
    public Result login(@Named("username") String username, @Named("pass") String pass) {
        Result res = new Result();
        Device device = findDevice(username);
        if (device == null) {
            log.info("Username " + username + " don't exist");
            res.setStatus(false);
            res.setMsg("Username " + username + " don't exist");
        }
        else if(!device.getPass().equals(pass)){
            res.setStatus(false);
            res.setMsg("This password is incorrect");
        }else{
            res.setStatus(true);
            res.setObj(device);
        }

        return res;
    }

    @ApiMethod(name = "register")
    public Result register(@Named("username") String username, @Named("pass") String pass) {
        Result res = new Result();
        if (findDevice(username) != null) {
            log.info("Device " + username + " already registered, skipping register");
            res.setStatus(false);
            res.setMsg("Device " + username + " already registered, skipping register");
        }else{
            Device record = new Device(username,pass);
            ofy().save().entity(record).now();
            res.setStatus(true);
            res.setObj(record);
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
        }else{
            device.setGCMId(regId);
            ofy().save().entity(device).now();
            res.setStatus(true);
        }

        return res;
    }

    /**
     * Unregister a device from the backend
     *
     * @param regId The Google Cloud Messaging registration Id to remove
     */
    @ApiMethod(name = "unregisterGCMId")
    public void unregisterDevice(@Named("regId") String regId) {
        RegistrationRecord record = findRecord(regId);
        if (record == null) {
            log.info("Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    /**
     * Register a device to the backend
     *
     * @param regId The Google Cloud Messaging registration Id to add
     */
    @ApiMethod(name = "registerTEST")
    public RegistrationRecord registerDevice(@Named("regId") String regId) {
        if (findRecord(regId) != null) {
            log.info("Device " + regId + " already registered, skipping register");
            return null;
        }
        RegistrationRecord record = new RegistrationRecord();
        record.setRegId(regId);
        ofy().save().entity(record).now();
        return record;
    }

    /**
     * Return a collection of registered devices
     *
     * @param count The number of devices to list
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<Device> listDevices(@Named("count") int count) {
        List<Device> records = ofy().load().type(Device.class).limit(count).list();
        return CollectionResponse.<Device>builder().setItems(records).build();
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
}
