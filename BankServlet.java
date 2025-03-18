package com.bank;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Account class
class Acct implements Serializable {
    private static final long serialVersionUID = 1L;
    private long num;
    private String fName;
    private String lName;
    private float bal;
    private static final float MIN_BAL = 500;

    public Acct(long num, String fname, String lname, float balance) {
        this.num = num;
        this.fName = fname;
        this.lName = lname;
        this.bal = balance;
    }

    public long getNum() { return num; }
    public String getFName() { return fName; }
    public String getLName() { return lName; }
    public float getBal() { return bal; }

    public void deposit(float amt) { bal += amt; }
    public void withdraw(float amt) throws InsufficientFundsException {
        if (bal - amt < MIN_BAL) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        bal -= amt;
    }
}

// Bank class for account management
class Bank {
    private static Bank instance;
    private Map<Long, Acct> accts = new HashMap<>();
    private Connection conn;

    private Bank() {
        conn = DatabaseConfig.getConnection();
    }

    public static synchronized Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    public Acct openAcct(String fname, String lname, float balance) {
        long num = System.currentTimeMillis();
        Acct acct = new Acct(num, fname, lname, balance);
        accts.put(num, acct);
        saveToDatabase(acct);
        return acct;
    }

    public Acct balanceEnq(long num) {
        return accts.get(num);
    }

    public void deposit(long num, float amt) {
        Acct acct = accts.get(num);
        if (acct != null) {
            acct.deposit(amt);
            updateDatabase(acct);
        }
    }

    public void withdraw(long num, float amt) throws InsufficientFundsException {
        Acct acct = accts.get(num);
        if (acct != null) {
            acct.withdraw(amt);
            updateDatabase(acct);
        }
    }

    public void closeAcct(long num) {
        accts.remove(num);
        deleteFromDatabase(num);
    }

    private void saveToDatabase(Acct acct) {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO accounts VALUES (?, ?, ?, ?)");) {
            ps.setLong(1, acct.getNum());
            ps.setString(2, acct.getFName());
            ps.setString(3, acct.getLName());
            ps.setFloat(4, acct.getBal());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDatabase(Acct acct) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE num = ?");) {
            ps.setFloat(1, acct.getBal());
            ps.setLong(2, acct.getNum());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteFromDatabase(long num) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM accounts WHERE num = ?")) {
            ps.setLong(1, num);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Database Configuration
class DatabaseConfig {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "password");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

@WebServlet("/BankServlet")
public class BankServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Bank bank = Bank.getInstance();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        switch (action) {
            case "openAccount":
                openAccount(request, response);
                break;
            case "balanceEnquiry":
                balanceEnquiry(request, response);
                break;
            case "deposit":
                deposit(request, response);
                break;
            case "withdraw":
                withdraw(request, response);
                break;
            case "closeAccount":
                closeAccount(request, response);
                break;
        }
    }
}
