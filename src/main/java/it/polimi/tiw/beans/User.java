package it.polimi.tiw.beans;

import java.util.ArrayList;
import java.util.List;

public class User {
	private int userId;
	private String username;
	private String email;
	private List<String> navigationHistory = new ArrayList<>();
	
	public User() {
		super();
	}
	
	public User(int userId, String username, String email) {
		super();
		this.userId = userId;
		this.username = username;
		this.email = email;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<String> getNavigationHistory() {
		return navigationHistory;
	}

	public void setNavigationHistory(List<String> navigationHistory) {
		this.navigationHistory = navigationHistory;
	}
	
	public String getLastPage() {
		return this.navigationHistory.get(this.navigationHistory.size() - 1);
	}
	
	public void addNewPage(String url) {
		this.navigationHistory.add(url);
	}
}
