package com.ballbattle.android;

import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.ballbattle.BallBattleGame;

public class AndroidLauncher extends AndroidApplication {
    private static final String TAG = "BallBattle";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Log.i(TAG, "Starting BallBattle...");
            
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            config.useImmersiveMode = true;
            
            Log.i(TAG, "Initializing game...");
            initialize(new BallBattleGame(), config);
            Log.i(TAG, "Game initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Fatal error during startup", e);
            e.printStackTrace();
            throw e;
        }
    }
}
