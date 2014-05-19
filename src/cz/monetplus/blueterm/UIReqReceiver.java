package cz.monetplus.blueterm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.UIReqListener;

public class UIReqReceiver implements UIReqListener {

    private Context context;

    public UIReqReceiver(Context context) {
        super();
        this.context = context;
    }

    /**
     * String tag for logging.
     */
    private static final String TAG = "UIReqReceiver";

    @Override
    public void onReceive(byte[] uiReqData) {
        Log.i(TAG, "No HTML data received -> send back to sender");

        // Mirror the data back to the Vx600
        VMF.sendUIResponseData(uiReqData);
    }

}
