package emotiv.mon.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import emotiv.mon.R;
import emotiv.mon.dataget.EngineConnector;
import emotiv.mon.dataget.EngineInterface;
import emotiv.mon.spinner.SpinnerAdapter;
import emotiv.mon.spinner.SpinnerModel;

/**
 * Created by admin on 9/7/2016.
 */
public class MentalCommandActivity extends Activity implements EngineInterface {
    EngineConnector engineConnector;
    Spinner spinAction;
    Button btnTrain, btnClear;
    ProgressBar progressBarTime,progressPower;
    SpinnerAdapter spinAdapter;
    ImageView imgBox;
    TextView tvTime;
    ArrayList<SpinnerModel> model = new ArrayList<SpinnerModel>();

    Timer timer;
    TimerTask timerTask;

    boolean isTrainning = false;

    int indexAction;
    int count = 0;
    int userId = 0;
    int _currentAction;

    float _currentPower = 0;
    float startLeft     = -1;
    float startRight    = 0;
    float widthScreen   = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainning);
        EngineConnector.setContext(this);
        engineConnector = EngineConnector.shareInstance();
        engineConnector.delegate = this;
        init();
    }

    public void init(){
        spinAction = (Spinner) findViewById(R.id.spinnerAction);
        btnTrain = (Button) findViewById(R.id.btstartTraing);
        btnClear = (Button) findViewById(R.id.btClearData);
        progressBarTime = (ProgressBar) findViewById(R.id.progressBarTime);
        progressPower = (ProgressBar) findViewById(R.id.ProgressBarpower);
        imgBox = (ImageView) findViewById(R.id.imgBox);
        tvTime = (TextView) findViewById(R.id.tvTime);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (indexAction){
                    case 0:
                        engineConnector.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt());
                        break;
                    case 1:
                        engineConnector.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt());
                        break;
                    case 2:
                        engineConnector.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL.ToInt());
                        break;
                    case 3:
                        engineConnector.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt());
                        break;
                    case 4:
                        engineConnector.trainningClear(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT.ToInt());
                        break;
                    default:
                        break;
                }
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!engineConnector.isConnected ){
                    Toast.makeText(MentalCommandActivity.this, "You need to connect your headset", Toast.LENGTH_LONG).show();
                }else {
                    switch (indexAction){
                        case 0:
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL);
                            break;
                        case 1:
                            engineConnector.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                            break;
                        case 2:
                            engineConnector.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL);
                            break;
                        case 3:
                            engineConnector.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT);
                            break;
                        case 4:
                            engineConnector.enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT);
                            startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        setDataSpinner();

        spinAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexAction = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Timer timerListenAction = new Timer();
        timerListenAction.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handlerUpdateUI.sendEmptyMessage(1);
            }
        }, 0, 20);
    }

    Handler handlerUpdateUI = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    count ++;
                    int trainningTime =(int) MentalCommandDetection.IEE_MentalCommandGetTrainingTime(userId)[1]/1000;
                    if(trainningTime > 0)
                        progressBarTime.setProgress(count / trainningTime);
                    if (progressBarTime.getProgress() >= 100) {
                        timerTask.cancel();
                        timer.cancel();
                    }
                    break;
                case 1:
                    moveBox();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        widthScreen = size.x;
        startLeft = imgBox.getLeft();
        startRight = imgBox.getRight();
    }

    private void moveBox(){
        float power = _currentPower;
        if (isTrainning){
            imgBox.setLeft((int)(startLeft));
            imgBox.setRight((int) startRight);
            imgBox.setScaleX(1.0f);
            imgBox.setScaleY(1.0f);
        }
        if(( _currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt())  || (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT.ToInt()) && power > 0) {

            if(imgBox.getScaleX() == 1.0f && startLeft > 0) {
                imgBox.setRight((int) widthScreen);
                power = (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt()) ? power*3 : power*-3;
                imgBox.setLeft((int) (power > 0 ? Math.max(0, (int)(imgBox.getLeft() - power)) : Math.min(widthScreen - imgBox.getMeasuredWidth(), (int)(imgBox.getLeft() - power))));
            }
        }
        else if(imgBox.getLeft() != startLeft && startLeft > 0){
            power = (imgBox.getLeft() > startLeft) ? 6 : -6;
            imgBox.setLeft(power > 0  ? Math.max((int)startLeft, (int)(imgBox.getLeft() - power)) : Math.min((int)startLeft, (int)(imgBox.getLeft() - power)));
        }
        if(((_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL.ToInt()) || (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt())) && power > 0) {
            if(imgBox.getLeft() != startLeft)
                return;
            imgBox.setRight((int) startRight);
            power = (_currentAction == IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt()) ? power / 20 : power/-20;
            imgBox.setScaleX((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleX() - power)) : Math.min(2, (imgBox.getScaleX() - power))));
            imgBox.setScaleY((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleY() - power)) : Math.min(2, (imgBox.getScaleY() - power))));
        }
        else if(imgBox.getScaleX() != 1.0f){
            power = (imgBox.getScaleX() < 1.0f) ? 0.03f : -0.03f;
            imgBox.setScaleX((float) (power > 0 ? Math.min(1, (imgBox.getScaleX() + power)) : Math.max(1, (imgBox.getScaleX() + power))));
            imgBox.setScaleY((float) (power > 0 ? Math.min(1, (imgBox.getScaleY() + power)) : Math.max(1, (imgBox.getScaleY() + power))));
        }

    }

    public void setDataSpinner() {
        model.clear();
        SpinnerModel data = new SpinnerModel();
        data.setTvName("Neutral");
        data.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_NEUTRAL.ToInt()));
        model.add(data);

        data = new SpinnerModel();
        data.setTvName("Push");
        data.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PUSH.ToInt()));
        model.add(data);

        data = new SpinnerModel();
        data.setTvName("Pull");
        data.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_PULL.ToInt()));
        model.add(data);

        data = new SpinnerModel();
        data.setTvName("Left");
        data.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_LEFT.ToInt()));
        model.add(data);

        data = new SpinnerModel();
        data.setTvName("Right");
        data.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_MentalCommandAction_t.MC_RIGHT.ToInt()));
        model.add(data);

        Log.d("SpinnerSize:", model.size() + "");

        spinAdapter = new SpinnerAdapter(this, R.layout.row, model);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAction.setAdapter(spinAdapter);
    }

    public void startTrainingMentalcommand(IEmoStateDLL.IEE_MentalCommandAction_t MentalCommandAction) {
        isTrainning = engineConnector.startTrainingMetalcommand(isTrainning, MentalCommandAction);
        btnTrain.setText((isTrainning) ? "Abort Trainning" : "Train");
    }

    private void doTaskTrain(){
        count = 0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handlerUpdateUI.sendEmptyMessage(0);
            }
        };
    }

    private void enableClick(){
        btnClear.setClickable(true);
        spinAction.setClickable(true);
    }

    @Override
    public void trainStarted() {
        progressBarTime.setVisibility(View.VISIBLE);
        btnClear.setClickable(false);
        spinAction.setClickable(false);
        timer = new Timer();
        doTaskTrain();
        timer.schedule(timerTask , 0, 10);
    }

    @Override
    public void trainSucceed() {
        progressBarTime.setVisibility(View.INVISIBLE);
        btnTrain.setText("Train");
        enableClick();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MentalCommandActivity.this);
        alertDialogBuilder.setTitle("Trainning Succeeded");
        alertDialogBuilder.setMessage("Training is successful. Accept this training?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,int which) {
                                engineConnector.setTrainControl(MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ACCEPT.getType());
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                engineConnector.setTrainControl(MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_REJECT.getType());
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void trainFailed() {
        progressBarTime.setVisibility(View.INVISIBLE);
        btnTrain.setText("Train");
        enableClick();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MentalCommandActivity.this);
        alertDialogBuilder.setTitle("Trainning Failed");
        alertDialogBuilder.setMessage("Signal is noisy. Can't training")
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        isTrainning = false;
    }

    @Override
    public void trainCompleted() {
        SpinnerModel data = model.get(indexAction);
        data.setChecked(true);
        model.set(indexAction, data);
        spinAdapter.notifyDataSetChanged();
        isTrainning = false;
    }

    @Override
    public void trainRejected() {
        SpinnerModel data=model.get(indexAction);
        data.setChecked(false);
        model.set(indexAction, data);
        spinAdapter.notifyDataSetChanged();
        enableClick();
        isTrainning = false;
    }

    @Override
    public void trainErased() {
        new AlertDialog.Builder(this)
                .setTitle("Training Erased")
                .setMessage("")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        SpinnerModel data=model.get(indexAction);
        data.setChecked(false);
        model.set(indexAction, data);
        spinAdapter.notifyDataSetChanged();
        enableClick();
        isTrainning = false;
    }

    @Override
    public void trainReset() {
        if(timer!=null){
            timer.cancel();
            timerTask.cancel();
        }
        isTrainning = false;
        progressBarTime.setVisibility(View.INVISIBLE);
        progressBarTime.setProgress(0);
        enableClick();
    }

    @Override
    public void userAdd(int userId) {
        this.userId = userId;
    }

    @Override
    public void userRemoved() {
        this.userId = -1;
    }

    @Override
    public void detectedActionLowerFace(int typeAction, float power) {

    }

    @Override
    public void currentAction(int typeAction, float power, float time) {
        progressPower.setProgress((int)(power*100));
        _currentAction = typeAction;
        _currentPower  = power;
        tvTime.setText("" + time);
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }
}