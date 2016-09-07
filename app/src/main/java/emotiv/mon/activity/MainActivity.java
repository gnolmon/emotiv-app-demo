package emotiv.mon.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import emotiv.mon.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void mentalClick(View v){
        Intent intent = new Intent(this, MentalCommandActivity.class);
        startActivity(intent);
    }

    public void facialClick(View v){
        Intent intent = new Intent(this, FacialExpression.class);
        startActivity(intent);
    }

    public void motionClick(View v){
        Intent intent = new Intent(this, MotionLogger.class);
        startActivity(intent);
    }
}
