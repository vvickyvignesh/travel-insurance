document.addEventListener('DOMContentLoaded', async () => {
    // Check if authenticated first
    if (!auth.getToken()) return;

    const usernameDisplay = document.getElementById('usernameDisplay');
    const profileName = document.getElementById('profileName');
    const profileEmail = document.getElementById('profileEmail');
    const profilePhone = document.getElementById('profilePhone');
    const profileRole = document.getElementById('profileRole');
    const adminLinkContainer = document.getElementById('adminLinkContainer');

    const editProfileBtn = document.getElementById('editProfileBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const profileViewCard = document.getElementById('profileViewCard');
    const profileEditCard = document.getElementById('profileEditCard');
    const editProfileForm = document.getElementById('editProfileForm');
    const editNameInput = document.getElementById('editName');
    const editPhoneInput = document.getElementById('editPhone');
    const alertContainer = document.getElementById('alertContainer');

    function renderProfileData(data) {
        usernameDisplay.textContent = data.name;
        profileName.textContent = data.name;
        profileEmail.textContent = data.email;
        profilePhone.textContent = data.phone || 'Not Provided';
        profileRole.textContent = data.role;

        // Apply badge color
        if (data.role === 'ADMIN') {
            profileRole.className = 'badge badge-admin';
            if (adminLinkContainer) {
                adminLinkContainer.style.display = 'block';
            }
        } else {
            profileRole.className = 'badge badge-user';
        }

        // Keep local user details sync
        auth.saveSession(auth.getToken(), data);
    }

    // Fetch fresh profile details from API
    const result = await api.getProfile();

    if (result.success !== false) {
        renderProfileData(result);
    } else {
        console.error('Failed to load profile:', result.message);
        auth.logout();
        return;
    }

    // Event listener for showing Edit Form
    editProfileBtn.addEventListener('click', () => {
        alertContainer.innerHTML = '';
        editNameInput.value = profileName.textContent;
        editPhoneInput.value = profilePhone.textContent === 'Not Provided' ? '' : profilePhone.textContent;
        profileViewCard.style.display = 'none';
        profileEditCard.style.display = 'block';
    });

    // Event listener for hiding Edit Form
    cancelEditBtn.addEventListener('click', () => {
        profileViewCard.style.display = 'block';
        profileEditCard.style.display = 'none';
    });

    // Submit Handler
    editProfileForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        alertContainer.innerHTML = '';

        const name = editNameInput.value.trim();
        const phone = editPhoneInput.value.trim();

        if (!name) {
            alertContainer.innerHTML = `<div class="alert alert-danger">Name cannot be empty</div>`;
            return;
        }

        const updateResult = await api.updateProfile({ name, phone });

        if (updateResult.success !== false) {
            renderProfileData(updateResult);
            alertContainer.innerHTML = `<div class="alert alert-success">Profile updated successfully!</div>`;
            profileViewCard.style.display = 'block';
            profileEditCard.style.display = 'none';
        } else {
            alertContainer.innerHTML = `<div class="alert alert-danger">${updateResult.message}</div>`;
        }
    });
});
