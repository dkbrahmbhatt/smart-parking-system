const BASE_URL = 'http://localhost:8080/api/parking';
const slotsContainer = document.getElementById('all-slots-container');
const formMessage = document.getElementById('form-message');

// =================================================================
// 1. DATA FETCHING & RENDERING (Metrics, Management, Transactions)
// =================================================================

/**
 * Fetches all necessary data from the backend concurrently.
 */
async function fetchAdminData() {
    try {
        const [statusResponse, reportResponse, transactionResponse] = await Promise.all([
            fetch(`${BASE_URL}/status`),
            fetch(`${BASE_URL}/admin/report`),
            fetch(`${BASE_URL}/admin/transactions`)
        ]);

        // Check for non-200 responses
        if (!statusResponse.ok || !reportResponse.ok || !transactionResponse.ok) {
             throw new Error("Failed to fetch one or more API endpoints.");
        }
        
        const slots = await statusResponse.json();
        const report = await reportResponse.json();
        const transactions = await transactionResponse.json(); 

        renderMetricsDashboard(report, slots);
        renderManagementList(slots);
        renderTransactionHistory(transactions);

    } catch (error) {
        console.error('Error fetching admin data:', error);
        slotsContainer.innerHTML = '<p class="message error">Cannot connect to Java backend. Is the server running?</p>';
        formMessage.className = 'message error';
        formMessage.textContent = 'Failed to load admin data.';
    }
}

/**
 * Updates the System Overview metric cards (Total, Available, Occupied, Revenue).
 */
function renderMetricsDashboard(report, allSlots) {
    // Calculate occupied slots from the 'allSlots' list for accuracy
    const occupiedCount = allSlots.filter(s => s.occupied).length;

    document.querySelector('#total-slots-card .metric-value').textContent = report.totalSlots;
    document.querySelector('#available-slots-card .metric-value').textContent = report.availableSlots;
    document.querySelector('#occupied-slots-card .metric-value').textContent = occupiedCount;
    // Uses the new 'realRevenue' property from the updated backend report endpoint
    document.querySelector('#simulated-revenue-card .metric-value').textContent = `₹${report.realRevenue.toFixed(2)}`;
}

/**
 * Renders the interactive list for Slot Management (CRUD operations).
 */
function renderManagementList(slots) {
    slotsContainer.innerHTML = '';
    
    // Sort slots alphabetically for easier management
    slots.sort((a, b) => a.slotId.localeCompare(b.slotId)); 

    slots.forEach(slot => {
        const row = document.createElement('div');
        row.className = `slot-row ${slot.occupied ? 'occupied' : ''}`;
        
        const details = document.createElement('span');
        details.className = 'slot-row-details';
        details.innerHTML = `
            <strong>ID: ${slot.slotId}</strong> | 
            Price: ₹${slot.currentPrice} | 
            Status: ${slot.occupied ? `Booked (Plate: ${slot.vehiclePlate || 'N/A'})` : 'Available'}
        `;
        row.appendChild(details);
        
        const actions = document.createElement('span');
        actions.className = 'slot-row-actions';
        
        // --- DELETE: Remove button ---
        const removeBtn = document.createElement('button');
        removeBtn.textContent = '❌ Remove';
        removeBtn.className = 'button btn-danger remove-btn';
        removeBtn.disabled = slot.occupied; // Disable if occupied
        removeBtn.title = slot.occupied ? 'Cannot remove occupied slot' : 'Remove this slot';
        removeBtn.addEventListener('click', () => removeSlot(slot.slotId));
        actions.appendChild(removeBtn);
        
        // --- UPDATE: Release/Un-book button ---
        if (slot.occupied) {
            const releaseBtn = document.createElement('button');
            releaseBtn.textContent = '🔑 Release';
            releaseBtn.className = 'button btn-warning release-btn';
            releaseBtn.title = 'Manually release this occupied slot';
            releaseBtn.addEventListener('click', () => releaseSlot(slot.slotId));
            actions.appendChild(releaseBtn);
        }
        
        row.appendChild(actions);
        slotsContainer.appendChild(row);
    });
}

