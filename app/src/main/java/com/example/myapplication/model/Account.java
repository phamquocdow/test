package com.example.myapplication.model;

public class Account {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private long id;
    private String username;
    private String password;
    private String role;

    public Account(long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}
