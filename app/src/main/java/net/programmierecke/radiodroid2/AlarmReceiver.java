package net.programmierecke.radiodroid2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    String url;
    int alarmId;
    DataRadioStation station;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("recv","received broadcast");
        Toast toast = Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT);
        toast.show();

        alarmId = intent.getIntExtra("id",-1);
        Log.w("recv","alarm id:"+alarmId);

        RadioAlarmManager ram = new RadioAlarmManager(context.getApplicationContext(),null);
        station = ram.getStation(alarmId);

        if (station != null && alarmId >= 0) {
            Log.w("recv","radio id:"+alarmId);
            Play(context, station.ID);
        }else{
            toast = Toast.makeText(context, "not enough info for alarm!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    IPlayerService itsPlayerService;
    private ServiceConnection svcConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.v("", "Service came online");
            itsPlayerService = IPlayerService.Stub.asInterface(binder);
            try {
                itsPlayerService.Play(url, station.Name, station.ID);
            } catch (RemoteException e) {
                Log.e("recv","play error:"+e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.v("", "Service offline");
            itsPlayerService = null;
        }
    };

    private void Play(final Context context, final String stationId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return Utils.getRealStationLink(context, stationId);
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    url = result;

                    Intent anIntent = new Intent(context, PlayerService.class);
                    context.getApplicationContext().bindService(anIntent, svcConn, context.BIND_AUTO_CREATE);
                    context.getApplicationContext().startService(anIntent);
                } else {
                    Toast toast = Toast.makeText(context, context.getResources().getText(R.string.error_station_load), Toast.LENGTH_SHORT);
                    toast.show();
                }
                super.onPostExecute(result);
            }
        }.execute();
    }
}
