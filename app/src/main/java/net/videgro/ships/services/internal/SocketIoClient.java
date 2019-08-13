package net.videgro.ships.services.internal;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.videgro.crypt.DigitalSignature;
import net.videgro.ships.Analytics;
import net.videgro.ships.NmeaTO;
import net.videgro.ships.tasks.domain.SocketIoConfig;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoClient {
    private static final String TAG = "SocketIoClient";

    private final Context context;
    private final SocketIoListener listener;
    private final SocketIoConfig config;
    private final DigitalSignature digitalSignature;

    private static final boolean IGNORE_INVALID_SIGNATURE=true;

    private int repeatedNmeaMessages = 0;

    private Socket socket;

    public SocketIoClient(final Context context,final Resources resources, final SocketIoListener listener, final SocketIoConfig config) {
        this.context=context;

        if (listener == null) {
            throw new IllegalArgumentException("Listener can not be null.");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration can not be null.");
        }
        this.listener = listener;
        this.config = config;
        this.digitalSignature = new DigitalSignature(resources);
    }

    public boolean connect() {
        boolean result = false;
        if (socket == null) {
            try {
                socket = IO.socket(config.getServer());
                socket.on(config.getTopic(), onNewMessage);
                socket.connect();
                result = true;
            } catch (URISyntaxException e) {
                socket = null;
                Log.e(TAG, "Not possible to connect to SocketIO server", e);
            }
        } else {
            throw new IllegalArgumentException("Connect - Socket is not null.");
        }

        return result;
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off(config.getTopic(), onNewMessage);
            socket = null;
            Analytics.logEvent(context,Analytics.CATEGORY_NMEA_REPEAT, "Number of messages repeated", String.valueOf(repeatedNmeaMessages));
        } else {
            throw new IllegalArgumentException("Disconnect - Socket is already/still null.");
        }
    }

    public boolean isConnected() {
        // Assume when socket is not null, we are connected
        return socket != null;
    }

    public void requestServerCachedMessages() {
        if (socket != null) {
            socket.emit(NmeaTO.TOPIC_NMEA_RETRANSMIT, config.getAndroidId());
        }
    }

    public boolean repeatToSocketIoServer(final String line) {
        final String tag = "repeatToSocketIoServer - ";
        boolean result = false;
        if (socket != null) {
            final String androidId = config.getAndroidId();
            final String timestamp = String.valueOf(System.currentTimeMillis());
            final String dataToSign = androidId + NmeaTO.TOPIC_NMEA_SIGN_SEPARATOR + timestamp + NmeaTO.TOPIC_NMEA_SIGN_SEPARATOR + line;
            final String signature = digitalSignature.sign(dataToSign);
            if (signature != null) {
                final NmeaTO nmeaTO = new NmeaTO();
                nmeaTO.setOrigin(androidId);
                nmeaTO.setTimestamp(timestamp);
                nmeaTO.setData(line);
                nmeaTO.setSignature(signature);

                final String json = new Gson().toJson(nmeaTO);
                socket.emit(config.getTopic(), json);
                repeatedNmeaMessages++;
                result = true;
            } else {
                Log.e(TAG, tag + "Not possible to sign data.");
            }
        } else {
            Log.e(TAG, tag + "SocketIO is not set.");
        }
        return result;
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final String tag = "onNewMessage - ";
            if (args != null && args.length == 1 && args[0] instanceof String) {

                try {
                    final NmeaTO nmeaTO = new Gson().fromJson((String) args[0], NmeaTO.class);

                    if (nmeaTO.getOrigin() != null && !nmeaTO.getOrigin().equals(config.getAndroidId())) {
                        // Ignoring bad formatted and own messages

                        final String dataToVerify = nmeaTO.getOrigin() + NmeaTO.TOPIC_NMEA_SIGN_SEPARATOR + nmeaTO.getTimestamp() + NmeaTO.TOPIC_NMEA_SIGN_SEPARATOR + nmeaTO.getData();

                        if (IGNORE_INVALID_SIGNATURE || digitalSignature.verify(dataToVerify, nmeaTO.getSignature())) {
                            listener.onNmeaViaSocketIoReceived(nmeaTO.getData());
                        } else {
                            Log.w(TAG, tag + "Invalid data received via SocketIO (invalid signature).");
                        }
                    }
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, tag, e);
                }
            } else {
                Log.w(TAG, tag + "Invalid message received via SocketIO (format).");
            }

        }
    };

    public interface SocketIoListener {
        void onNmeaViaSocketIoReceived(final String nmea);
    }
}
