package com.CashTrack.CustomComponents;

//container class for friends list
public class Friend implements Comparable<Friend>{
	
	public int confirmed = -1;
	public String username = null;
	public String fname = null;
	public String lname = null;
	public String email = null;
	public String phone = null;
	public int id = -1;
		
	public Friend(String username, String fname, String lname, String email, String phone, int id, int confirmed) {
		this.username = username;
		this.fname = fname;
		this.lname = lname;
		this.email = email;
		this.phone = phone;
		this.id = id;
		this.confirmed = confirmed;
	}
	public Friend(int id, String fname, String lname, int confirmed){
		this.id = id;
		this.fname = fname;
		this.lname = lname;
		this.confirmed = confirmed;
	}
	public Friend(int id){
		this.id = id;
	}
	
	public int compareTo(Friend friend) {
	    int result = fname.compareToIgnoreCase(friend.fname);
	    return result == 0 ? lname.compareTo(((Friend) friend).lname) : result;
	  }
	
	public String toString() {
		if(confirmed == 0) return fname + " " + lname +"*";
		else if(confirmed == 1) return fname + " " + lname +"*";
		else return fname + " " + lname;
	}

	
	public boolean equals(Object o) {
		return o instanceof Friend && ((Friend) o).id == id;
	}
}
