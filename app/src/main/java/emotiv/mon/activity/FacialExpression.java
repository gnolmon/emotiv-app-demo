package emotiv.mon.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
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

import com.emotiv.insight.FacialExpressionDetection;
import com.emotiv.insight.IEmoStateDLL;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import emotiv.mon.R;
import emotiv.mon.dataget.EngineConnector;
import emotiv.mon.dataget.EngineInterface;
import emotiv.mon.spinner.CustomSpinner;
import emotiv.mon.spinner.SpinnerAdapter;
import emotiv.mon.spinner.SpinnerModel;

/**
 * Created by admin on 9/7/2016.
 */
public class FacialExpression extends Activity implements EngineInterface {

    Spinner spinner,spinnerSensitive;
    CustomSpinner spinner2;
    TimerTask timerTask,timerTaskAnimation;
    SpinnerAdapter adapter, adapterSpinnerAction, adapterSensitive;
    ImageView imgBox;
    ProgressBar barTime,powerBar;
    Timer timer;
    TextView tvTime;
    boolean mapping= false;
    int indexActionSellected = 0;

    private Vector<String> mappingAction;
    int userId = 0,count = 0;
    EngineConnector engineConnector;
    Button btStartTrainning, btClear;

    public static float _currentPower = 0;
    boolean isTrainning = false;
    String currentRunningAction="";

    float startLeft 	= -1;
    float startRight 	= 0;
    float widthScreen 	= 0;

