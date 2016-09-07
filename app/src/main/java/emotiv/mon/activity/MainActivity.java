package emotiv.mon.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import emotiv.mon.R;
import emotiv.mon.dataget.EngineConnector;

public class MainActivity extends AppCompatActivity {
    EngineConnector engineConnector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EngineConnector.setContext(this);
        engineConnector = EngineConnector.shareInstance();
    }

    public void mentalClick(View v){
        Intent intent = new Intent(this, MentalCommand.class);
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
