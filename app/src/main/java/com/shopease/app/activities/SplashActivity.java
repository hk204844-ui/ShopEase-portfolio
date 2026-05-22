package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.shopease.app.R;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable navigationRunnable = () -> {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        SessionManager sessionManager = new SessionManager(SplashActivity.this);
        Intent intent = sessionManager.isLoggedIn()
                ? new Intent(SplashActivity.this, HomeActivity.class)
                : new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Run database initialization in background to avoid blocking UI thread
        new Thread(() -> {
            DatabaseHelper databaseHelper = new DatabaseHelper(SplashActivity.this);
            databaseHelper.seedProducts();
            
            // Navigate after seeding is complete and delay has passed
            handler.postDelayed(navigationRunnable, SPLASH_DELAY_MS);
        }).start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigationRunnable);
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        // Disable back navigation from splash to ensure database seeding completes.
        // We do not call super.onBackPressed() here.
    }
}
