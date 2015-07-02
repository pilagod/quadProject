package tw.com.flag.cardboardtest.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import java.net.URISyntaxException;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static String TAG = "CardboardTest";
    private CardboardOverlayView overlayView;

    private SensorManager sensorMgr;
    private float[] accelerometer_values;
    private float[] magnitude_values;

    private boolean first = true;
    private int modifiedX = 0;
    private int preX =  0;

    private Socket mSocket;
    {
        try{
//            mSocket = IO.socket("http://192.168.0.23:3000");
            mSocket = IO.socket("http://172.20.10.14:3000");
        }
        catch (URISyntaxException e){
            Log.d("quad", e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    mSocket.connect();
                }catch (Exception e){
                    Log.d("quad", e.toString());
                }
                sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
                mSocket.on("image", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        Log.d("quad", "get Emit");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap image = BitmapFactory.decodeByteArray((byte[]) args[0], 0, ((byte[]) args[0]).length);
                                overlayView.show3DImage(image);
                            }
                        });
                    }
                });
//
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(sensorMgr.registerListener(listener, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI) &&
                sensorMgr.registerListener(listener, sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI))){
            Log.w("OrientationEx", "sensor not found!");
            sensorMgr.unregisterListener(listener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(listener);
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            StringBuilder sensorInfo = new StringBuilder();
            sensorInfo.append("sensor name: " + sensor.getName() + "\n");
            sensorInfo.append("sensor type: " + sensor.getType() + "\n");
            sensorInfo.append("used power: " + sensor.getPower() + "\n");
            sensorInfo.append("values: \n");

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometer_values = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magnitude_values = event.values.clone();
                    break;
                default:
                    break;
            }

            if (magnitude_values != null && accelerometer_values != null) {
                float[] R = new float[9];
                float[] values = new float[3];
                SensorManager.getRotationMatrix(R, null, accelerometer_values, magnitude_values);
                SensorManager.getOrientation(R, values);
                if (first) {
                    modifiedX = (int) Math.toDegrees(values[0]);
                    first = false;
                }
                for (int i = 0; i < 3; i++) {
                    values[i] = (int) Math.toDegrees(values[i]);
                    if (i == 0) {
                        if (values[0] * preX < 0) {
                            if (values[0] > preX) {
                                values[0] = (values[0] - 360 - preX) + preX;
                            } else {
                                values[0] = (values[0] + 360 - preX) + preX;
                            }
                        }
                        preX = (int) values[0];
                        values[0] = values[0] - modifiedX + 90;
                    }

                }
//                sendArduinoData(values);

                String sendData = "";
                for (int i = 0; i < values.length; i++) {
                    sensorInfo.append("-values[" + i + "] = " + values[i] + "\n");
                    if (sendData != "")
                        sendData += ",";
                    sendData += String.valueOf((int) values[i]);
                }
                sendData += ";";
                Log.d("quad", sendData);
                mSocket.emit("sensorOnChanged", sendData);
//                tvSensorMessage.setText(sensorInfo);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    @Override
    public void onDrawEye(Eye eye) {
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
    }

    @Override
    public void onRendererShutdown() {
    }
}
