package com.kingteller.client.utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;









import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.services.core.LatLonPoint;
import com.kingteller.R;
import com.kingteller.client.bean.map.StaffLocationBean;
import com.kingteller.client.bean.map.StaffPointBean;



public class MoveUtils {
	// 通过设置间隔时间和距离可以控制速度和图标移动的距离
		private static final int TIME_INTERVAL = 80;
		private static final double DISTANCE = 0.0001;
	/**
	 * 根据点获取图标转的角度
	 */
	public static double getAngle(int startIndex,Polyline mVirtureRoad) {
		if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
			throw new RuntimeException("index out of bonds");
		}
		LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
		LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
		return getAngle(startPoint, endPoint);
	}

	/**
	 * 根据两点算取图标转的角度
	 */
	public static  double getAngle(LatLng fromPoint, LatLng toPoint) {
		double slope = getSlope(fromPoint, toPoint);
		if (slope == Double.MAX_VALUE) {
			if (toPoint.latitude > fromPoint.latitude) {
				return 0;
			} else {
				return 180;
			}
		}
		float deltAngle = 0;
		if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
			deltAngle = 180;
		}
		double radio = Math.atan(slope);
		double angle = 180 * (radio / Math.PI) + deltAngle - 90;
		return angle;
	}

	/**
	 * 根据点和斜率算取截距
	 */
	public static  double getInterception(double slope, LatLng point) {

		double interception = point.latitude - slope * point.longitude;
		return interception;
	}

	/**
	 * 算取斜率
	 */
	public static  double getSlope(int startIndex,Polyline mVirtureRoad) {
		if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
			throw new RuntimeException("index out of bonds");
		}
		LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
		LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
		return getSlope(startPoint, endPoint);
	}

	/**
	 * 算斜率
	 */
	public static  double getSlope(LatLng fromPoint, LatLng toPoint) {
		if (toPoint.longitude == fromPoint.longitude) {
			return Double.MAX_VALUE;
		}
		double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
		return slope;

	}
	
	/**
	 * 计算每次移动的距离
	 */
	public static double getMoveDistance(double slope) {
		if (slope == Double.MAX_VALUE || slope == 0) {
			return DISTANCE;
		}
		return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
	}

	/**
	 * 判断是否为反序
	 * */
	public static boolean isReverse(LatLng startPoint,LatLng endPoint,double slope){
		if(slope==0){
		return	startPoint.longitude>endPoint.longitude;
		}
		return (startPoint.latitude > endPoint.latitude);
		 
	}

	/**
	 * 获取循环初始值大小
	 * */
	public static double getStart(LatLng startPoint,double slope){
		if(slope==0){
			return	startPoint.longitude;
			}
			return  startPoint.latitude;
	}
	
	/**
	 * 获取循环结束大小
	 * */
	public static double getEnd(LatLng endPoint,double slope){
		if(slope==0){
			return	endPoint.longitude;
			}
			return  endPoint.latitude;
	}
	 
	
	/**
	 * 移动
	 * @param mMoveMarker  覆盖物
	 * @param startPoint   开始位置
	 * @param endPoint	        结束位置
	 */
	public static void move(Marker mMoveMarker,LatLng startPoint,LatLng endPoint){
		mMoveMarker
		.setPosition(startPoint);
		mMoveMarker.setRotateAngle((float) getAngle(startPoint,
				endPoint));
		double slope = getSlope(startPoint, endPoint);
		boolean isReverse =isReverse(startPoint, endPoint, slope);	
		double moveDistance = isReverse ? getMoveDistance(slope) : -1 * getMoveDistance(slope);
		double intercept = getInterception(slope, startPoint);
		for(double j=getStart(startPoint, slope); (j > getEnd(endPoint, slope))==isReverse;j = j
				- moveDistance){
			LatLng latLng = null;
            if (slope == 0) {
                latLng = new LatLng(startPoint.latitude, j);
            } else {
                if (slope == Double.MAX_VALUE) {
                    latLng = new LatLng(j, startPoint.longitude);
                } else {
                    latLng = new LatLng(j, (j - intercept) / slope);
                }
            }
            mMoveMarker.setPosition(latLng);
			try {
				Thread.sleep(TIME_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 改变每个员工当前位置
	 * @param aMap  地图
	 * @param data	所有员工
	 */
	public static void staffMove(AMap aMap,List<StaffLocationBean> data){
		List<LatLng> staffLatLngList = new ArrayList<>();
		aMap.clear();//清除覆盖物
		if (data != null && data.size() > 0) {
			for (int i = 0; i < data.size(); i++) {
				StaffLocationBean staffLocationBean = data.get(i);
				Marker mMoveMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.tu_ing))
						.draggable(true));
				List<StaffPointBean> staffPointBeans = staffLocationBean.getuMaplist();
				moveToStaffCenter(aMap, staffLatLngList); //移动到所有员工中心位置
				if (staffPointBeans != null && staffPointBeans.size() > 1) {	
					StaffPointBean startStaffPointBean = staffPointBeans.get(staffPointBeans.size() -2);
					StaffPointBean endStaffPointBean = staffPointBeans.get(staffPointBeans.size() -1);
					LatLng startPoint = new LatLng(startStaffPointBean.getLatItude(), startStaffPointBean.getLongItude());
					LatLng endPoint = new LatLng(endStaffPointBean.getLatItude(), endStaffPointBean.getLongItude());					
					//move(mMoveMarker, startPoint, endPoint);
					mMoveMarker.setPosition(endPoint);
					staffLatLngList.add(endPoint);
					modifyMarkerInfo(mMoveMarker, staffLocationBean, endStaffPointBean);
				}
			}
		}	
	}
	
	/**
	 * 移动到所有员工中心位置
	 * @param aMap
	 * @param staffLatLngList   所有员工最后一次位置
	 */
	public static void moveToStaffCenter(AMap aMap,List<LatLng> staffLatLngList){
		double LatItude = 0d;
		double LongItude = 0d;
		List<Double> LatItudeList = new ArrayList<>();
		List<Double> LongItudeList = new ArrayList<>();
		if (staffLatLngList != null && staffLatLngList.size() > 0) {
			for (int i = 0; i < staffLatLngList.size(); i++) {
				LatItudeList.add(staffLatLngList.get(i).latitude);
				LongItudeList.add(staffLatLngList.get(i).longitude);
			}
		}
		LatItude = mid(LatItudeList);
		LongItude = mid(LongItudeList);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.00d,116.00d), 5));
	}
	
	
	public static double mid(List<Double> number){
		return (double)(min(number) + max(number)) / 2;
	}
	
	public static double min(List<Double> number){
		double min = 0d;
		if (number.size() > 0) {
			min = number.get(0);
			for (int i = 0; i < number.size(); i++) {
				if (number.get(i) < min) {
					min = number.get(i);
				}
			}
		}
		return min;
	}
	
	public static double max(List<Double> number){
		double max = 0d;
		if (number.size() > 0) {
			max = number.get(0);
			for (int i = 0; i < number.size(); i++) {
				if (number.get(i) > max) {
					max = number.get(i);
				}
			}
		}
		return max;
	}
	/**
	 * 修改覆盖物内容
	 * @param nowMaker       覆盖物
	 * @param staffPointBean 当前员工
	 */
	public static void modifyMarkerInfo(Marker nowMaker,StaffLocationBean staffLocationBean , StaffPointBean staffPointBean){
		List<StaffPointBean> staffPointBeans = staffLocationBean.getuMaplist();
		String title = "当前位置";
		String desc = staffLocationBean.getUserName() + "："
				+ staffPointBean.getMonitorDate();				
		if (!KingTellerJudgeUtils.isEmpty(staffPointBean.getOrderNo()) && !staffPointBean.getOrderNo().equals("orderNo")) {
			desc += "正在维护机器" + ":" + staffPointBean.getJqbh() + "\n维护工单号:"
					+ staffPointBean.getOrderNo();
		}
		desc += "\n地址：正在获取中...";
		nowMaker.setAnchor(0.5f, 0.5f);
		nowMaker.setTitle(title);
		nowMaker.setSnippet(desc);
		nowMaker.setDraggable(true);
		nowMaker.setObject(staffPointBean);
	}
}
