package emotiv.mon.dataget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.emotiv.insight.FacialExpressionDetection;
import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEmoStateDLL;
import com.emotiv.insight.MentalCommandDetection;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by admin on 9/7/2016.
 */
public class EngineConnector {
    public static Context context;
    public static EngineConnector engineConnectorInstance;
    private Timer timer;
    private TimerTask timerTask;

    public boolean isConnected = false;
    private int state;
    private int userId = -1;

    /* ============================================ */
    protected static final int TYPE_USER_ADD                = 16;
    protected static final int TYPE_USER_REMOVE             = 32;
    protected static final int TYPE_EMOSTATE_UPDATE         = 64;
    protected static final int TYPE_METACOMMAND_EVENT       = 256;
    protected static final int TYPE_FACIAL_EVENT    	    = 512;
    /* ============================================ */
    protected static final int HANDLER_TRAIN_STARTED        = 1;
    protected static final int HANDLER_TRAIN_SUCCEED        = 2;
    protected static final int HANDLER_TRAIN_FAILED         = 3;
    protected static final int HANDLER_TRAIN_COMPLETED      = 4;
    protected static final int HANDLER_TRAIN_ERASED         = 5;
    protected static final int HANDLER_TRAIN_REJECTED       = 6;
    protected static final int HANDLER_ACTION_CURRENT       = 7;
    protected static final int HANDLER_USER_ADD             = 8;
    protected static final int HANDLER_USER_REMOVE          = 9;
    protected static final int HANDLER_TRAINED_RESET        = 10;

    public EngineInterface delegate;

    public static void setContext(Context context){
        EngineConnector.context = context;
    }

    public static EngineConnector shareInstance(){
        if (engineConnectorInstance == null){
            engineConnectorInstance = new EngineConnector();
        }
        return  engineConnectorInstance;
    }

    public EngineConnector(){
        connectEngine();
    }

    private void connectEngine(){
        IEdk.IEE_EngineConnect(EngineConnector.context,"");
        timer = new Timer();
        doRepeatTask();
        timer.schedule(timerTask, 0, 10);
    }