    public ArrayList<SpinnerModel> CustomListViewValuesArr  = new ArrayList<SpinnerModel>();
    public ArrayList<SpinnerModel> CustomListViewValuesArr2 = new ArrayList<SpinnerModel>();
    public ArrayList<SpinnerModel> CustomListViewValuesArr3 = new ArrayList<SpinnerModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial);
        EngineConnector.setContext(this);
        engineConnector = EngineConnector.shareInstance();
        engineConnector.delegate = this;

        mappingAction = new Vector<String>();
        mappingAction.add("Neutral");
        mappingAction.add("Pull");
        mappingAction.add("Push");
        mappingAction.add("Left");
        mappingAction.add("Right");

        spinner = (Spinner) this.findViewById(R.id.spinner1);
        spinner2 = (CustomSpinner) this.findViewById(R.id.spinner2);
        spinnerSensitive = (Spinner) this.findViewById(R.id.spinner3);

        barTime = (ProgressBar) this.findViewById(R.id.progressTimer);
        powerBar = (ProgressBar) this.findViewById(R.id.ProgressBarpower);
        imgBox = (ImageView) this.findViewById(R.id.imgBox);
        tvTime = (TextView) this.findViewById(R.id.tvTime);
        btStartTrainning = (Button)this.findViewById(R.id.btStartTrainning);
        btClear = (Button)this.findViewById(R.id.btClear);

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearData();
            }
        });

        btStartTrainning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!engineConnector.isConnected)
                    Toast.makeText(FacialExpression.this,"You need to connect to your headset.",Toast.LENGTH_SHORT).show();
                else{
                    switch (indexActionSellected) {
                        case 0:
                            startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_NEUTRAL);
                            break;
                        case 1:
                            startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SMILE);
                            break;
                        case 2:
                            startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_CLENCH);
                            break;
                        case 3:
                            startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_FROWN);
                            break;
                        case 4:
                            startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SURPRISE);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        barTime.setVisibility(View.INVISIBLE);
        setListData();

        adapterSpinnerAction = new SpinnerAdapter(this, R.layout.row, CustomListViewValuesArr);
        spinner.setAdapter(adapterSpinnerAction);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexActionSellected = position;
                mapping = true;
                spinner2.setSelection(indexActionSellected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        adapter = new SpinnerAdapter(this, R.layout.row, CustomListViewValuesArr2);
        adapter.headerData = mappingAction;
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mapping) {
                    mapping = false;
                    return;
                }
                mapping = true;
                switch (position){
                    case 0:
                        mappingAction.setElementAt("Neutral", indexActionSellected);
                        break;
                    case 1:
                        mappingAction.setElementAt("Pull", indexActionSellected);
                        break;
                    case 2:
                        mappingAction.setElementAt("Push", indexActionSellected);
                        break;
                    case 3:
                        mappingAction.setElementAt("Left", indexActionSellected);
                        break;
                    case 4:
                        mappingAction.setElementAt("Right", indexActionSellected);
                        break;
                }
                spinner2.setSelection(indexActionSellected);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        adapterSensitive = new SpinnerAdapter(this, R.layout.row, CustomListViewValuesArr3);
        spinnerSensitive.setAdapter(adapterSensitive);
        spinnerSensitive.setSelection(4);
        spinnerSensitive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (indexActionSellected) {
                    case 0:
                        // neutral
                        FacialExpressionDetection.IEE_FacialExpressionSetThreshold(userId, IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_NEUTRAL.ToInt(), FacialExpressionDetection.IEE_FacialExpressionThreshold_t.FE_SENSITIVITY.toInt(), position*100);
                        break;
                    case 1:
                        //smile
                        FacialExpressionDetection.IEE_FacialExpressionSetThreshold(userId, IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SMILE.ToInt(), FacialExpressionDetection.IEE_FacialExpressionThreshold_t.FE_SENSITIVITY.toInt(), position*100);
                        break;
                    case 2:
                        //clench
                        FacialExpressionDetection.IEE_FacialExpressionSetThreshold(userId, IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_CLENCH.ToInt(), FacialExpressionDetection.IEE_FacialExpressionThreshold_t.FE_SENSITIVITY.toInt(), position*100);
                        break;
                    case 3:
                        //frown
                        FacialExpressionDetection.IEE_FacialExpressionSetThreshold(userId, IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_FROWN.ToInt(), FacialExpressionDetection.IEE_FacialExpressionThreshold_t.FE_SENSITIVITY.toInt(), position*100);
                        break;
                    case 4:
                        //suprise
                        FacialExpressionDetection.IEE_FacialExpressionSetThreshold(userId, IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SURPRISE.ToInt(), FacialExpressionDetection.IEE_FacialExpressionThreshold_t.FE_SENSITIVITY.toInt(), position*100);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Timer timerListenAction = new Timer();
        timerListenAction.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandlerUpdateUI.sendEmptyMessage(1);
            }
        }, 0, 20);

    }

    public Handler mHandlerUpdateUI = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    count ++;
                    int trainningTime = (int)FacialExpressionDetection.IEE_FacialExpressionGetTrainingTime(userId)[1]/1000;
                    if(trainningTime > 0)
                        barTime.setProgress(count / trainningTime);
                    if (barTime.getProgress() > 100){
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

    private void moveBox() {
        float power = _currentPower;
        if(isTrainning){
            imgBox.setLeft((int)(startLeft));
            imgBox.setRight((int) startRight);
            imgBox.setScaleX(1.0f);
            imgBox.setScaleY(1.0f);
        }
        if(( currentRunningAction.equals("Neutral"))  || (currentRunningAction.equals("Right")) && power > 0) {

            if(imgBox.getScaleX() == 1.0f && startLeft > 0) {

                imgBox.setRight((int) widthScreen);
                power = ( currentRunningAction.equals("Left")) ? power*3 : power*-3;
                imgBox.setLeft((int) (power > 0 ? Math.max(0, (int)(imgBox.getLeft() - power)) : Math.min(widthScreen - imgBox.getMeasuredWidth(), (int)(imgBox.getLeft() - power))));
            }
        }
        else if(imgBox.getLeft() != startLeft && startLeft > 0){
            power = (imgBox.getLeft() > startLeft) ? 6 : -6;
            imgBox.setLeft(power > 0  ? Math.max((int)startLeft, (int)(imgBox.getLeft() - power)) : Math.min((int)startLeft, (int)(imgBox.getLeft() - power)));
        }
        if((( currentRunningAction.equals("Pull")) || ( currentRunningAction.equals("Push"))) && power > 0) {
            if(imgBox.getLeft() != startLeft)
                return;
            imgBox.setRight((int) startRight);
            power = (currentRunningAction.equals("Push")) ? power / 20 : power/-20;
            imgBox.setScaleX((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleX() - power)) : Math.min(2, (imgBox.getScaleX() - power))));
            imgBox.setScaleY((float) (power > 0 ? Math.max(0.1, (imgBox.getScaleY() - power)) : Math.min(2, (imgBox.getScaleY() - power))));
        }
        else if(imgBox.getScaleX() != 1.0f){
            power = (imgBox.getScaleX() < 1.0f) ? 0.03f : -0.03f;
            imgBox.setScaleX((float) (power > 0 ? Math.min(1, (imgBox.getScaleX() + power)) : Math.max(1, (imgBox.getScaleX() + power))));
            imgBox.setScaleY((float) (power > 0 ? Math.min(1, (imgBox.getScaleY() + power)) : Math.max(1, (imgBox.getScaleY() + power))));
        }
    }

    private void setListData() {
        SpinnerModel sched = new SpinnerModel();
        sched.setTvName("Neutral");
        sched.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_NEUTRAL.ToInt()));
        CustomListViewValuesArr.add(sched);

        sched= new SpinnerModel();
        sched.setTvName("Smile");
        sched.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SMILE.ToInt()));
        CustomListViewValuesArr.add(sched);

        sched= new SpinnerModel();
        sched.setTvName("Clench");
        sched.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_CLENCH.ToInt()));
        CustomListViewValuesArr.add(sched);

        sched= new SpinnerModel();
        sched.setTvName("Frown");
        sched.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_FROWN.ToInt()));
        sched.setChecked(false);
        CustomListViewValuesArr.add(sched);

        sched= new SpinnerModel();
        sched.setTvName("Surprise");
        sched.setChecked(engineConnector.checkTrained(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SURPRISE.ToInt()));
        sched.setChecked(false);
        CustomListViewValuesArr.add(sched);

        for (int i = 0 ;i < mappingAction.size() ;i ++){
            sched = new SpinnerModel();
            sched.setTvName(mappingAction.elementAt(i));
            sched.setChecked(false);
            CustomListViewValuesArr2.add(sched);
        }
        for(int i = 0; i < 11 ; i++){
            sched = new SpinnerModel();
            sched.setTvName("" + i);
            CustomListViewValuesArr3.add(sched);
        }
    }

    private void clearData(){
        switch (indexActionSellected) {
            case 0:
                engineConnector.tranningClearFacial(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_NEUTRAL.ToInt());
                break;
            case 1:
                engineConnector.tranningClearFacial(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SMILE.ToInt());
                break;
            case 2:
                engineConnector.tranningClearFacial(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_CLENCH.ToInt());
                break;
            case 3:
                engineConnector.tranningClearFacial(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_FROWN.ToInt());
                break;
            case 4:
                engineConnector.tranningClearFacial(IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SURPRISE.ToInt());
                break;
            default:
                break;
        }
    }

    private void startTrainingFacialExpression(IEmoStateDLL.IEE_FacialExpressionAlgo_t FacialExpressionAction) {
        isTrainning = engineConnector.startFacialExpression(isTrainning, FacialExpressionAction);
        btStartTrainning.setText((isTrainning) ? "Abort Trainning" : "Train");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        widthScreen = size.x;
        startLeft = imgBox.getLeft();
        startRight = imgBox.getRight();
    }

    public void enableClick() {
        spinner.setClickable(true);
        spinner2.setClickable(true);
        btClear.setClickable(true);
        spinnerSensitive.setClickable(true);
    }

    @Override
    public void trainStarted() {
        barTime.setVisibility(View.VISIBLE);
        Log.d("STEP:", "1");
        spinner.setClickable(false);
        spinner2.setClickable(false);
        btClear.setClickable(false);
        spinnerSensitive.setClickable(false);
        timer = new Timer();
        intTimerTask();
        timer.schedule(timerTask ,0, 10);
    }

    private void intTimerTask(){
        count=0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandlerUpdateUI.sendEmptyMessage(0);
            }
        };
    }

    @Override
    public void trainSucceed() {
        barTime.setVisibility(View.INVISIBLE);
        enableClick();
        btStartTrainning.setText("Train");
        new AlertDialog.Builder(this)
                .setTitle("Training Succeeded")
                .setMessage("Training is successful. Accept this training?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        engineConnector.setTrainControl(FacialExpressionDetection.IEE_FacialExpressionTrainingControl_t.FE_ACCEPT.getType());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        engineConnector.setTrainControl(FacialExpressionDetection.IEE_FacialExpressionTrainingControl_t.FE_REJECT.getType());
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void trainFailed() {

    }

    @Override
    public void trainCompleted() {
        SpinnerModel model = CustomListViewValuesArr.get(indexActionSellected);
        model.setChecked(true);
        CustomListViewValuesArr.set(indexActionSellected, model);
        adapterSpinnerAction.notifyDataSetChanged();

        new AlertDialog.Builder(this)
                .setTitle("Training Completed")
                .setMessage("")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        isTrainning = false;
    }

    @Override
    public void trainRejected() {
        SpinnerModel model = CustomListViewValuesArr.get(indexActionSellected);
        model.setChecked(false);
        enableClick();
        isTrainning=false;
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

        SpinnerModel model = CustomListViewValuesArr.get(indexActionSellected);
        model.setChecked(false);
        CustomListViewValuesArr.set(indexActionSellected, model);
        adapterSpinnerAction.notifyDataSetChanged();
        enableClick();
        isTrainning=false;
    }

    @Override
    public void trainReset() {
        if(timer!=null){
            timer.cancel();
            timerTask.cancel();
            barTime.setVisibility(View.INVISIBLE);
            powerBar.setProgress(0);
        }
        enableClick();
        isTrainning=false;
    }

    @Override
    public void userAdd(int userId) {
        this.userId=userId;
    }

    @Override
    public void userRemoved() {
        this.userId = -1;
    }

    @Override
    public void detectedActionLowerFace(int typeAction, float power) {
        _currentPower=power;
        if (typeAction == IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_SMILE.ToInt()) {
            runAnimation(indexActionSellected,power);
        }
        else if (typeAction == IEmoStateDLL.IEE_FacialExpressionAlgo_t.FE_CLENCH.ToInt())
            runAnimation(indexActionSellected,power);
    }

    @Override
    public void currentAction(int typeAction, float power, float time) {
        tvTime.setText("" + time);
    }

    private void runAnimation(int index,float power){
        powerBar.setProgress((int) (power*100));
        currentRunningAction = mappingAction.elementAt(index);
    }

    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
    }
}
