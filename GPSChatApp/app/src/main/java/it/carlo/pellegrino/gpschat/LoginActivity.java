package it.carlo.pellegrino.gpschat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
// TODO: Set up production ready app by creating secure connection.
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public Context context;

    public static String pref_string = "com.gpschat";
    public static String pref_string_token = "com.gpschat.token";
    public static String pref_string_nickname = "com.gpschat.nickname";
    public static String pref_string_expiresAt = "com.gpschat.expiresAt";
    public static String pref_string_sessionId = "com.gpschat.sessionId";
    private SharedPreferences preferences;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.userId);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        /* Set Listener for Sign In button */
        Button mUserIDSignInButton = (Button) findViewById(R.id.userID_sign_in_button);
        mUserIDSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        /* Set Listener for Go registering Button */
        Button mRegisterButton = (Button) findViewById(R.id.go_registering_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, RegisterActivity.class);
                startActivityForResult(i, 1);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        preferences = getSharedPreferences(pref_string, Context.MODE_PRIVATE);
        launchIfTokenIsValid();
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mEmailView.setText(data.getStringExtra("registered_email"));
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userId = mEmailView.getText().toString();
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
        if (TextUtils.isEmpty(userId)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isUserIDValid(userId)) {
            mEmailView.setError(getString(R.string.error_invalid_userID));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(userId, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserIDValid(String email) {
        return email.matches("^(.+)@(.+)$");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    private void launchIfTokenIsValid() {

        Log.i("GPSCHAT", "Entered in launchiftokenisvalid");
        if ( preferences.getString(pref_string_token, "") != ""
                && preferences.getString(pref_string_nickname, "") != ""
                && preferences.getString(pref_string_sessionId, "") != ""
                && preferences.getString(pref_string_expiresAt, "") != "" ) {
            Log.e("GPSCHAT", "Preferences are set up");
            DateFormat df = DateFormat.getTimeInstance();

            String expDateToParse   = preferences.getString(pref_string_expiresAt, "") + "000";
            long currentDate        = System.currentTimeMillis();
            long expireDate         = Long.parseLong(expDateToParse);
            Log.v("GPSCHAT", "Current date is " + currentDate);
            Log.v("GPSCHAT", "Expire date is " + expDateToParse);

            if (currentDate <= expireDate)
                launchMainActivity();
        }
    }

    private void launchMainActivity() {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserId;
        private final String mPassword;
        private URL authenticationService;
        private final OkHttpClient httpClient = new OkHttpClient();
        private final Context login_context;

        private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        UserLoginTask(String id, String password) {
            mUserId = id;
            mPassword = password;
            login_context = context;

            try {
                this.authenticationService = new URL(login_context.getResources().getString(R.string.auth_url));
            }
            catch (MalformedURLException ex) {
                this.authenticationService = null;
            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean result = false;

            try {
                JSONObject jsonCredential = new JSONObject()
                        .put("usr", this.mUserId)
                        .put("pwd", this.mPassword);

                RequestBody body = RequestBody.create(jsonCredential.toString(), JSON);

                Request req = new Request.Builder()
                        .url(this.authenticationService)
                        .addHeader("Content-Type",  "application/json")
                        .post(body)
                        .build();

                Log.i("GPSCHAT", req.toString());

                try (Response res = this.httpClient.newCall(req).execute()) {
                    result = res.isSuccessful();

                    String res_body = res.body().string();

                    Log.v("GPSCHAT", res.toString() + "\n" + res_body);

                    if(result) {
                        JSONObject fromAuthServiceBody = new JSONObject(res_body);
                        preferences.edit().putString(pref_string_token, (fromAuthServiceBody.get("token").toString())).apply();
                        preferences.edit().putString(pref_string_nickname, (fromAuthServiceBody.get("nickname").toString())).apply();
                        preferences.edit().putString(pref_string_expiresAt, (fromAuthServiceBody.get("expiresAt").toString())).apply();
                        preferences.edit().putString(pref_string_sessionId, (fromAuthServiceBody.get("session-id").toString())).apply();
                    }

                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }

            }
            catch (JSONException ex) {
                Log.e("GPSCHAT", ex.getStackTrace().toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);
            Toast.makeText(login_context, success.equals(true) ?
                    "Successfully authenticated" :
                    "Email or Password are not correct", Toast.LENGTH_SHORT).show();

            if(success) {
                launchMainActivity();
            }
        }

        @Override
        protected void onCancelled() {
            Log.e("GPSCHAT", "Cancelled");
            showProgress(false);
        }
    }

}

