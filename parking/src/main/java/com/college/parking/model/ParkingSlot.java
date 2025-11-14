package com.college.parking.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ParkingSlot {

    @Id
    private String slotId;
    private boolean isOccupied;
    private String vehiclePlate;
    private double baseHourlyPrice; // Base price per hour for dynamic calculation
    private String mobileNumber; 

    private LocalDateTime startTime;
    private String transactionId; 

    public ParkingSlot() {
        this.isOccupied = false;
        this.baseHourlyPrice = 20.0; // Default base hourly rate
    }

    public ParkingSlot(String slotId, double baseHourlyPrice) {
        this.slotId = slotId;
        this.isOccupied = false;
        this.baseHourlyPrice = baseHourlyPrice;
    }

    // --- Getters and Setters ---

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
    
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    
    public double getBaseHourlyPrice() { return baseHourlyPrice; }
    public void setBaseHourlyPrice(double baseHourlyPrice) { this.baseHourlyPrice = baseHourlyPrice; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}