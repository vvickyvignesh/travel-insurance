document.addEventListener('DOMContentLoaded', async () => {
    // Authenticate checks
    const token = auth.getToken();
    const currentUser = auth.getCurrentUser();
    if (!token || !currentUser || currentUser.role !== 'ADMIN') {
        window.location.href = 'dashboard.html';
        return;
    }

    const adminAppTableBody = document.getElementById('adminAppTableBody');
    const alertContainer = document.getElementById('alertContainer');
    
    // Details modal elements
    const detailsModalCard = document.getElementById('detailsModalCard');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const modalAppNumber = document.getElementById('modalAppNumber');
    const modalDetailsGrid = document.getElementById('modalDetailsGrid');

    let allApplications = [];

    async function loadApplications() {
        alertContainer.innerHTML = '';
        const result = await api.getAdminApplications();

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        allApplications = result;
        renderApplicationsTable(allApplications);
    }

    function renderApplicationsTable(appList) {
        adminAppTableBody.innerHTML = '';

        if (appList.length === 0) {
            adminAppTableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; color: var(--text-muted);">No policy applications found.</td>
                </tr>
            `;
            return;
        }

        appList.forEach(app => {
            const row = document.createElement('tr');
            row.id = `admin-app-row-${app.id}`;

            // Status style
            let badgeClass = 'badge badge-user';
            let badgeStyle = '';
            if (app.status === 'PENDING_PAYMENT') {
                badgeStyle = 'background-color: #fef3c7; color: #d97706;';
            } else if (app.status === 'CANCELLED') {
                badgeClass = 'badge badge-admin';
                badgeStyle = 'background-color: #e2e8f0; color: #64748b;';
            } else if (app.status === 'APPROVED') {
                badgeStyle = 'background-color: #d1fae5; color: #059669;';
            } else if (app.status === 'REJECTED') {
                badgeClass = 'badge badge-admin';
            }

            // Actions logic
            let actionHtml = '';
            if (app.status === 'PENDING_PAYMENT') {
                actionHtml = `
                    <div style="display: flex; gap: 0.25rem;">
                        <button class="btn btn-secondary btn-approve" data-id="${app.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; background-color: var(--success); color: #ffffff;">Approve</button>
                        <button class="btn btn-secondary btn-reject" data-id="${app.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; background-color: var(--danger); color: #ffffff;">Reject</button>
                        <button class="btn btn-secondary btn-view" data-id="${app.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; border: 1px solid var(--border); color: var(--text-main);">View</button>
                    </div>
                `;
            } else {
                actionHtml = `<button class="btn btn-secondary btn-view" data-id="${app.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; border: 1px solid var(--border); color: var(--text-main);">View Details</button>`;
            }

            row.innerHTML = `
                <td><strong>${app.applicationNumber}</strong></td>
                <td>
                    <div style="font-weight: 600;">${app.userName}</div>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${app.userEmail}</div>
                </td>
                <td>${app.planName}</td>
                <td>${app.destination}</td>
                <td>${app.departureDate} to ${app.returnDate}</td>
                <td style="font-style: italic; color: var(--text-muted);">Not Calculated</td>
                <td><span class="${badgeClass}" style="${badgeStyle}">${app.status}</span></td>
                <td>${actionHtml}</td>
            `;
            adminAppTableBody.appendChild(row);
        });

        // Event hooks
        document.querySelectorAll('.btn-view').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                showApplicationDetails(id);
            });
        });

        document.querySelectorAll('.btn-approve').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                if (confirm('Are you sure you want to approve this application?')) {
                    const res = await api.approveAdminApplication(id);
                    if (res.success !== false) {
                        alertContainer.innerHTML = `<div class="alert alert-success">Application approved.</div>`;
                        loadApplications();
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                    }
                }
            });
        });

        document.querySelectorAll('.btn-reject').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                if (confirm('Are you sure you want to reject this application?')) {
                    const res = await api.rejectAdminApplication(id);
                    if (res.success !== false) {
                        alertContainer.innerHTML = `<div class="alert alert-danger" style="background-color: #fee2e2; color: #991b1b;">Application rejected.</div>`;
                        loadApplications();
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                    }
                }
            });
        });
    }

    async function showApplicationDetails(id) {
        alertContainer.innerHTML = '';
        const app = allApplications.find(a => a.id === parseInt(id));
        if (!app) return;

        // Fetch traveler specific detail info using TravelDetail API
        const traveler = await api.getTravelDetail(app.travelDetailsId);

        modalAppNumber.textContent = `Details: ${app.applicationNumber}`;
        modalDetailsGrid.innerHTML = `
            <div><strong>Plan Name:</strong> ${app.planName}</div>
            <div><strong>Premium Estimated:</strong> ₹${app.premiumAmount.toLocaleString('en-IN')}</div>
            <div><strong>Applicant User:</strong> ${app.userName} (${app.userEmail})</div>
            <div><strong>Status State:</strong> <span class="badge">${app.status}</span></div>
            <div style="grid-column: span 2; border-top: 1px solid var(--border); margin-top: 0.5rem; padding-top: 0.5rem; font-weight: bold; color: var(--primary);">Traveller Parameters</div>
            <div><strong>Traveller Name:</strong> ${app.travellerName}</div>
            <div><strong>Date of Birth:</strong> ${traveler.dateOfBirth}</div>
            <div><strong>Passport Number:</strong> ${traveler.passportNumber}</div>
            <div><strong>Contact Phone:</strong> ${traveler.phone || '—'}</div>
            <div><strong>Trip Destination:</strong> ${app.destination}</div>
            <div><strong>Trip Dates:</strong> ${app.departureDate} to ${app.returnDate}</div>
            <div><strong>Trip Mode:</strong> ${traveler.tripType}</div>
            <div><strong>Trip Purpose:</strong> ${traveler.travelPurpose}</div>
        `;

        detailsModalCard.style.display = 'block';
        detailsModalCard.scrollIntoView({ behavior: 'smooth' });
    }

    closeModalBtn.addEventListener('click', () => {
        detailsModalCard.style.display = 'none';
    });

    // Initial load
    await loadApplications();
});
