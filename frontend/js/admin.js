document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication and role
    const token = auth.getToken();
    const currentUser = auth.getCurrentUser();
    if (!token || !currentUser || currentUser.role !== 'ADMIN') {
        window.location.href = 'dashboard.html';
        return;
    }

    const totalUsersEl = document.getElementById('totalUsers');
    const usersTableBody = document.getElementById('usersTableBody');
    const alertContainer = document.getElementById('alertContainer');

    const result = await api.getAdminUsers();

    if (result.success === false) {
        alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
        return;
    }

    // Update total count
    totalUsersEl.textContent = result.length;

    // Render users table
    usersTableBody.innerHTML = '';
    result.forEach(user => {
        const row = document.createElement('tr');
        const roleBadgeClass = user.role === 'ADMIN' ? 'badge badge-admin' : 'badge badge-user';
        
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.name}</td>
            <td>${user.email}</td>
            <td>${user.phone || '—'}</td>
            <td><span class="${roleBadgeClass}">${user.role}</span></td>
        `;
        usersTableBody.appendChild(row);
    });
});
