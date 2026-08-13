document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    // Admin link visibility check
    const currentUser = auth.getCurrentUser();
    if (currentUser && currentUser.role === 'ADMIN') {
        const adminLinkContainer = document.getElementById('adminLinkContainer');
        if (adminLinkContainer) adminLinkContainer.style.display = 'block';
    }

    const policiesTableBody = document.getElementById('policiesTableBody');
    const alertContainer = document.getElementById('alertContainer');

    async function loadPolicies() {
        alertContainer.innerHTML = '';
        const result = await api.getPolicies();

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        renderPoliciesTable(result);
    }

    function renderPoliciesTable(policyList) {
        policiesTableBody.innerHTML = '';

        if (policyList.length === 0) {
            policiesTableBody.innerHTML = `
                <tr>
                    <td colspan="9" style="text-align: center; color: var(--text-muted);">You have no active insurance policies. Complete an application and payment to receive policy coverage.</td>
                </tr>
            `;
            return;
        }

        policyList.forEach(policy => {
            const row = document.createElement('tr');
            row.id = `policy-row-${policy.id}`;

            // Status Badge Formatting
            let badgeStyle = 'background-color: #d1fae5; color: #065f46;';
            if (policy.status !== 'ACTIVE') {
                badgeStyle = 'background-color: #e2e8f0; color: #64748b;';
            }

            // Coverage Formatting
            const coverageDisplay = `₹${policy.coverageAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
            const premiumDisplay = `₹${policy.premiumAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

            row.innerHTML = `
                <td><strong>${policy.policyNumber}</strong></td>
                <td>${policy.applicationNumber}</td>
                <td>${policy.planName}</td>
                <td>${policy.destination}</td>
                <td><strong>${coverageDisplay}</strong></td>
                <td><strong>${premiumDisplay}</strong></td>
                <td>${policy.startDate} to ${policy.endDate}</td>
                <td><span class="badge" style="${badgeStyle}">${policy.status}</span></td>
                <td>
                    <button class="btn btn-primary btn-download-cert" data-id="${policy.id}" data-num="${policy.policyNumber}" style="padding: 0.35rem 0.7rem; font-size: 0.85rem; background-color: var(--primary);">
                        Download Certificate
                    </button>
                </td>
            `;
            policiesTableBody.appendChild(row);
        });

        // Event hooks for downloading certificates
        document.querySelectorAll('.btn-download-cert').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const policyNum = e.target.getAttribute('data-num');
                const originalText = e.target.textContent;
                
                e.target.disabled = true;
                e.target.textContent = 'Downloading...';
                
                const result = await api.downloadPolicyDocument(id, `Policy_Certificate_${policyNum}.txt`);
                
                e.target.disabled = false;
                e.target.textContent = originalText;

                if (result.success) {
                    alertContainer.innerHTML = `<div class="alert alert-success">Policy certificate generated and downloaded successfully!</div>`;
                } else {
                    alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
                }
            });
        });
    }

    // Load active policies
    await loadPolicies();
});
