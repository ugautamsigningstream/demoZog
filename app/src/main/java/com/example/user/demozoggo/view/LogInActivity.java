package com.example.user.demozoggo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.demozoggo.R;
import com.example.user.demozoggo.db.UserDatabaseHelper;

import static android.Manifest.permission.READ_CONTACTS;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_READ_CONTACTS = 0;
    private static UserLoginTask mAuthTask = null;
    private final String TAG = "LogInActivity";
    //get UI
    private View mProgressView; //progressview
    private View mLoginFormView; //scrollview

    private AutoCompleteTextView mEmailAutoCompleteTv;
    private EditText mUserPassword;

    private Button mLogInButton, mRegisterButton;


    private UserDatabaseHelper mUserDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize all UIs
        mEmailAutoCompleteTv = findViewById(R.id.email);
        populateAutoComplete();

        mUserPassword = findViewById(R.id.password);
        mUserPassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin(); /* It should try to login if user hits ENTER key after providing password */
                return true;
            }
            return false;
        });

        mLogInButton = findViewById(R.id.log_in_button1);
        mRegisterButton = findViewById(R.id.register_button);

        //setup click listeners on the buttons
        mLogInButton.setOnClickListener(this);
        mRegisterButton.setOnClickListener(this);

        //UserDatabaseHelper
        mUserDatabaseHelper = UserDatabaseHelper.getDataHelper(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailAutoCompleteTv.setError(null);
        mUserPassword.setError(null);

        String email = mEmailAutoCompleteTv.getText().toString();
        String password = mUserPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mUserPassword.setError(getString(R.string.error_invalid_password));
            focusView = mUserPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailAutoCompleteTv.setError(getString(R.string.error_field_required));
            focusView = mEmailAutoCompleteTv;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailAutoCompleteTv.setError(getString(R.string.error_invalid_email));
            focusView = mEmailAutoCompleteTv;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

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

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailAutoCompleteTv, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.log_in_button1: {
                attemptLogin();
                break;
            }
            case R.id.register_button: {
                Log.i(TAG, "REGISTER");

                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);


                break;
            }
            default: {
                //don't do anything
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    //inner AsyncTask to validate email/password in the database
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mInsideEmail;
        private String mInsidePassword;

        UserLoginTask(String email, String password) {
            mInsideEmail = email;
            mInsidePassword = password;
        }

        public UserLoginTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            //validate email and password from the server
            if (mUserDatabaseHelper.checkUser(mInsideEmail, mInsidePassword)) {
                //email and password exists
                startHomeActivity();
            }


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mUserPassword.setError(getString(R.string.error_incorrect_password));
                mUserPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