/**
 * Renders the table displaying the historical transaction data.
 */
function renderTransactionHistory(transactions) {
    const tableBody = document.querySelector('#transaction-table tbody');
    tableBody.innerHTML = '';
    
    if (transactions.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6">No transactions recorded yet.</td></tr>';
        return;
    }

    transactions.forEach(t => {
        const row = document.createElement('tr');
        const statusClass = `status-${t.paymentStatus}`;
        
        // Format booking time to local readable string
        const formattedTime = t.bookingTime ? new Date(t.bookingTime).toLocaleString() : 'N/A';
        
        row.innerHTML = `
            <td>${t.transactionId ? t.transactionId.substring(0, 8) + '...' : 'N/A'}</td>
            <td>${t.slotId || 'N/A'}</td>
            <td>${t.vehiclePlate || 'N/A'}</td>
            <td>${formattedTime}</td>
            <td>₹${t.amountPaid ? t.amountPaid.toFixed(2) : '0.00'}</td>
            <td class="${statusClass}">${t.paymentStatus || 'UNKNOWN'}</td>
        `;
        tableBody.appendChild(row);
    });
}


// =================================================================
// 2. CRUD OPERATIONS (CREATE, DELETE, UPDATE)
// =================================================================

/**
 * Handles the creation of a new parking slot via the form.
 */
document.getElementById('slot-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const newSlotId = document.getElementById('newSlotId').value.toUpperCase();
    const newSlotPrice = parseFloat(document.getElementById('newSlotPrice').value);
    
    formMessage.textContent = '';
    formMessage.className = 'message';

    if (!newSlotId || isNaN(newSlotPrice) || newSlotPrice <= 0) {
        formMessage.classList.add('error');
        formMessage.textContent = 'Please enter a valid Slot ID and a positive price.';
        return;
    }

    const newSlotData = {
        slotId: newSlotId,
        currentPrice: newSlotPrice
    };

    try {
        const response = await fetch(`${BASE_URL}/admin/slots`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSlotData)
        });

        if (response.ok) {
            formMessage.classList.add('success');
            formMessage.textContent = `Slot ${newSlotId} successfully added!`;
            document.getElementById('slot-form').reset();
            fetchAdminData(); // Refresh all data
        } else {
            const errorText = await response.text();
            formMessage.classList.add('error');
            formMessage.textContent = `Error adding slot: ${errorText}`;
        }
    } catch (error) {
        formMessage.classList.add('error');
        formMessage.textContent = 'Network error during slot creation.';
    }
});

/**
 * Deletes a parking slot based on its ID.
 */
async function removeSlot(slotId) {
    if (!confirm(`Are you sure you want to permanently remove slot ${slotId}? This action cannot be undone.`)) return;

    try {
        const response = await fetch(`${BASE_URL}/admin/slots/${slotId}`, {
            method: 'DELETE'
        });
        
        const message = await response.text();

        if (response.ok) {
            alert(message);
            fetchAdminData(); // Refresh all data
        } else {
            alert(`Failed to remove slot: ${message}`);
        }
    } catch (error) {
        alert('Network error during slot removal.');
    }
}

/**
 * Manually releases an occupied slot, setting it back to available.
 */
async function releaseSlot(slotId) {
     if (!confirm(`Are you sure you want to manually release slot ${slotId}? This will immediately un-book the spot.`)) return;
    
    try {
        const response = await fetch(`${BASE_URL}/admin/release/${slotId}`, {
            method: 'PUT'
        });
        
        const message = await response.text();
        
        if (response.ok) {
            alert(message);
            fetchAdminData(); // Refresh all data
        } else {
            alert(`Failed to release slot: ${message}`);
        }
    } catch (error) {
        alert('Network error during slot release.');
    }
}


// =================================================================
// 3. INITIALIZATION & REFRESH
// =================================================================

// Initial load of all data
fetchAdminData();

// Refresh data every 10 seconds to keep metrics up-to-date
setInterval(fetchAdminData, 10000);