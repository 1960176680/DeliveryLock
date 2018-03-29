package com.atfeeling.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.atfeeling.adapter.deviceAdapter;
import com.atfeeling.beans.device;
import com.atfeeling.cb.cb;
import com.atfeeling.cb.event.e;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener{

    //获取门锁单例对象
    private cb lock = cb.getLock();
    private Button scan;
    private ListView listview;
    public deviceAdapter adapter;
    private String connecteddevice ="";
    Handler updateUiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lock.startWork(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = (ListView) findViewById(R.id.listview);
        scan = (Button) findViewById(R.id.scan);
        adapter = new deviceAdapter(this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            lock.scan(new e.scan() {
                @Override
                public void onFound(HashMap<Object, Object> map) {
                    String deviceId = (String) map.get("deviceId");
                    String name = (String) map.get("name");
                    String rssi = "信号:" + map.get("rssi");
                    String electricity = "电量"+map.get("electricity");
                    String isOpen = ((Integer)map.get("isOpen") == 1)?"门开着":"门关着";
                    boolean canbeadd = true;
                    for (int i = 0; i < adapter.arr.size(); i++) {
                        if (adapter.arr.get(i).getDeviceId().equals(deviceId)) {
                            adapter.arr.get(i).setElectricity(electricity);
                            adapter.arr.get(i).setIsOpen(isOpen);
                            adapter.arr.get(i).setRssi(rssi);
                            canbeadd = false;
                            break;
                        }
                    }
                    if (canbeadd) {
                        adapter.arr.add(new device(name,deviceId,rssi,"未连接",electricity,isOpen));

                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(int errno) {
                    if (errno ==-1){
                        //手机不支持蓝牙
                    }

                    if(errno == -2){
                        //蓝牙未打开
                        if (lock.changeState(true)){
                            //如果用户允许直接打开蓝牙，就发起扫描
                            lock.scan(null);
                        }else{
                            //如果蓝牙没有被打开，需要提示用户去打开蓝牙
                        };
                    }

                }
            });

            }
        });

        class UpdateUi implements Runnable{
            private int position;
            private String state;

            UpdateUi(int position,String state){
                this.position = position;
                this.state = state;
            }

            @Override
            public void run() {
                //更新UI
                adapter.arr.get(position).setState(state);
                adapter.notifyDataSetChanged();
            }

        }
        lock.registerState(new e.state() {
            @Override
            public void onConnected(String deviceId) {
                //门锁建立连接事件
                connecteddevice =deviceId;
                for (int i = 0; i < adapter.arr.size(); i++) {
                    if (adapter.arr.get(i).getDeviceId().equals(deviceId)) {
                        UpdateUi updateUi = new UpdateUi(i,"已连接");
                        updateUiHandler.post(updateUi);
                    }else{
                        if (adapter.arr.get(i).getState() == "已连接"){
                            UpdateUi updateUi = new UpdateUi(i,"未连接");
                            updateUiHandler.post(updateUi);
                        }
                    }
                }
            }

            @Override
            public void onDisconnected(String deviceId) {
                //门锁断开连接事件
                connecteddevice ="";
                for (int i = 0; i < adapter.arr.size(); i++) {
                        if (adapter.arr.get(i).getState() == "已连接"){
                            UpdateUi updateUi = new UpdateUi(i,"未连接");
                            updateUiHandler.post(updateUi);
                        }
                }
            }

            @Override
            public void onNoMsgBack(int times) {
                //门锁通讯异常的时候，超过一定的阈值可以认为通讯异常，要断开门锁。和提醒用户开门失败
                if (times>10){
                    Log.i("atfeeling","蓝牙通讯异常");
                    lock.disconnect();
                }
            }
        });


    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
        if (connecteddevice.equals(adapter.arr.get(position).getDeviceId())){
            lock.disconnect();
        }else{
            lock.connect(adapter.arr.get(position).getDeviceId(), new e.connect() {
                @Override
                public void onSuccess() {
                    // 连接上门锁之后开门
                    Thread t = new Thread(new Runnable(){
                        public void run(){
                            //先根据mac地址去服务器上去拿keya，
                            String mac = adapter.arr.get(position).getDeviceId();
                            String keya = doget(mac);
                            if (keya != null){
                                // f2d 指令是 开门指令，需要传入keya，注意把keya 转换成byte[]数组，可以调用lock.hex （）进行转换
                                lock.f2d(lock.hex(keya),new e.res() {
                                    @Override
                                    public void onSuccess(byte[] res) {
                                        //成功开门后要断开与门锁的连接
                                        lock.disconnect();
                                    }
                                });
                            }else{
                                //如果服务器上没有与mac对应的key，那么就去跟门锁动态生成key，传入 0x34 是 生成开门的keya，传入0x30 是生成 keyc，keyc的作用是重置keya，用于keya忘记，或被泄漏的情景
                                //切记，这种开门方式在门锁正式上线后是不能使用的，因为门锁会拒绝动态生成
                                lock.makeKEY(0x34,new e.res() {
                                    @Override
                                    public void onSuccess(byte[] keya) {
                                        if (keya!=null){
                                            lock.f2d(keya,new e.res() {
                                                @Override
                                                public void onSuccess(byte[] res) {
                                                    lock.disconnect();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                    t.start();
                }
            });
        }
    }

    public static String doget(String MAC){
        try {
            URL url = new URL("https://xcxapi.sijiabox.com/sjyz/sjyzInterfaceOutSaleman/sjyzInterfaceOutSalemanAction!getKeyaByMAC.action?params={'maccode':'"+MAC+"'}");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(5000);
            if(200 == urlConnection.getResponseCode()){
                //得到输入流
                InputStream is =urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while(-1 != (len = is.read(buffer))){
                    baos.write(buffer,0,len);
                    baos.flush();
                }
                String objstr = baos.toString("utf-8");
                JSONObject obj = new JSONObject(objstr);
                if (obj!=null && obj.getBoolean("success")){
                    return obj.getString("results");
                }
            }
        }  catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
        return null;
    }
}
