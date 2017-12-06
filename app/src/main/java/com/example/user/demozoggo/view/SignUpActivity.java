package com.example.user.demozoggo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.demozoggo.R;
import com.example.user.demozoggo.db.UserDatabaseHelper;
import com.example.user.demozoggo.model.User;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static UserSignUpTask mAuthTask = null;
    private final String TAG = "SignUpActivity";
    //get UI
    private View mProgressView; //progressview
    private View mLoginFormView; //scrollview


    //get UI
    private AutoCompleteTextView mFirstName, mLastName, mEmail;
    private EditText mPassword, mRepeatPassword;
    private AutoCompleteTextView mPhone;
    private Button mSignUpButton, mLogInButton;

    private UserDatabaseHelper mUserDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //initialize all UIs
        mFirstName = findViewById(R.id.firstName);
        mLastName = findViewById(R.id.lastName);
        mEmail = findViewById(R.id.email);

        mPassword = findViewById(R.id.password);
        mRepeatPassword = findViewById(R.id.passwordAgain);

        mPhone = findViewById(R.id.phone);

        mSignUpButton = findViewById(R.id.sign_up_button);
        mLogInButton = findViewById(R.id.log_in_button2);

        //setup click listeners on the buttons
        mSignUpButton.setOnClickListener(this);
        mLogInButton.setOnClickListener(this);

        //UserDatabaseHelper
        mUserDatabaseHelper = UserDatabaseHelper.getDataHelper(this);

        mLoginFormView = findViewById(R.id.login_form_signup);
        mProgressView = findViewById(R.id.login_progress_signup);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.log_in_button2: {
                Log.i(TAG, "LOGIN");

                //just redirect to LogInActivity
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);


                break;
            }
            case R.id.sign_up_button: {
                Log.i(TAG, "SIGNUP");
                doSignUp();
                break;
            }
            default: {
                //don't do anything
            }
        }
    }

    private void doSignUp() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mFirstName.setError(null);
        mLastName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);
        mRepeatPassword.setError(null);
        mPhone.setError(null);

        String firstName = mFirstName.getText().toString();
        String lastName = mLastName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String repeatPassword = mRepeatPassword.getText().toString();
        String phone = mPhone.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(firstName) && !isNameValid(firstName)) {
            mFirstName.setError(getString(R.string.error_invalid_first_name));
            focusView = mFirstName;
            cancel = true;
        }

        if (!TextUtils.isEmpty(lastName) && !isNameValid(lastName)) {
            mLastName.setError(getString(R.string.error_invalid_last_name));
            focusView = mLastName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }
        if (!TextUtils.isEmpty(repeatPassword) && !isPasswordValid(repeatPassword)) {
            mRepeatPassword.setError(getString(R.string.error_invalid_password));
            focusView = mRepeatPassword;
            cancel = true;
        }

        if (!TextUtils.isEmpty(phone) && !isPhoneValid(phone)) {
            mPhone.setError(getString(R.string.error_invalid_phone));
            focusView = mPhone;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserSignUpTask(firstName, lastName, email, password, phone);
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

    private boolean isNameValid(String name) {
        return name.length() > 2;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isPhoneValid(String phone) {
        return phone.length() == 10;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LogInActivity.class);
        //show the database insertion successful toast
//        Toast.makeText(this, "Database insertion successful", Toast.LENGTH_SHORT);
        startActivity(intent);
    }

    //inner AsyncTask to validate email/password in the database
    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {
        private String insideFirstName, insideLastName, insideEmail, insidePassword, insidePhone;


        public UserSignUpTask(String firstName, String lastName, String email, String password, String phone) {
            super();
            this.insideFirstName = firstName;
            this.insideLastName = lastName;
            this.insideEmail = email;
            this.insidePassword = password;
            this.insidePhone = phone;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
            }
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

            //write to model and also write to database:             insideFirstName, insideLastName, insideEmail, insidePassword, insidePhone;

            User user = new User(insideFirstName, insideLastName, insideEmail, insidePassword, insidePhone);
            mUserDatabaseHelper.addUser(user);

            startLoginActivity();
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
