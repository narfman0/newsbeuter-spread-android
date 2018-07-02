package org.atlaslabs.newsbeuterspread.network;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.Disposable;

public class SSHUtil {
    private static final String TAG = SSHUtil.class.getSimpleName();

    public static int parseRemoteHost(String host){
        try {
            if (host.contains(":"))
                return Integer.parseInt(host.substring(host.lastIndexOf(":") + 1));
        }catch (Exception e){
            Log.w(TAG, "Error parsing port from host: " + host);
        }
        return 80;
    }

    public static Disposable connect(Context context, String host, String username,
                              String password) throws JSchException{
        int localPort = 49152;
        JSch jsch=new JSch();

        Session session = jsch.getSession(username, host, 22);

        UserInfo ui = new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public boolean promptPassword(String message) {
                return true;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return false;
            }

            @Override
            public boolean promptYesNo(String message) {
                return true;
            }

            @Override
            public void showMessage(String message) {
                Log.w(TAG, message);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        };
        session.setUserInfo(ui);
        session.connect();

        int remotePort = parseRemoteHost(host);
        session.setPortForwardingL(localPort, host, remotePort);

        return new Disposable() {
            @Override
            public void dispose() {
                session.disconnect();
            }

            @Override
            public boolean isDisposed() {
                return !session.isConnected();
            }
        };
    }

    public boolean buildYesNoDialog(Context context, String message){
        AtomicBoolean result = new AtomicBoolean();
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    result.set(true);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    result.set(false);
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
        return result.get();
    }
}
