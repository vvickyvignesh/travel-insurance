document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    const alertContainer = document.getElementById('alertContainer');
    const planDetailsCard = document.getElementById('planDetailsCard');
    const planName = document.getElementById('planName');
    const planDescription = document.getElementById('planDescription');
    const detailTotalLimit = document.getElementById('detailTotalLimit');
    const detailMedicalLimit = document.getElementById('detailMedicalLimit');
    const detailBaggageLimit = document.getElementById('detailBaggageLimit');
    const detailCancellationLimit = document.getElementById('detailCancellationLimit');
    const detailEmergencyAssist = document.getElementById('detailEmergencyAssist');
    const planPremium = document.getElementById('planPremium');
    const selectPlanBtn = document.getElementById('selectPlanBtn');

    // Parse URL query parameter: plan-details.html?id=2
    const params = new URLSearchParams(window.location.search);
    const planId = params.get('id');

    if (!planId) {
        alertContainer.innerHTML = `<div class="alert alert-danger">Invalid Plan Selection: missing ID param</div>`;
        return;
    }

    const result = await api.getPlan(planId);

    if (result.success === false) {
        alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
        return;
    }

    // Render properties
    planName.textContent = result.name;
    planDescription.textContent = result.description;
    detailTotalLimit.textContent = `₹${result.coverageAmount.toLocaleString('en-IN')}`;
    detailMedicalLimit.textContent = `₹${result.medicalCoverage.toLocaleString('en-IN')}`;
    detailBaggageLimit.textContent = `₹${result.baggageCoverage.toLocaleString('en-IN')}`;
    detailCancellationLimit.textContent = `₹${result.tripCancellation.toLocaleString('en-IN')}`;
    detailEmergencyAssist.textContent = result.emergencyAssistance ? '✅ Included' : '❌ Not Included';
    planPremium.textContent = `₹${result.basePremium.toLocaleString('en-IN')}`;

    planDetailsCard.style.display = 'block';

    // Hook Select Plan button click
    selectPlanBtn.addEventListener('click', () => {
        localStorage.setItem('selectedPlanId', result.id);
        alertContainer.innerHTML = `<div class="alert alert-success">"${result.name}" selected successfully!</div>`;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
});
