package yassine.com.devchatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button NeedNewAcoountButton;
    private Button AlreadyHaveAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        NeedNewAcoountButton = (Button) findViewById(R.id.need_account_button);
        AlreadyHaveAccountButton = (Button) findViewById(R.id.already_have_account_button);
        NeedNewAcoountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent RegisterIntent  = new Intent(StartPageActivity.this, RegisterActivity.class);
                startActivity(RegisterIntent);

            }
        });
        AlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent LogIntent = new Intent(StartPageActivity.this,LoginActivity.class);
                startActivity(LogIntent);


            }
        });
    }
}
