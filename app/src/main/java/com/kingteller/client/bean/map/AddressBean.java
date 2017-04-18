package com.kingteller.client.bean.map;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AddressBean  {
	@Id
	private Long id;
	private String address = "";
	private String name;
	private double lat;
	private double lng;
	private String city;
	@Generated(hash = 1341806956)
	public AddressBean(Long id, String address, String name, double lat, double lng,
			String city) {
		this.id = id;
		this.address = address;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.city = city;
	}
	@Generated(hash = 30780671)
	public AddressBean() {
	}
	public Long getId() {
		return this.id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAddress() {
		return this.address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getLat() {
		return this.lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return this.lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getCity() {
		return this.city;
	}
	public void setCity(String city) {
		this.city = city;
	}
}