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

        const reviewPremiumLabel = document.getElementById('reviewPremiumLabel');
        if (result.premiumAmount === null) {
            reviewPremiumLabel.textContent = "Calculated Premium";
            reviewPremium.textContent = "Not Calculated";
        } else {
            reviewPremiumLabel.textContent = "Calculated Premium";
            reviewPremium.textContent = `₹${result.premiumAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        }

        // Set status badge style & dynamic buttons
        reviewActions.innerHTML = ''; // Clear existing action buttons

        if (result.status === 'DRAFT') {
            reviewStatus.className = 'badge badge-user';
            reviewStatus.style.backgroundColor = '#e2e8f0';
            reviewStatus.style.color = '#334155';

            // DRAFT buttons
            const submitBtn = document.createElement('button');
            submitBtn.className = 'btn btn-primary';
            submitBtn.style.flex = '2';
            submitBtn.style.padding = '1rem';
            submitBtn.textContent = 'Submit Application';
            submitBtn.addEventListener('click', async () => {
                const res = await api.submitApplication(appId);
                if (res.success) {
                    loadApplicationDetails();
                } else {
                    alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                }
            });

            const editBtn = document.createElement('button');
            editBtn.className = 'btn btn-secondary';
            editBtn.style.flex = '1';
            editBtn.style.border = '1px solid var(--border)';
            editBtn.style.color = 'var(--text-main)';
            editBtn.textContent = 'Edit Details';
            editBtn.addEventListener('click', () => {
                localStorage.setItem('selectedPlanId', result.planId);
                window.location.href = `travel-details.html?edit=${result.travelDetailsId}&appId=${result.id}`;
            });

            const cancelBtn = document.createElement('button');
            cancelBtn.className = 'btn btn-secondary';
            cancelBtn.style.flex = '1';
            cancelBtn.style.border = '1px solid var(--danger)';
            cancelBtn.style.color = 'var(--danger)';
            cancelBtn.style.backgroundColor = 'transparent';
            cancelBtn.textContent = 'Cancel Application';
            cancelBtn.addEventListener('click', async () => {
                if (confirm('Are you sure you want to cancel this application?')) {
                    const res = await api.cancelApplication(appId);
                    if (res.success) {
                        loadApplicationDetails();
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                    }
                }
            });

            reviewActions.appendChild(submitBtn);
            reviewActions.appendChild(editBtn);
            reviewActions.appendChild(cancelBtn);

        } else if (result.status === 'PENDING_PAYMENT') {
            reviewStatus.className = 'badge badge-user';
            reviewStatus.style.backgroundColor = '#fef3c7';
            reviewStatus.style.color = '#d97706';

            if (result.premiumAmount === null) {
                // Not Calculated -> show [ Calculate Premium ]
                const calcBtn = document.createElement('button');
                calcBtn.className = 'btn btn-primary';
                calcBtn.style.flex = '2';
                calcBtn.style.padding = '1rem';
                calcBtn.textContent = 'Calculate Premium';
                calcBtn.addEventListener('click', async () => {
                    const res = await api.calculatePremium(appId);
                    if (res.success) {
                        // Redirect to quote page
                        window.location.href = `quote.html?applicationId=${appId}`;
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                    }
                });

                const cancelBtn = document.createElement('button');
                cancelBtn.className = 'btn btn-secondary';
                cancelBtn.style.flex = '1';
                cancelBtn.style.border = '1px solid var(--danger)';
                cancelBtn.style.color = 'var(--danger)';
                cancelBtn.style.backgroundColor = 'transparent';
                cancelBtn.textContent = 'Cancel Application';
                cancelBtn.addEventListener('click', async () => {
                    if (confirm('Are you sure you want to cancel this application?')) {
                        const res = await api.cancelApplication(appId);
                        if (res.success) {
                            loadApplicationDetails();
                        } else {
                            alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                        }
                    }
                });

                reviewActions.appendChild(calcBtn);
                reviewActions.appendChild(cancelBtn);

            } else {
                // Calculated -> show [ View Quote ] and [ Proceed to Payment ]
                const viewQuoteBtn = document.createElement('button');
                viewQuoteBtn.className = 'btn btn-primary';
                viewQuoteBtn.style.flex = '1';
                viewQuoteBtn.style.padding = '1rem';
                viewQuoteBtn.textContent = 'View Quote';
                viewQuoteBtn.addEventListener('click', () => {
                    window.location.href = `quote.html?applicationId=${appId}`;
                });

                const proceedPayBtn = document.createElement('button');
                proceedPayBtn.className = 'btn btn-primary';
                proceedPayBtn.style.flex = '2';
                proceedPayBtn.style.padding = '1rem';
                proceedPayBtn.style.backgroundColor = 'var(--success)';
                proceedPayBtn.textContent = 'Proceed to Payment';
                proceedPayBtn.addEventListener('click', () => {
                    window.location.href = `payment.html?applicationId=${appId}`;
                });

                const cancelBtn = document.createElement('button');
                cancelBtn.className = 'btn btn-secondary';
                cancelBtn.style.flex = '1';
                cancelBtn.style.border = '1px solid var(--danger)';
                cancelBtn.style.color = 'var(--danger)';
                cancelBtn.style.backgroundColor = 'transparent';
                cancelBtn.textContent = 'Cancel Application';
                cancelBtn.addEventListener('click', async () => {
                    if (confirm('Are you sure you want to cancel this application?')) {
                        const res = await api.cancelApplication(appId);
                        if (res.success) {
                            loadApplicationDetails();
                        } else {
                            alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                        }
                    }
                });

                reviewActions.appendChild(proceedPayBtn);
                reviewActions.appendChild(viewQuoteBtn);
                reviewActions.appendChild(cancelBtn);
            }

        } else if (result.status === 'CANCELLED') {
            reviewStatus.className = 'badge badge-admin';
            alertContainer.innerHTML = `<div class="alert alert-danger">This policy application has been CANCELLED.</div>`;
        } else {
            // APPROVED or REJECTED
            reviewStatus.className = 'badge badge-user';
            if (result.status === 'APPROVED') {
                reviewStatus.style.backgroundColor = '#d1fae5';
                reviewStatus.style.color = '#059669';
            }
        }

        reviewCard.style.display = 'block';
    }

    // Load data
    await loadApplicationDetails();
});
