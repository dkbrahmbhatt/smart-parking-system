const BASE_URL = 'http://localhost:8080/api/parking';

// =================================================================
// 1. DATA FETCHING & RENDERING
// =================================================================

/**
 * Fetches the current status of all parking slots from the backend.
 */
async function fetchParkingStatus() {
    try {
        const response = await fetch(`${BASE_URL}/status`); 
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const slots = await response.json();
        renderSlots(slots);
    } catch (error) {
        console.error('Error fetching parking status:', error);
        document.getElementById('parking-slots-grid').innerHTML = '<p class="message error">Cannot connect to Java backend. Is the server running?</p>';
    }
}

/**
 * Renders the parking slot cards in the grid and updates the availability count.
 */
function renderSlots(slots) {
    const container = document.getElementById('parking-slots-grid');
    container.innerHTML = ''; // Clear existing slots
    let availableCount = 0;

    slots.forEach(slot => {
        const slotEl = document.createElement('div');
        // Determine the class based on occupancy status
        slotEl.className = `parking-slot-card ${slot.occupied ? 'booked' : 'available'}`;
        
        slotEl.innerHTML = `
            <span class="slot-id">${slot.slotId}</span>
            <span class="slot-price">Base ₹${slot.baseHourlyPrice.toFixed(2)}/hr</span>
        `;
        slotEl.title = slot.occupied 
            ? `Booked by ${slot.vehiclePlate || 'N/A'}` 
            : `Available! Click to book. Base Price: ₹${slot.baseHourlyPrice.toFixed(2)}/hr`;

        if (!slot.occupied) {
            slotEl.addEventListener('click', () => {
                // Auto-fill the Slot ID in the booking form when a slot is clicked
                document.getElementById('slotId').value = slot.slotId;
                // Scroll to booking form for a better user experience
                document.getElementById('booking-section').scrollIntoView({ behavior: 'smooth' });
            });
            availableCount++;
        }
        container.appendChild(slotEl);
    });
    
    const totalSlots = slots.length;
    document.getElementById('total-available').textContent = `Available: ${availableCount} / ${totalSlots} spots`;
}

// =================================================================
// 2. BOOKING & PAYMENT LOGIC
// =================================================================

/**
 * Handles the booking request and redirects the user to the payment page URL returned by the backend.
 */
document.getElementById('book-spot-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const slotId = document.getElementById('slotId').value.toUpperCase();
    const vehiclePlate = document.getElementById('vehiclePlate').value;
    const mobileNumber = document.getElementById('mobileNumber').value;
    const durationHours = parseInt(document.getElementById('durationHours').value);
    
    const bookingMessage = document.getElementById('booking-message');
    const submitButton = document.querySelector('#book-spot-form button');

    // Clear previous messages
    bookingMessage.textContent = '';
    bookingMessage.className = 'message';

    if (!slotId || !vehiclePlate || !mobileNumber || isNaN(durationHours)) {
        bookingMessage.classList.add('error');
        bookingMessage.textContent = 'Please select a slot, duration, and fill in all details.';
        return;
    }
    if (!/^\d{10}$/.test(mobileNumber)) {
        bookingMessage.classList.add('error');
        bookingMessage.textContent = 'Please enter a valid 10-digit mobile number.';
        return;
    }

    // START: Disable button and show processing state
    submitButton.disabled = true;
    submitButton.textContent = 'Initiating Payment... 🚀';
    bookingMessage.className = 'message';
    bookingMessage.textContent = 'Requesting payment URL from server...';

    // Booking Request Object to match the Java DTO
    const bookingData = {
        vehiclePlate: vehiclePlate,
        mobileNumber: mobileNumber,
        durationHours: durationHours
    };

    try {
        const response = await fetch(`${BASE_URL}/book/${slotId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bookingData) 
        });
        
        const resultText = await response.text();

        if (response.ok) {
            // SUCCESS: Response body contains the payment URL
            const paymentUrl = resultText;
            
            bookingMessage.classList.add('success');
            bookingMessage.textContent = 'Redirecting to payment gateway...';

            // *** REDIRECT ACTION ***
            window.location.href = paymentUrl;

        } else {
            // Failure State (e.g., Slot booked, Payment Initiation Failed)
            bookingMessage.classList.add('error');
            bookingMessage.textContent = `Reservation Failed: ${resultText}`;
        }
        
    } catch (error) {
        bookingMessage.classList.add('error');
        bookingMessage.textContent = 'Network error: Could not reach the server.';
    } finally {
        // Re-enable button ONLY if no redirect occurred (i.e., on failure)
        // Check if the current URL is still the index.html page
        if (!window.location.href.includes("simulated-payment.html")) {
            submitButton.disabled = false;
            submitButton.textContent = 'Reserve & Proceed to Payment';
            fetchParkingStatus(); 
        }
    }
});


// =================================================================
// 3. INITIALIZATION
// =================================================================

// Initial load of parking status
fetchParkingStatus();

// Update status every 5 seconds (Simulated IoT refresh rate)
setInterval(fetchParkingStatus, 5000);