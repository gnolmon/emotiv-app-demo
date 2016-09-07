package emotiv.mon.dataget;

/**
 * Created by admin on 9/7/2016.
 */
public interface EngineInterface {
    //train
    public void trainStarted();
    public void trainSucceed();
    public void trainFailed();
    public void trainCompleted();
    public void trainRejected();
    public void trainErased();
    public void trainReset();
    public void userAdd(int userId);
    public void userRemoved();

    // detection
    public void detectedActionLowerFace(int typeAction, float power);
    public void currentAction(int typeAction,float power, float time);
}