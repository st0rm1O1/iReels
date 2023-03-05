package st0rm.ireels.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import st0rm.ireels.R;



public class Swash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swash);

//        initViews();
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.gradient_start));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.gradient_end));
        getWindow().getDecorView().setSystemUiVisibility(0);

        new Handler().postDelayed(() -> {
            finishAffinity();
            startActivity(new Intent(Swash.this, Home.class));
        }, 2000);


    }
}