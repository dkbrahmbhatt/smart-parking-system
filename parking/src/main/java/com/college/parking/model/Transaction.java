package com.college.parking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
	private Long id;

	private String transactionId; // Unique ID from payment gateway (simulated)
	private String slotId;
	private String vehiclePlate;
	private String mobileNumber; // NEW FIELD: User's mobile number
	private LocalDateTime bookingTime;
	private double amountPaid;
	private int durationHours; // NEW FIELD: User's selected booking duration
	private String paymentStatus; // e.g., SUCCESS, FAILED, PENDING

	// Constructor/Methods for convenience
	public Transaction() {
	}

	// UPDATED CONSTRUCTOR
	public Transaction(String slotId, String vehiclePlate, String mobileNumber, double amountPaid, int durationHours) {
		this.slotId = slotId;
		this.vehiclePlate = vehiclePlate;
		this.mobileNumber = mobileNumber;
		this.amountPaid = amountPaid;
		this.durationHours = durationHours;
		this.bookingTime = LocalDateTime.now();
		this.transactionId = java.util.UUID.randomUUID().toString(); // Generate unique ID
		this.paymentStatus = "PENDING";
	}

	// --- Getters and Setters ---

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getSlotId() {
		return slotId;
	}

	public void setSlotId(String slotId) {
		this.slotId = slotId;
	}

	public String getVehiclePlate() {
		return vehiclePlate;
	}

	public void setVehiclePlate(String vehiclePlate) {
		this.vehiclePlate = vehiclePlate;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public LocalDateTime getBookingTime() {
		return bookingTime;
	}

	public void setBookingTime(LocalDateTime bookingTime) {
		this.bookingTime = bookingTime;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(double amountPaid) {
		this.amountPaid = amountPaid;
	}

	public int getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(int durationHours) {
		this.durationHours = durationHours;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
}