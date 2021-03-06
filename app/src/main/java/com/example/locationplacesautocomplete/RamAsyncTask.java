package com.example.locationplacesautocomplete;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by vave on 22/04/2017.
 */
public abstract class RamAsyncTask extends AsyncTask<String, Void, String> {

    public static String FAILURE = "Failure";
    public static String SUCCESS = "Success";
    Context context;
    boolean showProgress = true;
    String progressMessage = "Loading....";
    ProgressDialog progressDialog;
    boolean isUserOffline = false;
    AlertDialog.Builder builder;

    public RamAsyncTask(Context context) {
        super();
        this.context = context;
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressdialoglayout);
        return dialog;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    @Override
    protected String doInBackground(String... urls) {

        String result = FAILURE;
        if (isOnline(context)) {
            try {
                process();
                result = SUCCESS;
            } catch (Exception exception) {
                Log.e("Application", exception.getMessage(), exception);
                Toast.makeText(context,"Internal Error Occurred!", Toast.LENGTH_LONG).show();
            }
        }
        else {
            isUserOffline = true;
        }
        return result;
    }

    public abstract void process();

    public abstract void afterPostExecute();

    @Override
    protected void onPostExecute(String result) {
        /*if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }*/
        try {
            if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        } catch (final IllegalArgumentException e) {
            // Handle or log or ignore
            e.printStackTrace();
        } catch (final Exception e) {
            // Handle or log or ignore
            e.printStackTrace();
        } finally {
            this.progressDialog = null;
        }
        if (isUserOffline){
            if (builder == null){
                builder = new AlertDialog.Builder(context);
                builder.setTitle("Internet");
                builder.setMessage("Please Enable your internet connection.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
                builder.show();
            }
        }
        afterPostExecute();
    }

    @Override
    protected void onPreExecute() {
        if (progressDialog != null) {
            progressDialog = null;
        }
        if (showProgress) {
            progressDialog = createProgressDialog(context);
            progressDialog.show();
        }
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public RamAsyncTask setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
        return this;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

}

