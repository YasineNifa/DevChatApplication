package yassine.com.devchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG ="HEYHEYEHEY" ;
    private Toolbar mToolbar;
    private EditText registerUserName;
    private EditText registerUserEmail;
    private EditText registerUserPassword;
    private Button registerCreateAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog LoadingBar;
    private DatabaseReference storeUserDefaultDataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerCreateAccount = (Button)findViewById(R.id.create_account_button);
        registerUserEmail = (EditText)findViewById(R.id.register_email);
        registerUserName = (EditText)findViewById(R.id.register_name);
        mAuth = FirebaseAuth.getInstance();
        LoadingBar = new ProgressDialog(this);
        registerUserPassword = (EditText)findViewById(R.id.register_password);
        registerCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = registerUserName.getText().toString();
                String email = registerUserEmail.getText().toString();
                String password = registerUserPassword.getText().toString();
                RegisterAccount(name,email,password);
            }
        });
    }
    private void RegisterAccount(final String name, String email, String password) {
        if (TextUtils.isEmpty(name)){
            Toast.makeText(RegisterActivity.this,"please write your name", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,"please write your email", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(RegisterActivity.this,"please write your password", Toast.LENGTH_LONG).show();
        }
        else {
            LoadingBar.setTitle("Creating new account");
            LoadingBar.setMessage("Please wait, while we are creating account for you");
            LoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this,new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String DeviceToken = FirebaseInstanceId.getInstance().getToken();


                        String current_user_id = mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("Hey there, I am using DevChat");
                        storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                        storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });



                    }
                    else{
                        Toast.makeText(RegisterActivity.this,"Authentification failed ", Toast.LENGTH_LONG).show();
                        Log.d("FirebaseAuth", "onComplete ERROR ERROR ERROR ERROR ERROR" + task.getException().getMessage());
                    }
                    LoadingBar.dismiss();
                }
            });
        }

    }
}
