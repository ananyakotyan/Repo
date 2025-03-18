import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String msg) {
        super(msg);
    }
}

class Acct implements Serializable {
    private static final long serialVersionUID = 1L;
    private long num;
    private String fName;
    private String lName;
    private float bal;
    private static long nextNum = 1;
    private static final float MIN_BAL = 500;

    public Acct(String fname, String lname, float balance) {
        this.num = nextNum++;
        this.fName = fname;
        this.lName = lname;
        this.bal = balance;
    }

    public long getNum() {
        return num;
    }

    public String getFName() {
        return fName;
    }

    public String getLName() {
        return lName;
    }

    public float getBal() {
        return bal;
    }

    public void deposit(float amt) {
        bal += amt;
    }

    public void withdraw(float amt) throws InsufficientFundsException {
        if (bal - amt < MIN_BAL) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        bal -= amt;
    }

    @Override
    public String toString() {
        return "Account Number: " + num +
               "\nFirst Name: " + fName +
               "\nLast Name: " + lName +
               "\nBalance: " + bal;
    }
}

class Bank {
    private Map<Long, Acct> accts = new HashMap<>();

    public Acct openAcct(String fname, String lname, float balance) {
        Acct acct = new Acct(fname, lname, balance);
        accts.put(acct.getNum(), acct);
        saveAccts();
        return acct;
    }

    public Acct balanceEnq(long num) {
        Acct acct = accts.get(num);
        if (acct == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        return acct;
    }

    public Acct deposit(long num, float amt) {
        Acct acct = accts.get(num);
        if (acct == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        acct.deposit(amt);
        saveAccts();
        return acct;
    }

    public Acct withdraw(long num, float amt) throws InsufficientFundsException {
        Acct acct = accts.get(num);
        if (acct == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        acct.withdraw(amt);
        saveAccts();
        return acct;
    }

    public void closeAcct(long num) {
        if (accts.remove(num) == null) {
            throw new IllegalArgumentException("Account not found.");
        }
        saveAccts();
    }

    public void showAllAccts(JTextArea area) {
        area.setText("");
        for (Acct acct : accts.values()) {
            area.append(acct.toString() + "\n\n");
        }
    }

    private void saveAccts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Bank.data"))) {
            oos.writeObject(accts);
        } catch (IOException e) {
            System.out.println("Error saving accts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadAccts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Bank.data"))) {
            accts = (Map<Long, Acct>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
    }
}

public class BankAppUI extends JFrame {
    private Bank bank = new Bank();
    private JTextArea area;

    public BankAppUI() {
        bank.loadAccts();
        setTitle("Banking System");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        area = new JTextArea();
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        JButton openAcctBtn = new JButton("Open Account");
        JButton balanceEnqBtn = new JButton("Balance Enquiry");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton closeAcctBtn = new JButton("Close Account");
        JButton showAllAcctsBtn = new JButton("Show All Accounts");

        panel.add(openAcctBtn);
        panel.add(balanceEnqBtn);
        panel.add(depositBtn);
        panel.add(withdrawBtn);
        panel.add(closeAcctBtn);
        panel.add(showAllAcctsBtn);

        add(panel, BorderLayout.SOUTH);

        openAcctBtn.addActionListener(e -> openAcct());
        balanceEnqBtn.addActionListener(e -> balanceEnq());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        closeAcctBtn.addActionListener(e -> closeAcct());
        showAllAcctsBtn.addActionListener(e -> showAllAccts());

        setVisible(true);
    }

    private void openAcct() {
        String fname = JOptionPane.showInputDialog("Enter First Name:");
        String lname = JOptionPane.showInputDialog("Enter Last Name:");
        float balance = Float.parseFloat(JOptionPane.showInputDialog("Enter Initial Balance:"));
        Acct acct = bank.openAcct(fname, lname, balance);
        area.setText("Account created successfully!\n" + acct);
    }

    private void balanceEnq() {
        long num = Long.parseLong(JOptionPane.showInputDialog("Enter Account Number:"));
        try {
            area.setText(bank.balanceEnq(num).toString());
        } catch (IllegalArgumentException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void deposit() {
        long num = Long.parseLong(JOptionPane.showInputDialog("Enter Account Number:"));
        float amt = Float.parseFloat(JOptionPane.showInputDialog("Enter Amount:"));
        try {
            area.setText("Amount deposited successfully!\n" + bank.deposit(num, amt));
        } catch (IllegalArgumentException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void withdraw() {
        long num = Long.parseLong(JOptionPane.showInputDialog("Enter Account Number:"));
        float amt = Float.parseFloat(JOptionPane.showInputDialog("Enter Amount:"));
        try {
            area.setText("Amount withdrawn successfully!\n" + bank.withdraw(num, amt));
        } catch (InsufficientFundsException | IllegalArgumentException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void closeAcct() {
        long num = Long.parseLong(JOptionPane.showInputDialog("Enter Account Number:"));
        try {
            bank.closeAcct(num);
            area.setText("Account closed successfully.");
        } catch (IllegalArgumentException e) {
            area.setText("Error: " + e.getMessage());
        }
    }

    private void showAllAccts() {
        bank.showAllAccts(area);
    }

    public static void main(String[] args) {
        new BankAppUI();
    }
}
