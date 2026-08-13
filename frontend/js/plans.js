document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    // Check if user is admin to show panel link
    const currentUser = auth.getCurrentUser();
    if (currentUser && currentUser.role === 'ADMIN') {
        const adminLinkContainer = document.getElementById('adminLinkContainer');
        if (adminLinkContainer) adminLinkContainer.style.display = 'block';
    }

    const plansGrid = document.getElementById('plansGrid');
    const searchPlanInput = document.getElementById('searchPlan');
    const alertContainer = document.getElementById('alertContainer');
    
    // Comparison element bindings
    const comparisonBar = document.getElementById('comparisonBar');
    const comparisonCount = document.getElementById('comparisonCount');
    const triggerCompareBtn = document.getElementById('triggerCompareBtn');
    const clearCompareBtn = document.getElementById('clearCompareBtn');
    const comparisonViewCard = document.getElementById('comparisonViewCard');
    const closeComparisonBtn = document.getElementById('closeComparisonBtn');
    const comparisonHeaders = document.getElementById('comparisonHeaders');
    const comparisonRows = document.getElementById('comparisonRows');

    let allActivePlans = [];
    let comparePlansBucket = [];

    async function loadActivePlans(keyword = '') {
        let result;
        if (keyword) {
            result = await api.searchPlans(keyword);
        } else {
            result = await api.getPlans();
        }

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        allActivePlans = result;
        renderPlans(allActivePlans);
    }

    function renderPlans(plansList) {
        plansGrid.innerHTML = '';

        if (plansList.length === 0) {
            plansGrid.innerHTML = `<p style="text-align: center; color: var(--text-muted); grid-column: 1/-1;">No active insurance plans found.</p>`;
            return;
        }

        plansList.forEach(plan => {
            const isComparing = comparePlansBucket.some(p => p.id === plan.id);
            const compareBtnText = isComparing ? 'Remove Compare' : 'Compare';
            const compareBtnStyle = isComparing ? 'background-color: var(--warning); color: #ffffff;' : 'border: 1px solid var(--border); color: var(--text-main);';

            const card = document.createElement('div');
            card.className = 'card';
            card.style.display = 'flex';
            card.style.flexDirection = 'column';
            card.style.justifyContent = 'space-between';

            card.innerHTML = `
                <div>
                    <h2 style="color: var(--primary); margin-bottom: 0.5rem;">${plan.name}</h2>
                    <p style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1.5rem; min-height: 50px;">${plan.description}</p>
                    
                    <div style="border-top: 1px solid var(--border); padding-top: 1rem; margin-bottom: 1.5rem; font-size: 0.95rem; display: flex; flex-direction: column; gap: 0.5rem;">
                        <div style="display: flex; justify-content: space-between;"><strong>Coverage Limit:</strong> <span>₹${plan.coverageAmount.toLocaleString('en-IN')}</span></div>
                        <div style="display: flex; justify-content: space-between;"><strong>Medical Cover:</strong> <span>₹${plan.medicalCoverage.toLocaleString('en-IN')}</span></div>
                        <div style="display: flex; justify-content: space-between;"><strong>Baggage Cover:</strong> <span>₹${plan.baggageCoverage.toLocaleString('en-IN')}</span></div>
                        <div style="display: flex; justify-content: space-between;"><strong>Trip Cancellation:</strong> <span>₹${plan.tripCancellation.toLocaleString('en-IN')}</span></div>
                        <div style="display: flex; justify-content: space-between;"><strong>Emergency Assist:</strong> <span>${plan.emergencyAssistance ? '✅ Yes' : '❌ No'}</span></div>
                    </div>
                </div>

                <div>
                    <div style="text-align: center; margin-bottom: 1rem;">
                        <span style="font-size: 0.85rem; color: var(--text-muted); text-transform: uppercase; font-weight: bold;">Base Premium</span>
                        <h3 style="font-size: 2rem; color: var(--primary); font-weight: 700;">₹${plan.basePremium.toLocaleString('en-IN')}</h3>
                    </div>

                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <button class="btn btn-secondary btn-block btn-select-plan" data-id="${plan.id}" data-name="${plan.name}" style="background-color: var(--accent); color: #ffffff;">Select Plan</button>
                        <div style="display: flex; gap: 0.5rem;">
                            <a href="plan-details.html?id=${plan.id}" class="btn btn-secondary" style="flex: 1; border: 1px solid var(--border); color: var(--text-main); font-size: 0.9rem; padding: 0.5rem;">Details</a>
                            <button class="btn btn-secondary btn-compare" data-id="${plan.id}" style="flex: 1; font-size: 0.9rem; padding: 0.5rem; ${compareBtnStyle}">${compareBtnText}</button>
                        </div>
                    </div>
                </div>
            `;
            plansGrid.appendChild(card);
        });

        // Setup Select Button Handlers
        document.querySelectorAll('.btn-select-plan').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                const name = e.target.getAttribute('data-name');
                localStorage.setItem('selectedPlanId', id);
                alertContainer.innerHTML = `<div class="alert alert-success">"${name}" selected successfully.</div>`;
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        });

        // Setup Compare Button Handlers
        document.querySelectorAll('.btn-compare').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = parseInt(e.target.getAttribute('data-id'));
                togglePlanComparison(id);
            });
        });
    }

    function togglePlanComparison(planId) {
        const plan = allActivePlans.find(p => p.id === planId);
        const index = comparePlansBucket.findIndex(p => p.id === planId);

        if (index > -1) {
            comparePlansBucket.splice(index, 1);
        } else {
            if (comparePlansBucket.length >= 3) {
                alert('You can compare up to 3 plans only.');
                return;
            }
            comparePlansBucket.push(plan);
        }

        updateComparisonBar();
        renderPlans(allActivePlans); // Refresh compare labels
    }

    function updateComparisonBar() {
        const count = comparePlansBucket.length;
        if (count > 0) {
            comparisonBar.style.display = 'block';
            comparisonCount.textContent = `${count} of 3 plans selected for comparison`;
        } else {
            comparisonBar.style.display = 'none';
            comparisonViewCard.style.display = 'none';
        }
    }

    // Compare Trigger Handler
    triggerCompareBtn.addEventListener('click', () => {
        if (comparePlansBucket.length === 0) return;

        // Clear header options except the base "Feature"
        comparisonHeaders.innerHTML = '<th>Feature</th>';
        comparePlansBucket.forEach(plan => {
            const th = document.createElement('th');
            th.textContent = plan.name;
            comparisonHeaders.appendChild(th);
        });

        // Dynamic rows mappings
        const features = [
            { label: 'Total Coverage', key: 'coverageAmount', format: val => `₹${val.toLocaleString('en-IN')}` },
            { label: 'Medical Coverage', key: 'medicalCoverage', format: val => `₹${val.toLocaleString('en-IN')}` },
            { label: 'Baggage Coverage', key: 'baggageCoverage', format: val => `₹${val.toLocaleString('en-IN')}` },
            { label: 'Trip Cancellation', key: 'tripCancellation', format: val => `₹${val.toLocaleString('en-IN')}` },
            { label: 'Emergency Assistance', key: 'emergencyAssistance', format: val => val ? '✅ Available' : '❌ Not Available' },
            { label: 'Base Premium Cost', key: 'basePremium', format: val => `₹${val.toLocaleString('en-IN')}` },
        ];

        comparisonRows.innerHTML = '';
        features.forEach(feat => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td style="font-weight: bold; text-align: left;">${feat.label}</td>`;
            
            comparePlansBucket.forEach(plan => {
                const td = document.createElement('td');
                td.textContent = feat.format(plan[feat.key]);
                tr.appendChild(td);
            });
            comparisonRows.appendChild(tr);
        });

        // Add final select action row inside comparison table
        const actionTr = document.createElement('tr');
        actionTr.innerHTML = `<td style="font-weight: bold; text-align: left;">Select Choice</td>`;
        comparePlansBucket.forEach(plan => {
            const td = document.createElement('td');
            td.innerHTML = `<button class="btn btn-primary btn-select-plan" data-id="${plan.id}" data-name="${plan.name}" style="padding: 0.4rem 1rem; font-size: 0.9rem;">Select Plan</button>`;
            actionTr.appendChild(td);
        });
        comparisonRows.appendChild(actionTr);

        // Bind new select plan actions loaded in comparison row elements
        actionTr.querySelectorAll('.btn-select-plan').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                const name = e.target.getAttribute('data-name');
                localStorage.setItem('selectedPlanId', id);
                alertContainer.innerHTML = `<div class="alert alert-success">"${name}" selected successfully.</div>`;
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        });

        comparisonViewCard.style.display = 'block';
        comparisonViewCard.scrollIntoView({ behavior: 'smooth' });
    });

    // Clear Comparison Handler
    clearCompareBtn.addEventListener('click', () => {
        comparePlansBucket = [];
        updateComparisonBar();
        renderPlans(allActivePlans);
    });

    // Close Comparison Card
    closeComparisonBtn.addEventListener('click', () => {
        comparisonViewCard.style.display = 'none';
    });

    // Search query listener with key up delay
    let searchTimeout;
    searchPlanInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();
        searchTimeout = setTimeout(() => {
            loadActivePlans(query);
        }, 300);
    });

    // Initial load
    await loadActivePlans();
});
