package com.kingteller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.kingteller.client.activity.base.BaseMapActivity;
import com.kingteller.client.bean.dao.AddressBeanDao;
import com.kingteller.client.bean.map.AddressBean;
import com.kingteller.client.callback.JsonCallback;
import com.kingteller.client.config.KingTellerStaticConfig;
import com.kingteller.client.service.KingTellerService;
import com.kingteller.client.utils.KingTellerConfigUtils;
import com.kingteller.client.utils.KingTellerJudgeUtils;
import com.kingteller.client.view.dialog.LoginSetUpDialog;
import com.lzy.okgo.OkGo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends BaseMapActivity {
    private AddressBeanDao addressBeanDao;
    private List<AddressBean> addressBeanList;
    private Timer timerCheckUpdatedz = null;
    private LatLng newLatLng;
    private boolean isFirst = true;

   private Handler handler = new Handler(){
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what) {
               case 0:
                   if (isFirst) {
                       aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15));
                       isFirst = false;
                   }/*else if (newLatLng != null) {
                       aMap.moveCamera(CameraUpdateFactory.changeLatLng(newLatLng));
                   }*/
                   mMoveMarker.setPosition(newLatLng);
                   break;
               default:
                   break;
           }
       }
   };
    private double nowLat;
    private double nowLng;
    private MarkerOptions markerOptions;
    private Marker mMoveMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        markerOptions = new MarkerOptions();
        markerOptions.anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.tu_ing))
                .draggable(true);
        mMoveMarker = aMap.addMarker(markerOptions);
        addressBeanDao =  KingTellerApplication.getApplication().getDaoSession().getAddressBeanDao();
    }

    @Override
    public void onResume() {
        super.onResume();
        //启动本地服务
        if (!KingTellerJudgeUtils.isServiceWorked(this, "com.kingteller.client.service.KingTellerService")) {
            startService(new Intent(this, KingTellerService.class));
        }
        if (timerCheckUpdatedz == null)
            timerCheckUpdatedz = new Timer();
        timerCheckUpdatedz.schedule(timerTaskUpdatedz,//监听方法
                KingTellerStaticConfig.SERVICE_LOCATION_TIME * 1000,//开始时 延迟时间
                KingTellerStaticConfig.SERVICE_LOCATION_TIME * 5 * 1000);//执行间隔时间
    }

    private TimerTask timerTaskUpdatedz = new TimerTask() {
        public void run() {
            String url = "http://"+ KingTellerConfigUtils.getIpDomain(MainActivity.this)+":"+KingTellerConfigUtils.getPort(MainActivity.this)+"/Location/LocationList";
            OkGo.get(url)     // 请求方式和请求url
                    .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                    .execute(new JsonCallback<List<AddressBean>>() {
                        @Override
                        public void onSuccess(List<AddressBean> addressBeanList, Call call, Response response) {
                            if (addressBeanList != null && addressBeanList.size() > 0) {
                                location(addressBeanList);
                            }
                        }
                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            Log.i("MainActivity", "onError: 请求失败"+e.getMessage());
                            super.onError(call, response, e);
                        }
                    });
          /*  OkGo.get("http://192.168.32.71:8080/AMapLocation/LocationList")     // 请求方式和请求url
                    .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {
                            // s 即为所需要的结果
                            Log.i(TAG, "onSuccess: " + s);
                        }

                        @Override
                        public void onError(Call call, Response response, Exception e) {
                            Log.i(TAG, "onError: 请求失败"+e.getMessage());
                            super.onError(call, response, e);
                        }
                    });*/
          //  addressBeanList = getAddressBeanList();
            // addressBeanList = addressBeanDao.loadAll();
        }
    };

    private void location(List<AddressBean> addressBeanList) {
        AddressBean addressBean = addressBeanList.get(addressBeanList.size() - 1);
        if (nowLat != addressBean.getLat() || nowLng != addressBean.getLng()) {
            nowLat = addressBean.getLat();
            nowLng = addressBean.getLng();
            newLatLng = new LatLng(nowLat, nowLng);
            handler.sendEmptyMessage(0);
        }
    }

    public List<AddressBean> getAddressBeanList() {
        return addressBeanList;
    }

    public void setting(View view) {
        new LoginSetUpDialog(MainActivity.this, R.style.Login_dialog).show();// 设置服务器
    }
}
