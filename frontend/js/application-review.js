document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    const alertContainer = document.getElementById('alertContainer');
    const reviewCard = document.getElementById('reviewCard');
    const reviewAppNumber = document.getElementById('reviewAppNumber');
    const reviewStatus = document.getElementById('reviewStatus');
    const reviewPlanName = document.getElementById('reviewPlanName');
    const reviewTravellerName = document.getElementById('reviewTravellerName');
    const reviewDestination = document.getElementById('reviewDestination');
    const reviewDates = document.getElementById('reviewDates');
    const reviewPremium = document.getElementById('reviewPremium');

    const submitAppBtn = document.getElementById('submitAppBtn');
    const editTravelDetailsBtn = document.getElementById('editTravelDetailsBtn');
    const cancelAppBtn = document.getElementById('cancelAppBtn');
    const reviewActions = document.getElementById('reviewActions');

    // Parse URL parameter: id=1
    const params = new URLSearchParams(window.location.search);
    const appId = params.get('id');

    if (!appId) {
        alertContainer.innerHTML = `<div class="alert alert-danger">Invalid Application: missing ID param</div>`;
        return;
    }

    let applicationData = null;

    async function loadApplicationDetails() {
        alertContainer.innerHTML = '';
        const result = await api.getApplication(appId);

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        applicationData = result;

        // Render values
        reviewAppNumber.textContent = result.applicationNumber;
        reviewStatus.textContent = result.status;
        reviewPlanName.textContent = result.planName;
        reviewTravellerName.textContent = result.travellerName;
        reviewDestination.textContent = result.destination;
        reviewDates.textContent = `${result.departureDate} to ${result.returnDate}`;
        reviewPremium.textContent = `₹${result.premiumAmount.toLocaleString('en-IN')}`;

        // Set status badge style
        if (result.status === 'DRAFT') {
            reviewStatus.className = 'badge badge-user';
            reviewStatus.style.backgroundColor = '#e2e8f0';
            reviewStatus.style.color = '#334155';
            reviewActions.style.display = 'flex'; // show options
        } else if (result.status === 'PENDING_PAYMENT') {
            reviewStatus.className = 'badge badge-user';
            reviewStatus.style.backgroundColor = '#fef3c7';
            reviewStatus.style.color = '#d97706';
            reviewActions.style.display = 'none'; // hide options
            alertContainer.innerHTML = `
                <div class="alert alert-success">
                    <h3>Application Submitted Successfully!</h3>
                    <p style="margin-top: 0.5rem;"><strong>Application Number:</strong> ${result.applicationNumber}</p>
                    <p style="margin-top: 0.25rem;">Your application is ready for premium calculation and payment.</p>
                    <a href="payment.html" class="btn btn-primary" style="margin-top: 1rem; display: inline-block;">Proceed to Payment</a>
                </div>
            `;
        } else if (result.status === 'CANCELLED') {
            reviewStatus.className = 'badge badge-admin';
            reviewActions.style.display = 'none'; // hide options
            alertContainer.innerHTML = `<div class="alert alert-danger">This policy application has been CANCELLED.</div>`;
        }

        reviewCard.style.display = 'block';
    }

    // Edit handler
    editTravelDetailsBtn.addEventListener('click', () => {
        if (!applicationData) return;
        // Redirect to travel details page with edit param and store selectedPlanId so form works
        localStorage.setItem('selectedPlanId', applicationData.planId);
        window.location.href = `travel-details.html?edit=${applicationData.travelDetailsId}&appId=${applicationData.id}`;
    });

    // Submit handler
    submitAppBtn.addEventListener('click', async () => {
        if (!applicationData) return;
        alertContainer.innerHTML = '';
        const result = await api.submitApplication(appId);

        if (result.success) {
            // Reload and show success prompt
            loadApplicationDetails();
        } else {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
        }
    });

    // Cancel handler
    cancelAppBtn.addEventListener('click', async () => {
        if (!applicationData) return;
        if (confirm('Are you sure you want to cancel this application?')) {
            alertContainer.innerHTML = '';
            const result = await api.cancelApplication(appId);

            if (result.success) {
                loadApplicationDetails();
            } else {
                alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            }
        }
    });

    // Load data
    await loadApplicationDetails();
});
