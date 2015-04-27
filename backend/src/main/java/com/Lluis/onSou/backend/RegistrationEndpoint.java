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
            Device record = new Device(username,pass);
            record.setOnline(true);
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

    /**
     * Return a collection of registered devices
     *
     */
    @ApiMethod(name = "listDevices")
    public CollectionResponse<Device> listDevices() {
        List<Device> records = ofy().load().type(Device.class).list();
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
