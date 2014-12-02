package com.romcessed.romsearch;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Closeable;

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept before
 * using the application. Your application should call {@link EULA#show(android.app.Activity)}
 * in the onCreate() method of the first activity. If the user accepts the EULA, it will never
 * be shown again. If the user refuses, {@link android.app.Activity#finish()} is invoked
 * on your activity.
 */
public class EULA {
    private static final String ASSET_EULA = "EULA";
    private static final String PREFERENCES_EULA = "eula";

    /**
     * callback to let the activity know when the user has accepted the EULA.
     */
    static interface OnEulaAgreedTo {

        /**
         * Called when the user has accepted the eula and the dialog closes.
         */
        void onEulaAgreedTo();
    }

    public static boolean show(EULAcapsule e){
    	if(e==null){
    		return false;
    	}
    	return show(e.getActivity(), e.getEULATitle(), e.getButtonAccept(), e.getButtonRefuse(), e.getEULAIdentifier(), e.getEulaXMLString());
    }
    
    /**
     * Displays the EULA if necessary. This method should be called from the onCreate()
     * method of your main Activity.
     *
     * @param activity The Activity to finish if the user rejects the EULA.
     * @return Whether the user has agreed already.
     */
    public static boolean show(final Activity activity, String EULATitle, String ButtonAccept, String ButtonRefuse, final String EULAIdentifier, int EulaXMLString) {
    	final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA,
                Activity.MODE_PRIVATE);
        if (!preferences.getBoolean("eula." + EULAIdentifier + ".accepted", false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(EULATitle);
            builder.setCancelable(true);
            builder.setPositiveButton(ButtonAccept, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    accept(preferences, EULAIdentifier);
                    if (activity instanceof OnEulaAgreedTo) {
                        ((OnEulaAgreedTo) activity).onEulaAgreedTo();
                    }
                }
            });
            builder.setNegativeButton(ButtonRefuse, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    refuse(activity);
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    refuse(activity);
                }
            });
            builder.setMessage(readEula(activity, EULAIdentifier, EulaXMLString));
            builder.create().show();
            return false;
        }
        return true;
    }

    private static void accept(SharedPreferences preferences, String EULAIdentifier) {
        preferences.edit().putBoolean("eula." + EULAIdentifier + ".accepted", true).commit();
    }

    private static void refuse(Activity activity) {
        activity.finish();
    }

    private static CharSequence readEula(Activity activity, String EULAIdentifier, int EulaXMLString) {
      return activity.getResources().getString(EulaXMLString);
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    

}
