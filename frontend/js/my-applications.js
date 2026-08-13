document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    // Admin link visibility check
    const currentUser = auth.getCurrentUser();
    if (currentUser && currentUser.role === 'ADMIN') {
        const adminLinkContainer = document.getElementById('adminLinkContainer');
        if (adminLinkContainer) adminLinkContainer.style.display = 'block';
    }

    const applicationsTableBody = document.getElementById('applicationsTableBody');
    const alertContainer = document.getElementById('alertContainer');

    async function loadApplications() {
        alertContainer.innerHTML = '';
        const result = await api.getApplications();

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        renderApplicationsTable(result);
    }

    function renderApplicationsTable(appList) {
        applicationsTableBody.innerHTML = '';

        if (appList.length === 0) {
            applicationsTableBody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; color: var(--text-muted);">You have no policy applications.</td>
                </tr>
            `;
            return;
        }

        appList.forEach(app => {
            const row = document.createElement('tr');
            row.id = `app-row-${app.id}`;
            
            // Format Created Date
            let createdDate = '—';
            if (app.createdAt) {
                const date = new Date(app.createdAt);
                createdDate = date.toLocaleDateString('en-IN') + ' ' + date.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
            }

            // Status Badge Formatting
            let badgeClass = 'badge';
            if (app.status === 'DRAFT') {
                badgeClass = 'badge badge-user';
                row.style.backgroundColor = 'transparent';
            } else if (app.status === 'PENDING_PAYMENT') {
                badgeClass = 'badge';
                row.style.backgroundColor = '#fffbeb'; // soft yellow highlight
            } else if (app.status === 'CANCELLED') {
                badgeClass = 'badge badge-admin';
                row.style.backgroundColor = '#f8fafc'; // greyed out
            }

            // Premium amount formatting
            let premiumDisplay = '<span style="color: var(--text-muted); font-style: italic;">Not Calculated</span>';
            if (app.premiumAmount !== null) {
                premiumDisplay = `<strong>₹${app.premiumAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</strong>`;
            }

            // Actions logic
            let actionHtml = '—';
            if (app.status === 'DRAFT') {
                actionHtml = `<a href="application-review.html?id=${app.id}" class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.85rem;">Review</a>`;
            } else if (app.status === 'PENDING_PAYMENT') {
                if (app.premiumAmount === null) {
                    actionHtml = `
                        <div style="display: flex; gap: 0.25rem;">
                            <a href="application-review.html?id=${app.id}" class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.85rem; background-color: var(--accent);">Calculate</a>
                            <button class="btn btn-secondary btn-cancel-app" data-id="${app.id}" style="padding: 0.3rem 0.6rem; font-size: 0.85rem; border: 1px solid var(--danger); color: var(--danger); background-color: transparent;">Cancel</button>
                        </div>
                    `;
                } else {
                    actionHtml = `
                        <div style="display: flex; gap: 0.25rem;">
                            <a href="payment.html?applicationId=${app.id}" class="btn btn-primary" style="padding: 0.3rem 0.6rem; font-size: 0.85rem; background-color: var(--success);">Pay</a>
                            <button class="btn btn-secondary btn-cancel-app" data-id="${app.id}" style="padding: 0.3rem 0.6rem; font-size: 0.85rem; border: 1px solid var(--danger); color: var(--danger); background-color: transparent;">Cancel</button>
                        </div>
                    `;
                }
            }

            row.innerHTML = `
                <td><strong>${app.applicationNumber}</strong></td>
                <td>${app.planName}</td>
                <td>${app.destination}</td>
                <td>${app.departureDate}</td>
                <td>${app.returnDate}</td>
                <td>${premiumDisplay}</td>
                <td><span class="${badgeClass}" style="${app.status === 'PENDING_PAYMENT' ? 'background-color: #fef3c7; color: #d97706;' : ''}">${app.status}</span></td>
                <td style="font-size: 0.9rem;">${createdDate}</td>
                <td>${actionHtml}</td>
            `;
            applicationsTableBody.appendChild(row);
        });

        // Cancel buttons hook
        document.querySelectorAll('.btn-cancel-app').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                if (confirm('Are you sure you want to cancel this application?')) {
                    const result = await api.cancelApplication(id);
                    if (result.success) {
                        alertContainer.innerHTML = `<div class="alert alert-success">Application cancelled successfully.</div>`;
                        loadApplications();
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
                    }
                }
            });
        });
    }

    // Load applications list
    await loadApplications();
});
