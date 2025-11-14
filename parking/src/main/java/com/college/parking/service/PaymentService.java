package com.college.parking.service;

import com.college.parking.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    // Placeholder for a simulated payment gateway URL
    private static final String PAYMENT_GATEWAY_URL = "http://localhost:8080/simulated-payment.html";

    /**
     * Simulates payment initiation and returns the redirect URL.
     * In a real app, this would generate a secure URL from the gateway.
     * @param transaction The pending transaction object.
     * @return The payment URL for redirect, or null if initiation fails.
     */
    public String initiatePayment(Transaction transaction) {
        System.out.println("Initiating payment for Slot: " + transaction.getSlotId() 
                           + " Amount: " + transaction.getAmountPaid() 
                           + " Booking Duration: " + transaction.getDurationHours() + " hours");

        // --- SIMULATION LOGIC ---
        // 99% chance of successful initiation
        if (Math.random() < 0.99) { 
            // Return a simulated redirect URL with transaction details
            return PAYMENT_GATEWAY_URL 
                 + "?txnId=" + transaction.getTransactionId() 
                 + "&amount=" + transaction.getAmountPaid() 
                 + "&slot=" + transaction.getSlotId();
        } else {
            return null; // Initiation failed
        }
    }

    /**
     * Simulates the final payment confirmation step (used by the controller, not the redirect).
     * Since we are redirecting, this logic is now integrated into the controller's success path.
     * For simplicity, we are assuming successful payment upon redirect.
     */
    public boolean confirmPayment(Transaction transaction) {
        // In a real system, this would be a webhook endpoint confirming payment.
        // For this flow, we will assume success after the redirect.
        return true; 
    }
}