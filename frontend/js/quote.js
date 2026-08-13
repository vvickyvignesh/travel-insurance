document.addEventListener('DOMContentLoaded', async () => {
    if (!auth.getToken()) return;

    const alertContainer = document.getElementById('alertContainer');
    const quoteCard = document.getElementById('quoteCard');

    // Values elements
    const quoteAppNumber = document.getElementById('quoteAppNumber');
    const quotePlanName = document.getElementById('quotePlanName');
    const quoteDestination = document.getElementById('quoteDestination');
    const quoteDuration = document.getElementById('quoteDuration');
    const quoteTripType = document.getElementById('quoteTripType');
    const quoteAge = document.getElementById('quoteAge');

    // Table breakdown elements
    const breakdownBase = document.getElementById('breakdownBase');
    const breakdownDuration = document.getElementById('breakdownDuration');
    const breakdownAge = document.getElementById('breakdownAge');
    const breakdownDestination = document.getElementById('breakdownDestination');
    const breakdownTripType = document.getElementById('breakdownTripType');
    const breakdownFinal = document.getElementById('breakdownFinal');

    const proceedToPaymentBtn = document.getElementById('proceedToPaymentBtn');
    const backToAppBtn = document.getElementById('backToAppBtn');

    // Parse URL query parameter: quote.html?applicationId=1
    const params = new URLSearchParams(window.location.search);
    const appId = params.get('applicationId');

    if (!appId) {
        alertContainer.innerHTML = `<div class="alert alert-danger">Invalid Quote Request: missing Application ID parameter</div>`;
        return;
    }

    async function loadQuote() {
        alertContainer.innerHTML = '';
        const result = await api.getQuote(appId);

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        const data = result.data;

        // Populate details
        quoteAppNumber.textContent = data.applicationNumber;
        quotePlanName.textContent = data.planName;
        quoteDestination.textContent = data.destination;
        quoteDuration.textContent = `${data.tripDuration} Days`;
        quoteTripType.textContent = formatTripType(data.tripType);
        quoteAge.textContent = `${data.travellerAge} Years`;

        // Populate table factors
        breakdownBase.textContent = `₹${data.basePremium.toLocaleString('en-IN')}`;
        breakdownDuration.textContent = data.durationMultiplier.toFixed(2);
        breakdownAge.textContent = data.ageMultiplier.toFixed(2);
        breakdownDestination.textContent = data.destinationMultiplier.toFixed(2);
        breakdownTripType.textContent = data.tripTypeMultiplier.toFixed(2);
        breakdownFinal.textContent = `₹${data.finalPremium.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

        // Back button href setting
        backToAppBtn.href = `application-review.html?id=${appId}`;

        quoteCard.style.display = 'block';
    }

    function formatTripType(type) {
        if (!type) return '—';
        return type.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
    }

    // Proceed to Payment button
    proceedToPaymentBtn.addEventListener('click', () => {
        window.location.href = `payment.html?applicationId=${appId}`;
    });

    // Load data
    await loadQuote();
});
