package com.college.parking.controller;

import com.college.parking.model.ParkingSlot;
import com.college.parking.model.Transaction;
import com.college.parking.model.dto.BookingRequest; // <-- UPDATED IMPORT
import com.college.parking.repository.ParkingRepository;
import com.college.parking.repository.TransactionRepository;
import com.college.parking.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*") // Allows access from the frontend on a different port
@RequestMapping("/api/parking")
public class ParkingController {

    @Autowired
    private ParkingRepository repo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private PaymentService paymentService;

    // =================================================================
    // USER API ENDPOINTS (READ & BOOK)
    // =================================================================

    // Feature 1: Real-time Availability
    @GetMapping("/status")
    public List<ParkingSlot> getStatus() {
        return repo.findAll(Sort.by("slotId"));
    }

    // Feature 2 & 3: Online Booking & Payment Redirection
    @PostMapping("/book/{slotId}")
    public ResponseEntity<String> bookSlot(@PathVariable String slotId, @RequestBody BookingRequest request) {
        Optional<ParkingSlot> optionalSlot = repo.findById(slotId);

        if (optionalSlot.isPresent()) {
            ParkingSlot slot = optionalSlot.get();
            if (slot.isOccupied()) {
                return new ResponseEntity("Slot " + slotId + " is already booked.", HttpStatus.BAD_REQUEST);
            }
            
            // --- 1. Calculate Final Price Based on Duration & Base Hourly Rate ---
            double finalPrice = slot.getBaseHourlyPrice() * request.durationHours;
            
            // --- 2. Create Pending Transaction ---
            Transaction pendingTransaction = new Transaction(
                slotId, 
                request.vehiclePlate, 
                request.mobileNumber, 
                finalPrice, 
                request.durationHours
            );
            transactionRepo.save(pendingTransaction); 

            // --- 3. Initiate Payment and Get Redirect URL ---
            String paymentUrl = paymentService.initiatePayment(pendingTransaction);

            if (paymentUrl != null) {
                // Update non-essential details in the slot before payment
                slot.setMobileNumber(request.mobileNumber); 
                repo.save(slot);

                // Return the URL. Frontend will perform the redirect.
                return new ResponseEntity(paymentUrl, HttpStatus.OK);

            } else {
                // Initiation failed
                pendingTransaction.setPaymentStatus("INITIATION_FAILED");
                transactionRepo.save(pendingTransaction); 
                return new ResponseEntity("Payment initiation failed. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity("Slot not found.", HttpStatus.NOT_FOUND);
    }

    // NEW Endpoint: To be called by the SIMULATED payment page upon successful payment
    @PutMapping("/confirm-payment/{txnId}")
    public ResponseEntity<String> confirmPayment(@PathVariable String txnId) {
        // You would typically need a custom repository method like findByTransactionId
        // Assuming this method exists on TransactionRepository for the sake of completion:
        Optional<Transaction> optionalTransaction = transactionRepo.findByTransactionId(txnId); // Assuming this method exists

        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            Optional<ParkingSlot> optionalSlot = repo.findById(transaction.getSlotId());

            if (optionalSlot.isPresent()) {
                ParkingSlot slot = optionalSlot.get();

                // 1. Mark Slot as Occupied
                slot.setOccupied(true);
                slot.setStartTime(LocalDateTime.now());
                slot.setTransactionId(txnId);
                // Note: VehiclePlate and MobileNumber are already available from the transaction object
                repo.save(slot);

                // 2. Mark Transaction as Success
                transaction.setPaymentStatus("SUCCESS");
                transactionRepo.save(transaction);
                
                return new ResponseEntity("SUCCESS: Slot booked and payment confirmed.", HttpStatus.OK);
            }
        }
        return new ResponseEntity("Error: Transaction or Slot not found/invalid.", HttpStatus.BAD_REQUEST);
    }


    // =================================================================
    // ADMIN API ENDPOINTS (REPORTING & CRUD)
    // =================================================================

    // Feature 5 & 7: Admin Report (Metrics and Real Revenue)
    @GetMapping("/admin/report")
    public Object getAdminReport() {
        List<ParkingSlot> slots = repo.findAll();
        long totalSlots = slots.size();
        long available = slots.stream().filter(s -> !s.isOccupied()).count();

        // Calculate REAL revenue from successful transactions (Feature 7)
        double realRevenue = transactionRepo.findAll().stream()
                               .filter(t -> "SUCCESS".equals(t.getPaymentStatus()))
                               .mapToDouble(Transaction::getAmountPaid)
                               .sum();

        // Return an anonymous object for easy JSON serialization
        return new Object() {
            public long getTotalSlots() { return totalSlots; }
            public long getAvailableSlots() { return available; }
            public double getRealRevenue() { return realRevenue; }
            public List<ParkingSlot> getOccupiedSlots() { return slots.stream().filter(ParkingSlot::isOccupied).collect(Collectors.toList()); }
        };
    }

    // Feature 7: Get Transaction History
    @GetMapping("/admin/transactions")
    public List<Transaction> getAllTransactions() {
        // Sort by booking time descending (most recent first)
        return transactionRepo.findAll(Sort.by(Sort.Direction.DESC, "bookingTime"));
    }

    // Feature: Admin to create a new slot (CRUD - Create)
    @PostMapping("/admin/slots")
    public ResponseEntity<ParkingSlot> createSlot(@RequestBody ParkingSlot newSlot) {
        if (repo.existsById(newSlot.getSlotId())) {
            return new ResponseEntity("Slot ID " + newSlot.getSlotId() + " already exists.", HttpStatus.BAD_REQUEST);
        }
        newSlot.setOccupied(false);
        if (newSlot.getBaseHourlyPrice() == 0.0) {
            newSlot.setBaseHourlyPrice(20.0);
        }
        ParkingSlot savedSlot = repo.save(newSlot);
        return new ResponseEntity<>(savedSlot, HttpStatus.CREATED);
    }

    // Feature: Admin to remove a slot (CRUD - Delete)
    @DeleteMapping("/admin/slots/{slotId}")
    public String removeSlot(@PathVariable String slotId) {
        Optional<ParkingSlot> optionalSlot = repo.findById(slotId);
        
        if (optionalSlot.isPresent()) {
            ParkingSlot slot = optionalSlot.get();

            // Safety check: Cannot remove an occupied slot
            if (slot.isOccupied()) {
                return "Error: Slot " + slotId + " is currently occupied and cannot be removed.";
            }

            repo.deleteById(slotId);
            return "Slot " + slotId + " successfully removed.";
        } else {
            return "Error: Slot " + slotId + " not found.";
        }
    }

    // Feature 5: Admin to manually release (un-book) a slot (CRUD - Update)
    @PutMapping("/admin/release/{slotId}")
    public String releaseSlot(@PathVariable String slotId) {
        return repo.findById(slotId)
            .map(slot -> {
                if (!slot.isOccupied()) {
                    return "Slot " + slotId + " is already available.";
                }
                slot.setOccupied(false);
                slot.setVehiclePlate(null);
                slot.setStartTime(null);
                slot.setTransactionId(null);
                slot.setMobileNumber(null); // Clear mobile number too
                repo.save(slot);
                return "Slot " + slotId + " successfully released.";
            })
            .orElse("Error: Slot " + slotId + " not found.");
    }
}