    private void doRepeatTask(){
        if (timerTask != null) return;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                int numDevices = IEdk.IEE_GetInsightDeviceCount();
                Log.i("Num Devices:", "" + numDevices);
                if (numDevices != 0) {
                    if (!isConnected){
                        IEdk.IEE_ConnectInsightDevice(0);
                    }
                }

                state = IEdk.IEE_EngineGetNextEvent();
                if(state == IEdkErrorCode.EDK_OK.ToInt()){
                    int eventType = IEdk.IEE_EmoEngineEventGetType();
                    switch (eventType){
                        case TYPE_USER_ADD:
                            Log.i("Connected", "User Added");
                            isConnected = true;
                            userId = IEdk.IEE_EmoEngineEventGetUserId();
                            handler.sendEmptyMessage(HANDLER_USER_ADD);
                            break;
                        case TYPE_USER_REMOVE:
                            Log.i("Disconnected", "User Removed");
                            userId = -1;
                            handler.sendEmptyMessage(HANDLER_USER_REMOVE);
                            break;
                        case TYPE_EMOSTATE_UPDATE:
                            if(!isConnected) break;
                            IEdk.IEE_EmoEngineEventGetEmoState();
                            handler.sendMessage(handler.obtainMessage(HANDLER_ACTION_CURRENT));
                            break;
                        case TYPE_METACOMMAND_EVENT:
                            int type = MentalCommandDetection.IEE_MentalCommandEventGetType();
                            if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingStarted
                                    .getType()) {
                                Log.e("MentalCommand", "training started");
                                handler.sendEmptyMessage(HANDLER_TRAIN_STARTED);
                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingSucceeded
                                    .getType()) {
                                Log.e("MentalCommand", "training Succeeded");
                                handler.sendEmptyMessage(HANDLER_TRAIN_SUCCEED);
                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingCompleted
                                    .getType()) {
                                Log.e("MentalCommand", "training Completed");
                                handler.sendEmptyMessage(HANDLER_TRAIN_COMPLETED);
                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingDataErased
                                    .getType()) {
                                Log.e("MentalCommand", "training erased");
                                handler.sendEmptyMessage(HANDLER_TRAIN_ERASED);

                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingFailed
                                    .getType()) {
                                Log.e("MentalCommand", "training failed");
                                handler.sendEmptyMessage(HANDLER_TRAIN_FAILED);

                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingRejected
                                    .getType()) {
                                Log.e("MentalCommand", "training rejected");
                                handler.sendEmptyMessage(HANDLER_TRAIN_REJECTED);
                            } else if (type == MentalCommandDetection.IEE_MentalCommandEvent_t.IEE_MentalCommandTrainingReset
                                    .getType()) {
                                Log.e("MentalCommand", "training Reset");
                                handler.sendEmptyMessage(HANDLER_TRAINED_RESET);
                            }
                            break;
                        case TYPE_FACIAL_EVENT:
                            int typeFacial = FacialExpressionDetection.IEE_FacialExpressionEventGetType();

                            if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingCompleted.getType()){
                                Log.e("FacialExpression", "training completed");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAIN_COMPLETED));
                            }
                            else if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingDataErased.getType()){
                                Log.e("FacialExpression", "training erased");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAIN_ERASED));
                            }
                            else if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingFailed.getType()){
                                Log.e("FacialExpression", "training failed");
                            }
                            else if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingRejected.getType()){
                                Log.e("FacialExpression", "training rejected");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAIN_REJECTED));
                            }
                            else
                            if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingReset.getType()){
                                Log.e("FacialExpression", "training reseted");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAINED_RESET));
                            }
                            else
                            if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingStarted.getType()){
                                Log.e("FacialExpression", "training started");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAIN_STARTED));
                            }
                            else
                            if (typeFacial == FacialExpressionDetection.IEE_FacialExpressionEvent_t.IEE_FacialExpressionTrainingSucceeded.getType()){
                                Log.e("FacialExpression", "training succeeded");
                                handler.sendMessage(handler.obtainMessage(HANDLER_TRAIN_SUCCEED));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_USER_ADD:
                    if (delegate != null)
                        delegate.userAdd(userId);
                    break;
                case HANDLER_USER_REMOVE:
                    if (delegate != null)
                        delegate.userRemoved();
                    break;
                case HANDLER_ACTION_CURRENT:
                    if (delegate != null)
                        delegate.currentAction(IEmoStateDLL.IS_MentalCommandGetCurrentAction(),
                                IEmoStateDLL.IS_MentalCommandGetCurrentActionPower());
                    break;
                case HANDLER_TRAIN_STARTED:
                    if (delegate != null)
                        delegate.trainStarted();
                    break;
                case HANDLER_TRAIN_SUCCEED:
                    if (delegate != null)
                        delegate.trainSucceed();
                    break;
                case HANDLER_TRAIN_FAILED:
                    if(delegate != null)
                        delegate.trainFailed();
                    break;
                case HANDLER_TRAIN_COMPLETED:
                    if (delegate != null)
                        delegate.trainCompleted();
                    break;
                case HANDLER_TRAIN_ERASED:
                    if (delegate != null)
                        delegate.trainErased();
                    break;
                case HANDLER_TRAIN_REJECTED:
                    if (delegate != null)
                        delegate.trainRejected();
                    break;
                case HANDLER_TRAINED_RESET:
                    if (delegate != null)
                        delegate.trainReset();
                    break;
                default:
                    break;
            }
        }
    };

    public boolean checkTrained(int action) {
        long[] result = MentalCommandDetection.IEE_MentalCommandGetTrainedSignatureActions(userId);
        if (result[0] == IEdkErrorCode.EDK_OK.ToInt()) {
            long y = result[1] & action;
            return (y == action);
        }
        return false;
    }

    public void trainningClear(int action){
        MentalCommandDetection.IEE_MentalCommandSetTrainingAction(userId, action);
        if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_ERASE.getType()) == IEdkErrorCode.EDK_OK.ToInt()) {
        }
    }


    public boolean startTrainingMetalcommand(Boolean isTrain, IEmoStateDLL.IEE_MentalCommandAction_t MetaCommandAction) {
        if (!isTrain) {
            if (MentalCommandDetection.IEE_MentalCommandSetTrainingAction(userId,
                    MetaCommandAction.ToInt()) == IEdkErrorCode.EDK_OK.ToInt()) {
                if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                        MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_START.getType()) == IEdkErrorCode.EDK_OK
                        .ToInt()) {
                    return true;
                }
            }
        } else {
            if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId,
                    MentalCommandDetection.IEE_MentalCommandTrainingControl_t.MC_RESET.getType()) == IEdkErrorCode.EDK_OK
                    .ToInt()) {
                return false;
            }
        }
        return false;
    }

    public void enableMentalcommandActions(IEmoStateDLL.IEE_MentalCommandAction_t _MetalcommandAction) {
        long MetaCommandActions;
        long[] activeAction = MentalCommandDetection.IEE_MentalCommandGetActiveActions(userId);
        if (activeAction[0] == IEdkErrorCode.EDK_OK.ToInt()) {
            long y = activeAction[1] & (long) _MetalcommandAction.ToInt();
            if (y == 0) {
                MetaCommandActions = activeAction[1]| ((long) _MetalcommandAction.ToInt());
                MentalCommandDetection.IEE_MentalCommandSetActiveActions(userId, MetaCommandActions);
            }
        }
    }

    public void setTrainControl(int type) {
        if (MentalCommandDetection.IEE_MentalCommandSetTrainingControl(userId, type) == IEdkErrorCode.EDK_OK
                .ToInt()) {
        }
    }

    //facial expression
    public boolean startFacialExpression(Boolean isTrain,IEmoStateDLL.IEE_FacialExpressionAlgo_t FacialExpressionAction) {
        if (!isTrain) {
            if (FacialExpressionDetection.IEE_FacialExpressionSetTrainingAction(userId,FacialExpressionAction.ToInt()) == IEdkErrorCode.EDK_OK.ToInt()) {
                if (FacialExpressionDetection.IEE_FacialExpressionSetTrainingControl(userId, FacialExpressionDetection.IEE_FacialExpressionTrainingControl_t.FE_START.getType()) == IEdkErrorCode.EDK_OK.ToInt()) {
                    return true;
                }
            }
        } else {
            if (FacialExpressionDetection.IEE_FacialExpressionSetTrainingControl(userId, FacialExpressionDetection.IEE_FacialExpressionTrainingControl_t.FE_RESET.getType()) == IEdkErrorCode.EDK_OK.ToInt()) {
                return false;
            }
        }
        return false;
    }

    public void tranningClearFacial(int action){
        FacialExpressionDetection.IEE_FacialExpressionSetTrainingAction(userId, action);
        if (FacialExpressionDetection.IEE_FacialExpressionSetTrainingControl(userId, FacialExpressionDetection.IEE_FacialExpressionTrainingControl_t.FE_ERASE.getType()) == IEdkErrorCode.EDK_OK.ToInt()) {
        }
    }

}
