document.addEventListener('DOMContentLoaded', async () => {
    // Authenticate check
    const token = auth.getToken();
    const currentUser = auth.getCurrentUser();
    if (!token || !currentUser || currentUser.role !== 'ADMIN') {
        window.location.href = 'dashboard.html';
        return;
    }

    const adminPlansTableBody = document.getElementById('adminPlansTableBody');
    const alertContainer = document.getElementById('alertContainer');
    
    // Search / Filter inputs
    const searchAdminPlan = document.getElementById('searchAdminPlan');
    const filterStatus = document.getElementById('filterStatus');

    // Form element bindings
    const toggleFormBtn = document.getElementById('toggleFormBtn');
    const planFormCard = document.getElementById('planFormCard');
    const planForm = document.getElementById('planForm');
    const formTitle = document.getElementById('formTitle');
    const editPlanId = document.getElementById('editPlanId');
    const nameInput = document.getElementById('name');
    const descriptionInput = document.getElementById('description');
    const coverageInput = document.getElementById('coverageAmount');
    const medicalInput = document.getElementById('medicalCoverage');
    const baggageInput = document.getElementById('baggageCoverage');
    const cancellationInput = document.getElementById('tripCancellation');
    const premiumInput = document.getElementById('basePremium');
    const emergencyInput = document.getElementById('emergencyAssistance');
    const activeInput = document.getElementById('active');
    const cancelPlanFormBtn = document.getElementById('cancelPlanFormBtn');

    let allPlans = [];

    async function loadPlans() {
        alertContainer.innerHTML = '';
        const result = await api.getAdminPlans();

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        allPlans = result;
        applyFiltersAndRender();
    }

    function applyFiltersAndRender() {
        const searchQuery = searchAdminPlan.value.toLowerCase().trim();
        const statusQuery = filterStatus.value;

        let filtered = allPlans;

        // Search Filter
        if (searchQuery) {
            filtered = filtered.filter(p => p.name.toLowerCase().includes(searchQuery));
        }

        // Status Filter
        if (statusQuery === 'ACTIVE') {
            filtered = filtered.filter(p => p.active === true);
        } else if (statusQuery === 'INACTIVE') {
            filtered = filtered.filter(p => p.active === false);
        }

        renderPlansTable(filtered);
    }

    function renderPlansTable(plansList) {
        adminPlansTableBody.innerHTML = '';

        if (plansList.length === 0) {
            adminPlansTableBody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; color: var(--text-muted);">No plans found</td>
                </tr>
            `;
            return;
        }

        plansList.forEach(plan => {
            const row = document.createElement('tr');
            row.id = `plan-row-${plan.id}`;
            const statusBadgeClass = plan.active ? 'badge badge-user' : 'badge badge-admin';
            const statusText = plan.active ? 'Active' : 'Inactive';

            // Toggle active/deactive label
            const toggleActiveBtn = plan.active
                ? `<button class="btn btn-secondary btn-deactivate" data-id="${plan.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; background-color: var(--warning); color: #ffffff;">Deactivate</button>`
                : `<button class="btn btn-secondary btn-activate" data-id="${plan.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; background-color: var(--success); color: #ffffff;">Activate</button>`;

            row.innerHTML = `
                <td>${plan.id}</td>
                <td style="font-weight: 600; color: var(--primary-light);">${plan.name}</td>
                <td>₹${plan.coverageAmount.toLocaleString('en-IN')}</td>
                <td>₹${plan.medicalCoverage.toLocaleString('en-IN')}</td>
                <td>₹${plan.baggageCoverage.toLocaleString('en-IN')}</td>
                <td style="font-weight: bold;">₹${plan.basePremium.toLocaleString('en-IN')}</td>
                <td><span class="${statusBadgeClass}">${statusText}</span></td>
                <td>
                    <div style="display: flex; gap: 0.25rem;">
                        <button class="btn btn-secondary btn-edit" data-id="${plan.id}" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; border: 1px solid var(--border); color: var(--text-main);">Edit</button>
                        ${toggleActiveBtn}
                        <button class="btn btn-secondary btn-delete-plan" data-id="${plan.id}" data-name="${plan.name}" style="background-color: var(--danger); color: #ffffff; padding: 0.25rem 0.5rem; font-size: 0.85rem;">Delete</button>
                    </div>
                </td>
            `;
            adminPlansTableBody.appendChild(row);
        });

        // Setup actions event listeners
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                editPlan(id);
            });
        });

        document.querySelectorAll('.btn-activate').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const res = await api.activateAdminPlan(id);
                if (res.success !== false) {
                    loadPlans();
                } else {
                    alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                }
            });
        });

        document.querySelectorAll('.btn-deactivate').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const res = await api.deactivateAdminPlan(id);
                if (res.success !== false) {
                    loadPlans();
                } else {
                    alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                }
            });
        });

        document.querySelectorAll('.btn-delete-plan').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const name = e.target.getAttribute('data-name');
                if (confirm(`Are you sure you want to delete plan "${name}"?`)) {
                    const res = await api.deleteAdminPlan(id);
                    if (res.success) {
                        alertContainer.innerHTML = `<div class="alert alert-success">Plan deleted successfully.</div>`;
                        loadPlans();
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${res.message}</div>`;
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    }
                }
            });
        });
    }

    async function editPlan(id) {
        alertContainer.innerHTML = '';
        const result = await api.getAdminPlan(id);

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        formTitle.textContent = "Edit Insurance Plan";
        editPlanId.value = result.id;
        nameInput.value = result.name;
        descriptionInput.value = result.description;
        coverageInput.value = result.coverageAmount;
        medicalInput.value = result.medicalCoverage;
        baggageInput.value = result.baggageCoverage;
        cancellationInput.value = result.tripCancellation;
        premiumInput.value = result.basePremium;
        emergencyInput.checked = result.emergencyAssistance;
        activeInput.checked = result.active;

        planFormCard.style.display = 'block';
        planFormCard.scrollIntoView({ behavior: 'smooth' });
    }

    // Toggle forms
    toggleFormBtn.addEventListener('click', () => {
        alertContainer.innerHTML = '';
        planForm.reset();
        editPlanId.value = '';
        formTitle.textContent = "Create New Insurance Plan";
        planFormCard.style.display = planFormCard.style.display === 'none' ? 'block' : 'none';
    });

    cancelPlanFormBtn.addEventListener('click', () => {
        planFormCard.style.display = 'none';
        planForm.reset();
        editPlanId.value = '';
    });

    // Form submit triggers POST or PUT requests
    planForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertContainer.innerHTML = '';

        const name = nameInput.value.trim();
        const description = descriptionInput.value.trim();
        const coverageAmount = parseFloat(coverageInput.value);
        const medicalCoverage = parseFloat(medicalInput.value);
        const baggageCoverage = parseFloat(baggageInput.value);
        const tripCancellation = parseFloat(cancellationInput.value);
        const basePremium = parseFloat(premiumInput.value);
        const emergencyAssistance = emergencyInput.checked;
        const active = activeInput.checked;

        // Front-end validations
        if (basePremium <= 0) {
            alertContainer.innerHTML = `<div class="alert alert-danger">Base premium must be greater than zero</div>`;
            return;
        }
        if (coverageAmount < 0 || medicalCoverage < 0 || baggageCoverage < 0 || tripCancellation < 0) {
            alertContainer.innerHTML = `<div class="alert alert-danger">Coverage values cannot be negative</div>`;
            return;
        }

        const payload = {
            name, description, coverageAmount, medicalCoverage, baggageCoverage, tripCancellation,
            emergencyAssistance, basePremium, active
        };

        const id = editPlanId.value;
        let response;

        if (id) {
            response = await api.updateAdminPlan(id, payload);
        } else {
            response = await api.createAdminPlan(payload);
        }

        if (response.success !== false) {
            alertContainer.innerHTML = `<div class="alert alert-success">Plan saved successfully!</div>`;
            planForm.reset();
            planFormCard.style.display = 'none';
            loadPlans();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            alertContainer.innerHTML = `<div class="alert alert-danger">${response.message}</div>`;
        }
    });

    // Filter listeners
    searchAdminPlan.addEventListener('input', applyFiltersAndRender);
    filterStatus.addEventListener('change', applyFiltersAndRender);

    // Initial load
    await loadPlans();
});
