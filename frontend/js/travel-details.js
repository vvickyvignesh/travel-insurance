document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    const selectedPlanId = localStorage.getItem('selectedPlanId');
    if (!selectedPlanId) {
        alert('Please select an insurance plan first.');
        window.location.href = 'plans.html';
        return;
    }

    const alertContainer = document.getElementById('alertContainer');
    const travelDetailsForm = document.getElementById('travelDetailsForm');

    // Sidebar plan elements
    const sidebarPlanName = document.getElementById('sidebarPlanName');
    const sidebarCoverage = document.getElementById('sidebarCoverage');
    const sidebarMedical = document.getElementById('sidebarMedical');
    const sidebarBaggage = document.getElementById('sidebarBaggage');
    const sidebarEmergency = document.getElementById('sidebarEmergency');
    const sidebarPremium = document.getElementById('sidebarPremium');

    // Fetch and render plan info in the sidebar
    const planResult = await api.getPlan(selectedPlanId);
    if (planResult.success !== false) {
        sidebarPlanName.textContent = planResult.name;
        sidebarCoverage.textContent = `₹${planResult.coverageAmount.toLocaleString('en-IN')}`;
        sidebarMedical.textContent = `₹${planResult.medicalCoverage.toLocaleString('en-IN')}`;
        sidebarBaggage.textContent = `₹${planResult.baggageCoverage.toLocaleString('en-IN')}`;
        sidebarEmergency.textContent = planResult.emergencyAssistance ? '✅ Yes' : '❌ No';
        sidebarPremium.textContent = `₹${planResult.basePremium.toLocaleString('en-IN')}`;
    } else {
        alertContainer.innerHTML = `<div class="alert alert-danger">${planResult.message}</div>`;
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const editId = params.get('edit');
    const appId = params.get('appId');

    // Fetch and populate form if in edit mode
    if (editId) {
        const detailResult = await api.getTravelDetail(editId);
        if (detailResult.success !== false) {
            document.getElementById('travellerName').value = detailResult.travellerName;
            document.getElementById('dateOfBirth').value = detailResult.dateOfBirth;
            document.getElementById('passportNumber').value = detailResult.passportNumber;
            document.getElementById('phone').value = detailResult.phone;
            document.getElementById('destination').value = detailResult.destination;
            document.getElementById('departureDate').value = detailResult.departureDate;
            document.getElementById('returnDate').value = detailResult.returnDate;
            document.getElementById('tripType').value = detailResult.tripType;
            document.getElementById('travelPurpose').value = detailResult.travelPurpose;
        } else {
            alertContainer.innerHTML = `<div class="alert alert-danger">${detailResult.message}</div>`;
        }
    }

    // Submit handler
    travelDetailsForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertContainer.innerHTML = '';

        const travellerName = document.getElementById('travellerName').value.trim();
        const dateOfBirth = document.getElementById('dateOfBirth').value;
        const passportNumber = document.getElementById('passportNumber').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const destination = document.getElementById('destination').value.trim();
        const departureDate = document.getElementById('departureDate').value;
        const returnDate = document.getElementById('returnDate').value;
        const tripType = document.getElementById('tripType').value;
        const travelPurpose = document.getElementById('travelPurpose').value;

        // Frontend validation
        const dobDate = new Date(dateOfBirth);
        const depDate = new Date(departureDate);
        const retDate = new Date(returnDate);

        if (dobDate >= depDate) {
            alertContainer.innerHTML = `<div class="alert alert-danger">Date of Birth must be before the Departure Date.</div>`;
            return;
        }
        if (depDate > retDate) {
            alertContainer.innerHTML = `<div class="alert alert-danger">Departure Date cannot be after the Return Date.</div>`;
            return;
        }

        const payload = {
            travellerName, dateOfBirth, passportNumber, phone, destination, departureDate, returnDate, tripType, travelPurpose
        };

        if (editId) {
            // Update Travel Details
            const updateResult = await api.updateTravelDetails(editId, payload);
            if (updateResult.success !== false) {
                window.location.href = `application-review.html?id=${appId}`;
            } else {
                alertContainer.innerHTML = `<div class="alert alert-danger">${updateResult.message}</div>`;
            }
        } else {
            // 1. Save Travel Details
            const travelResult = await api.createTravelDetails(payload);

            if (travelResult.success === false) {
                alertContainer.innerHTML = `<div class="alert alert-danger">${travelResult.message}</div>`;
                return;
            }

            // 2. Create Application with planId and travelDetailsId
            const appPayload = {
                planId: parseInt(selectedPlanId),
                travelDetailsId: travelResult.id
            };

            const appResult = await api.createApplication(appPayload);

            if (appResult.success) {
                window.location.href = `application-review.html?id=${appResult.data.id}`;
            } else {
                alertContainer.innerHTML = `<div class="alert alert-danger">${appResult.message}</div>`;
            }
        }
    });
});
