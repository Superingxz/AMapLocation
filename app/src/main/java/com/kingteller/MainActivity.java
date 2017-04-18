package com.kingteller;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.kingteller.client.activity.base.BaseMapActivity;
import com.kingteller.client.bean.dao.AddressBeanDao;
import com.kingteller.client.bean.dao.DaoSession;
import com.kingteller.client.bean.map.AddressBean;
import com.kingteller.client.config.KingTellerStaticConfig;
import com.kingteller.client.service.KingTellerService;
import com.kingteller.client.utils.KingTellerJudgeUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


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
                   }else if (newLatLng != null) {
                       float zoom = aMap.getCameraPosition().zoom;
                       aMap.moveCamera(CameraUpdateFactory.changeLatLng(newLatLng));
                   }
                   break;
               default:
                   break;
           }
       }
   };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                KingTellerStaticConfig.SERVICE_LOCATION_TIME * 2 * 1000);//执行间隔时间
    }

    private TimerTask timerTaskUpdatedz = new TimerTask() {
        public void run() {
            addressBeanList = addressBeanDao.loadAll();
            if (addressBeanList != null && addressBeanList.size() > 0) {
                location(aMap,addressBeanList);
            }
        }
    };

    private void location(AMap aMap, List<AddressBean> addressBeanList) {
        aMap.clear();
        Marker mMoveMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.tu_ing))
                .draggable(true));
        AddressBean addressBean = addressBeanList.get(addressBeanList.size() - 1);
        newLatLng = new LatLng(addressBean.getLat(),addressBean.getLng());
        mMoveMarker.setPosition(newLatLng);
        handler.sendEmptyMessage(0);
    }
}
