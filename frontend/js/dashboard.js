document.addEventListener('DOMContentLoaded', async () => {
    // Check if authenticated first
    if (!auth.getToken()) return;

    const usernameDisplay = document.getElementById('usernameDisplay');
    const profileName = document.getElementById('profileName');
    const profileEmail = document.getElementById('profileEmail');
    const profilePhone = document.getElementById('profilePhone');
    const profileRole = document.getElementById('profileRole');
    const adminLinkContainer = document.getElementById('adminLinkContainer');

    // Fetch fresh profile details from API
    const result = await api.getProfile();

    if (result.success !== false) {
        usernameDisplay.textContent = result.name;
        profileName.textContent = result.name;
        profileEmail.textContent = result.email;
        profilePhone.textContent = result.phone || 'Not Provided';
        profileRole.textContent = result.role;

        // Apply badge color
        if (result.role === 'ADMIN') {
            profileRole.className = 'badge badge-admin';
            if (adminLinkContainer) {
                adminLinkContainer.style.display = 'block';
            }
        } else {
            profileRole.className = 'badge badge-user';
        }
    } else {
        // Token might be invalid or expired
        console.error('Failed to load profile:', result.message);
        auth.logout();
    }
});
