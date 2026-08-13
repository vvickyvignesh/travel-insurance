document.addEventListener('DOMContentLoaded', async () => {
    if (typeof auth !== 'undefined' && !auth.getToken()) return;

    const dataStr = sessionStorage.getItem('payment_result_data');
    const alertContainer = document.getElementById('alertContainer');

    if (!dataStr) {
        alertContainer.innerHTML = '<div class="alert alert-danger">Error: No payment transaction details found.</div>';
        return;
    }

    const data = JSON.parse(dataStr);
    let paymentId = data.id; // May be null if failed

    // If ID is not in data, try to retrieve it from the application endpoint
    if (data.success && !paymentId && data.applicationId) {
        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`http://localhost:8080/api/applications/${data.applicationId}/payment`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            const resData = await response.json();
            if (resData.success && resData.data) {
                paymentId = resData.data.id;
            }
        } catch (e) {
            console.error('Failed to pre-fetch payment ID', e);
        }
    }

    if (data.success) {
        document.getElementById('successCard').style.display = 'block';
        document.getElementById('successTxnId').textContent = data.transactionId;
        document.getElementById('successAppNum').textContent = data.applicationNumber;
        document.getElementById('successAmount').textContent = '₹' + data.amount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        document.getElementById('successMethod').textContent = data.paymentMethod;

        const downloadBtn = document.getElementById('btnDownloadReceipt');
        downloadBtn.addEventListener('click', async () => {
            downloadBtn.disabled = true;
            downloadBtn.textContent = 'Generating...';

            try {
                const token = localStorage.getItem('token');
                // Fetch the receipt JSON from backend
                const response = await fetch(`http://localhost:8080/api/payments/${paymentId || 1}/receipt`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                if (!response.ok) {
                    throw new Error('Failed to retrieve receipt data');
                }
                const receiptJson = await response.json();
                
                // Create a text blob representing the receipt beautifully
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
            } catch (e) {
                console.error(e);
                alertContainer.innerHTML = `<div class="alert alert-danger">Error downloading receipt: ${e.message}</div>`;
            } finally {
                downloadBtn.disabled = false;
                downloadBtn.textContent = 'Download Receipt';
            }
        });

    } else {
        document.getElementById('failureCard').style.display = 'block';
        document.getElementById('failureTxnId').textContent = data.transactionId || 'N/A';

        document.getElementById('btnRetryPayment').addEventListener('click', () => {
            window.location.href = `payment.html?applicationId=${data.applicationId}`;
        });
    }

    // Clean up result storage so page refresh doesn't hold data
    sessionStorage.removeItem('payment_result_data');
});
