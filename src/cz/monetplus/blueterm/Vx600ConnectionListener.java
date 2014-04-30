package cz.monetplus.blueterm;

import com.verifone.vmf.api.VMF.ConnectionListener;

public class Vx600ConnectionListener implements ConnectionListener
{

    @Override
    public void onConnectionEstablished() {
        
    }

    @Override
    public void onConnectionFailed() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDisconnected(String arg0) {
        // TODO Auto-generated method stub
        
    }

//  @Override
//  public void onConnectionEstablished()
//  {
//    Runnable action = new Runnable()
//    {
//
//      @Override
//      public void run()
//      {
//        mLoadingDialog.dismissAllowingStateLoss();
//        Toast.makeText(MainActivity.this, R.string.vx_connection_stablished, Toast.LENGTH_SHORT).show();
//        changeControlsState(true);
//
//      }
//
//    };
//
//    runOnUiThread(action);
//  }

//  @Override
//  public void onConnectionFailed()
//  {
//    Runnable action = new Runnable()
//    {
//      @Override
//      public void run()
//      {
//        changeControlsState(false);
//        mLoadingDialog.dismissAllowingStateLoss();
//
//        Toast.makeText(MainActivity.this, getString(R.string.vx_connection_failed_message),
//                       Toast.LENGTH_SHORT).show();
//      }
//    };
//
//    runOnUiThread(action);
//  }
//
//  @Override
//  public void onDisconnected(final String deviceName)
//  {
//
//    DeviceDisconnectedDialogFragment reconnectDialog = new DeviceDisconnectedDialogFragment(
//      deviceName);
//    FragmentManager manager = getSupportFragmentManager();
//    FragmentTransaction transaction = manager.beginTransaction();
//    transaction.add(reconnectDialog, "reconnectDialog");
//    transaction.commitAllowingStateLoss();
//    changeControlsState(false);
//  }

}