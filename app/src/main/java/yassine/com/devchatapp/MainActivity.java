package yassine.com.devchatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter mTabsPagerAdapter;
    private FirebaseUser currentUser;
    private DatabaseReference UsersReference;
    private DatabaseReference Rootref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Rootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String online_user_id = mAuth.getCurrentUser().getUid();
            UsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mTabsPagerAdapter);
        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //mToolbar.setTitle("myChat");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("DevChat");

    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            LogOutUser();

        } else if (currentUser != null) {
            UsersReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null) {
            UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void LogOutUser() {
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.mainlogout_button) {

            if (currentUser != null) {
                UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            LogOutUser();

        }
        if (item.getItemId() == R.id.main_account_setting_button_button) {
            Intent settingdIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingdIntent);

        }


        if (item.getItemId() == R.id.main_all_users_button) {
            Intent AllUsersIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(AllUsersIntent);
        }
        if (item.getItemId() == R.id.main_create_groupe_button){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
            builder.setTitle("Enter Group Name");
            final EditText groupeNameField = new EditText(MainActivity.this);
            groupeNameField.setHint("Amigos");
            builder.setView(groupeNameField);
            builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String groupeName = groupeNameField.getText().toString();
                    if(TextUtils.isEmpty(groupeName)){
                        Toast.makeText(MainActivity.this, "Please write a name for your groupe", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Rootref.child("Groups").child(groupeName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MainActivity.this,groupeName + "group is Created Successfully",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        }


        return true;

    }
}