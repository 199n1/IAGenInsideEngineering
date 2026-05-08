package eci.edu.byteProgramming.ejercicio.paper.util;

import java.util.Date;

public abstract class PaymentMethod implements ValidatePayment{
    protected double amount;
    protected String transactionID;
    protected String customerID;
    protected String currency;
    protected Date timestamp;
    protected PaymentStatus status;
    protected String description;

    public PaymentMethod(double amount, String customerID, String description) {
        this.amount = amount;
        this.customerID = customerID;
        this.description = description;
        this.currency = "USD";
        this.status = PaymentStatus.PENDING;
        this.timestamp = new Date();
        this.transactionID = generateTransactionId();
    }

    public abstract boolean processPayment();
    public abstract String getPaymentMethod();

    protected String generateTransactionId() {
        
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 9999);
        return String.format("TXN%d%04d", timestamp, random);
    }
    
    
    protected String generateTransactionIdWithPrefix(String paymentType) {
        String prefix = getPaymentTypePrefix(paymentType);
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 9999);
        return String.format("%s%d%04d", prefix, timestamp, random);
    }
    
    private String getPaymentTypePrefix(String paymentType) {
        switch(paymentType) {
            case "CREDIT_CARD": return "CC";
            case "PAYPAL": return "PP";
            case "CRYPTO": return "CR";
            default: return "TX";
        }
    }

    public double setAmount(double amount){
        return this.amount = amount;
    }

    public double getAmount() { return amount; }
    public String getTransactionId() { return transactionID; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getCustomerId() { return customerID; }
    public String getDescription() { return description; }
    public Date getTimestamp() { return timestamp; }
}
