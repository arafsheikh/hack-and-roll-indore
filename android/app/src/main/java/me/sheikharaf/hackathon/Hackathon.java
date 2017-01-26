package me.sheikharaf.hackathon;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import me.sheikharaf.hackathon.activities.MainActivity;
import me.sheikharaf.hackathon.services.CameraService;

public class Hackathon extends Application implements BootstrapNotifier {
    private static final String TAG = ".Hackathon";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MainActivity monitoringActivity = null;
    private Identifier[] lastSeenBeacons = new Identifier[3];


    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24")); // iBeacon

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    @Override
    public void didEnterRegion(Region arg0) {
        sendToServer(arg0);

        if (lastSeenBeacons.length == 0) {
            // This is the first time we are seeing any beacon so we store all nearby beacons
            lastSeenBeacons[0] = arg0.getId1();
            lastSeenBeacons[1] = arg0.getId2();
            lastSeenBeacons[2] = arg0.getId3();
        } else {
            // Compare if we've hit two beacons in vicinity
            for (Identifier identifier: lastSeenBeacons) {
                if (identifier == null) continue;
                if (identifier.toString().equals(arg0.getUniqueId())) {
                    // LOOK UP!
                    lookup();
                    lastSeenBeacons[0] = arg0.getId1();
                    lastSeenBeacons[1] = arg0.getId2();
                    lastSeenBeacons[2] = arg0.getId3();
                }
            }
        }
    }

    /**
     * Send raw data to server for detailed tracking and analysis.
     * @param arg0 Region where the user entered.
     */
    private void sendToServer(Region arg0) {
    }

    private void lookup() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        final WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params.windowAnimations = android.R.style.Animation_Dialog;

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        final View view = Utils.blink(inflater.inflate(R.layout.popup, null), 200, 0);
        wm.addView(view, params);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.removeView(view);
            }
        }, 4000);

    }

    @Override
    public void didExitRegion(Region region) {
        if (monitoringActivity != null) {
            //monitoringActivity.logToDisplay("I no longer see a beacon.");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        if (monitoringActivity != null) {
            //monitoringActivity.logToDisplay("I have just switched from seeing/not seeing beacons: " + state);
        }
    }

    public void setMonitoringActivity(MainActivity activity) {
        this.monitoringActivity = activity;
    }

}