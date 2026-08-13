document.addEventListener('DOMContentLoaded', async () => {
    if (typeof auth !== 'undefined' && !auth.getToken()) return;

    const paymentsTableBody = document.getElementById('paymentsTableBody');
    const alertContainer = document.getElementById('alertContainer');

    async function loadPayments() {
        alertContainer.innerHTML = '';
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('http://localhost:8080/api/payments', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const result = await response.json();

            if (result.success === false) {
                alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
                return;
            }

            renderPaymentsTable(result.data);
        } catch (e) {
            console.error(e);
            alertContainer.innerHTML = '<div class="alert alert-danger">Failed to fetch payment details. Connection error.</div>';
        }
    }

    function renderPaymentsTable(paymentList) {
        paymentsTableBody.innerHTML = '';

        if (!paymentList || paymentList.length === 0) {
            paymentsTableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; color: var(--text-muted);">You have no recorded payment transactions.</td>
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

            let actionHtml = '';
            if (payment.status === 'SUCCESS') {
                actionHtml = `
                    <button class="btn btn-primary btn-download-receipt" data-id="${payment.id}" style="padding: 0.35rem 0.7rem; font-size: 0.85rem; background-color: var(--primary);">
                        Download Receipt
                    </button>
                `;
            } else {
                actionHtml = `<span style="color: var(--text-muted); font-size: 0.85rem; font-style: italic;">No Receipt</span>`;
            }

            row.innerHTML = `
                <td><strong>${payment.transactionId}</strong></td>
                <td>${payment.applicationNumber}</td>
                <td><strong>${amountDisplay}</strong></td>
                <td>${payment.paymentMethod}</td>
                <td><span class="badge" style="${badgeStyle}">${payment.status}</span></td>
                <td>${payDate}</td>
                <td>${actionHtml}</td>
            `;
            paymentsTableBody.appendChild(row);
        });

        // Event hooks for downloading receipt
        document.querySelectorAll('.btn-download-receipt').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const originalText = e.target.textContent;
                
                e.target.disabled = true;
                e.target.textContent = 'Downloading...';
                
                try {
                    const token = localStorage.getItem('token');
                    const response = await fetch(`http://localhost:8080/api/payments/${id}/receipt`, {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });
                    if (!response.ok) {
                        throw new Error('Failed to retrieve receipt data');
                    }
                    const receiptJson = await response.json();
                    
                    const receiptText = `
========================================
       TRAVEL SHIELD - PAYMENT RECEIPT  
========================================
Transaction ID  : ${receiptJson.transactionId}
Application No  : ${receiptJson.applicationNumber}
Customer Name   : ${receiptJson.customerName}
Plan Name       : ${receiptJson.planName}
Amount Paid     : ${receiptJson.amount} ${receiptJson.currency}
Payment Method  : ${receiptJson.paymentMethod}
Status          : ${receiptJson.status}
Payment Date    : ${receiptJson.paymentDate}
========================================
   Thank you for your purchase!
========================================
`;
                    const blob = new Blob([receiptText], { type: 'text/plain' });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `Receipt_${receiptJson.transactionId}.txt`;
                    document.body.appendChild(a);
                    a.click();
                    document.body.removeChild(a);
                    URL.revokeObjectURL(url);
                    alertContainer.innerHTML = '<div class="alert alert-success">Receipt downloaded successfully!</div>';
                } catch (err) {
                    console.error(err);
                    alertContainer.innerHTML = `<div class="alert alert-danger">Error: ${err.message}</div>`;
                } finally {
                    e.target.disabled = false;
                    e.target.textContent = originalText;
                }
            });
        });
    }

    await loadPayments();
});
