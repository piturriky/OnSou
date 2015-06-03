package com.udl.lluis.onsou;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.lluis.onsou.backend.registration.Registration;
import com.lluis.onsou.backend.registration.model.Result;
import com.udl.lluis.onsou.entities.MyDevice;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity{



    private LoginActivity loginActivity = this;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private GcmRegistrationAsyncTask mGCMRegistrationTask = null;
    private NodeJSServerRegistration mNodeJSServerRegistration = null;

    // UI references.
    private EditText mUserNamelView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String regid;
    private GoogleCloudMessaging gcm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUserNamelView = (EditText) findViewById(R.id.user);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin("login");
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin("login");
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin("register");
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.e(Globals.TAG,"ONRESUME");
        checkPlayServices();
        if(MyDevice.getInstance().isOnline()){
            startMainActivity();
            finish();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(String type) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserNamelView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNamelView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUserNamelView.setError(getString(R.string.error_field_required));
            focusView = mUserNamelView;
            cancel = true;
        }

        // Check if network is active
        if(!checkNetwork()){
            showToast(getString(R.string.activateNetwork));
            cancel = true;

            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
//            final ComponentName cn = new ComponentName("com.android.phone","com.android.phone.NetworkSetting");//MobileNetworkSettings
//            final Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
//            intent.addCategory(Intent.ACTION_MAIN);
//            intent.setComponent(cn);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if(focusView != null)focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute(type);
        }
    }

    public boolean checkNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Result> {
        private Registration regService = null;

        private final String mUserName;
        private final String mPassword;

        private String type;

        private Result result;
        private com.lluis.onsou.backend.registration.model.Device device;

        UserLoginTask(String userName, String password) {
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected Result doInBackground(String... params) {
            type = (String) params[0];
            if (regService == null) {
                Registration.Builder builder;

                builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");

                regService = builder.build();
            }

            try {
                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                switch(type){
                    case "login":
                        result = regService.login(mUserName, mPassword).execute();
                        break;
                    case "register":
                        result = regService.register(mUserName, mPassword).execute();
                        break;
                    default:
                        result = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Result result) {
            mAuthTask = null;

            if (result.getStatus()) {
                device = (com.lluis.onsou.backend.registration.model.Device)result.getDevice();
                MyDevice.getInstance().setId(device.getId());
                MyDevice.getInstance().setName(device.getUsername());
                MyDevice.getInstance().setOnline(device.getOnline());
                if(type == "register"){
                    mNodeJSServerRegistration = new NodeJSServerRegistration();
                    mNodeJSServerRegistration.execute( mUserName, mPassword, Long.toString(MyDevice.getInstance().getId()));
                }
                mGCMRegistrationTask = new GcmRegistrationAsyncTask(getApplicationContext(), mUserName, mPassword);
                mGCMRegistrationTask.execute((Void)null);
            } else {
                showProgress(false);
                switch(result.getErrorType()){
                    case 1:
                    case 3:
                        mUserNamelView.setError(result.getMsg());
                        mUserNamelView.requestFocus();
                        break;
                    case 2:
                        mPasswordView.setError(result.getMsg());
                        mPasswordView.requestFocus();
                        break;
                    default:
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class NodeJSServerRegistration extends AsyncTask<String,  Void, String> {
        //http://hmkcode.com/android-send-json-data-to-server/
        String s_url = "http://192.168.1.24:3000/register";

        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            String result = "";
            try {

                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(s_url);

                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("username", (String) params[0]);
                jsonObject.accumulate("password", (String) params[1]);
                jsonObject.accumulate("id", (String) params[2]);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            // 11. return result
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e(Globals.TAG, result);

        }

        @Override
        protected void onCancelled() {
            mNodeJSServerRegistration = null;
            super.onCancelled();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    private class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private Registration regService = null;

        private GoogleCloudMessaging gcm;
        private Context context;

        private final String mUserName;
        private final String mPassword;

        public GcmRegistrationAsyncTask(Context context, String userName, String password) {
            this.context = context;
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://tranquil-well-88613.appspot.com/_ah/api/");

                regService = builder.build();
            }

            //Chequemos si está instalado Google Play Services
            if(checkPlayServices()){
                gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);

                //Obtenemos el Registration ID guardado
                regid = getRegistrationId(getApplicationContext());

                //Si no disponemos de Registration ID comenzamos el registro
                if (regid.equals("")) {

                    try{
                        if (gcm == null){
                            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                        }

                        //Nos registramos en los servidores de GCM
                        regid = gcm.register(Globals.SENDER_ID);
                        MyDevice.getInstance().setGCMId(regid);
                        Log.d("--->", "Registrado en GCM: registration_id=" + regid);
                    }
                    catch (IOException ex){
                        Log.d("--->", "Error registro en GCM:" + ex.getMessage());
                    }
                }
            }else{
                Log.i("--->", "No se ha encontrado Google Play Services.");
                return false;
            }

            try {
                Result res = regService.registerGCMId(mUserName, mPassword,regid).execute();
                if (!res.getStatus()){
                    return false;
                }
                //Guardamos los datos del registro
                setRegistrationId(context, mUserName, regid);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            MyDevice.getInstance().setGCMId(regid);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGCMRegistrationTask = null;

            if (success) {
                startMainActivity();
                showProgress(false);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                showProgress(false);
            }
        }

        @Override
        protected void onCancelled() {
            mGCMRegistrationTask = null;
            showProgress(false);
        }
    }

    private void startMainActivity(){
        Intent intent = null;
        intent = new Intent(loginActivity, MainActivity.class);
        startActivity(intent);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        Globals.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Log.i("--->", "Dispositivo no soportado.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context){
        SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);

        String registrationId = prefs.getString(Globals.PROPERTY_REG_ID, "");

        if (registrationId.length() == 0){
            Log.d("-->", "Registro GCM no encontrado.");
            return "";
        }

        String registeredUser =
                prefs.getString(Globals.PROPERTY_USER, "user");

        int registeredVersion =
                prefs.getInt(Globals.PROPERTY_APP_VERSION, Integer.MIN_VALUE);

        long expirationTime =
                prefs.getLong(Globals.PROPERTY_EXPIRATION_TIME, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String expirationDate = sdf.format(new Date(expirationTime));

        Log.d("-->", "Registro GCM encontrado (usuario=" + registeredUser +
                ", version=" + registeredVersion +
                ", expira=" + expirationDate + ")");

        int currentVersion = getAppVersion(context);

        if (registeredVersion != currentVersion){
            Log.d("-->", "Nueva versión de la aplicación.");
            return "";
        }
        else if (System.currentTimeMillis() > expirationTime){
            Log.d("-->", "Registro GCM expirado.");
            return "";
        }else if (!mUserNamelView.getText().toString().equals(registeredUser)){
            Log.d("-->", "Nuevo nombre de usuario.");
            return "";
        }

        return registrationId;
    }

    private static int getAppVersion(Context context){
        try{
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        }catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException("Error al obtener versión: " + e);
        }
    }

    private void setRegistrationId(Context context, String user, String regId) {
        SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);

        int appVersion = getAppVersion(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Globals.PROPERTY_USER, user);
        editor.putString(Globals.PROPERTY_REG_ID, regId);
        editor.putInt(Globals.PROPERTY_APP_VERSION, appVersion);
        editor.putLong(Globals.PROPERTY_EXPIRATION_TIME,
                System.currentTimeMillis() + Globals.EXPIRATION_TIME_MS);

        editor.commit();
    }

    private void showToast(CharSequence text){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

}



