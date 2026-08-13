document.addEventListener('DOMContentLoaded', async () => {
    if (typeof auth !== 'undefined' && !auth.getToken()) return;

    // Direct redirection if the user is not ADMIN
    const currentUser = auth.getCurrentUser();
    if (!currentUser || currentUser.role !== 'ADMIN') {
        window.location.href = 'dashboard.html';
        return;
    }

    const tableBody = document.getElementById('adminPaymentsTableBody');
    const alertContainer = document.getElementById('alertContainer');
    const searchInput = document.getElementById('searchPayment');
    const statusSelect = document.getElementById('filterStatus');

    let allPayments = [];

    async function loadPayments() {
        alertContainer.innerHTML = '';
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('http://localhost:8080/api/admin/payments', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const result = await response.json();

            if (result.success === false) {
                alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
                return;
            }

            allPayments = result.data || [];
            applyFiltersAndSearch();
        } catch (e) {
            console.error(e);
            alertContainer.innerHTML = '<div class="alert alert-danger">Failed to connect to backend server.</div>';
        }
    }

    function applyFiltersAndSearch() {
        const query = searchInput.value.trim().toLowerCase();
        const statusFilter = statusSelect.value;

        const filtered = allPayments.filter(payment => {
            const matchesSearch = 
                payment.transactionId.toLowerCase().includes(query) ||
                payment.applicationNumber.toLowerCase().includes(query);

            const matchesStatus = statusFilter === 'ALL' || payment.status === statusFilter;

            return matchesSearch && matchesStatus;
        });

        renderPaymentsTable(filtered);
    }

    function renderPaymentsTable(paymentList) {
        tableBody.innerHTML = '';

        if (!paymentList || paymentList.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; color: var(--text-muted);">No payment transactions found matching the criteria.</td>
                </tr>
            `;
            return;
        }

        paymentList.forEach(payment => {
            const row = document.createElement('tr');
            row.id = `payment-row-${payment.id}`;

            let badgeStyle = 'background-color: #d1fae5; color: #065f46;';
            if (payment.status === 'FAILED') {
                badgeStyle = 'background-color: #fee2e2; color: #991b1b;';
            } else if (payment.status === 'PENDING') {
                badgeStyle = 'background-color: #fef3c7; color: #d97706;';
            }

            const amountDisplay = `₹${payment.amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
            const payDate = payment.paymentDate ? new Date(payment.paymentDate).toLocaleString() : 'N/A';

            row.innerHTML = `
                <td><strong>${payment.transactionId}</strong></td>
                <td>${payment.applicationNumber}</td>
                <td><strong>${amountDisplay}</strong></td>
                <td>${payment.paymentMethod}</td>
                <td><span class="badge" style="${badgeStyle}">${payment.status}</span></td>
                <td>${payDate}</td>
            `;
            tableBody.appendChild(row);
        });
    }

    searchInput.addEventListener('input', applyFiltersAndSearch);
    statusSelect.addEventListener('change', applyFiltersAndSearch);

    await loadPayments();
});
