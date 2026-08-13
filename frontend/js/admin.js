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
    const searchUserEl = document.getElementById('searchUser');

    let allUsers = [];

    async function loadUsers() {
        alertContainer.innerHTML = '';
        const result = await api.getAdminUsers();

        if (result.success === false) {
            alertContainer.innerHTML = `<div class="alert alert-danger">${result.message}</div>`;
            return;
        }

        allUsers = result;
        renderUsers(allUsers);
    }

    function renderUsers(usersList) {
        totalUsersEl.textContent = usersList.length;
        usersTableBody.innerHTML = '';

        if (usersList.length === 0) {
            usersTableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; color: var(--text-muted);">No users found</td>
                </tr>
            `;
            return;
        }

        usersList.forEach(user => {
            const row = document.createElement('tr');
            row.id = `user-row-${user.id}`;
            const roleBadgeClass = user.role === 'ADMIN' ? 'badge badge-admin' : 'badge badge-user';
            
            // Format Created At
            const dateStr = user.createdAt ? user.createdAt : '—';
            
            // Render delete button (disabled if it is the current user)
            const isSelf = currentUser.id === user.id;
            const deleteBtnHtml = isSelf 
                ? `<button class="btn btn-secondary" style="padding: 0.25rem 0.5rem; font-size: 0.85rem; opacity: 0.5; cursor: not-allowed;" disabled>Delete</button>`
                : `<button class="btn btn-primary btn-delete" data-id="${user.id}" data-name="${user.name}" style="background-color: var(--danger); padding: 0.25rem 0.5rem; font-size: 0.85rem;">Delete</button>`;

            row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>${user.phone || '—'}</td>
                <td><span class="${roleBadgeClass}">${user.role}</span></td>
                <td>${dateStr}</td>
                <td>${deleteBtnHtml}</td>
            `;
            usersTableBody.appendChild(row);
        });

        // Attach event listeners to delete buttons
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = e.target.getAttribute('data-id');
                const name = e.target.getAttribute('data-name');

                if (confirm(`Are you sure you want to delete user "${name}"?`)) {
                    alertContainer.innerHTML = '';
                    const delResult = await api.deleteUser(id);

                    if (delResult.success) {
                        alertContainer.innerHTML = `<div class="alert alert-success">User "${name}" deleted successfully.</div>`;
                        loadUsers(); // Refresh the list
                    } else {
                        alertContainer.innerHTML = `<div class="alert alert-danger">${delResult.message}</div>`;
                    }
                }
            });
        });
    }

    // Search Filtering Handler
    searchUserEl.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        if (!query) {
            renderUsers(allUsers);
            return;
        }

        const filtered = allUsers.filter(user => {
            const nameMatch = user.name && user.name.toLowerCase().includes(query);
            const emailMatch = user.email && user.email.toLowerCase().includes(query);
            const phoneMatch = user.phone && user.phone.toLowerCase().includes(query);
            const roleMatch = user.role && user.role.toLowerCase().includes(query);
            const idMatch = user.id && user.id.toString().includes(query);

            return nameMatch || emailMatch || phoneMatch || roleMatch || idMatch;
        });

        renderUsers(filtered);
    });

    // Initial load
    await loadUsers();
});
