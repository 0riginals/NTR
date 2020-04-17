package com.banque.classes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Action implements Serializable {

	private static final long serialVersionUID = 4969106242993203847L;
	
	private int id;
	private int userId;
	private String seller;
	private String description;
	private double price;
	
	public Action() {
		this.userId = -1;
		this.seller = "Default";
		this.description = "Default";
		this.price = 0.;
	}

	public Action(int id, int userid, String seller, String description, double price) {
		this.id = id;
		this.userId = userid;
		this.seller = seller;
		this.description = description;
		this.price = price;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}

}
