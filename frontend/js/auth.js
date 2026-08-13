const auth = {
    saveSession(token, user) {
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
    },

    clearSession() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getToken() {
        return localStorage.getItem('token');
    },

    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        try {
            return userStr ? JSON.parse(userStr) : null;
        } catch (e) {
            return null;
        }
    },

    logout() {
        this.clearSession();
        window.location.href = 'login.html';
    },

    checkAuth() {
        const token = this.getToken();
        const user = this.getCurrentUser();
        const path = window.location.pathname;

        // Pages that require authentication
        const isProtectedRoute = path.includes('dashboard.html') || path.includes('admin.html');
        // Pages that only ADMIN can access
        const isAdminRoute = path.includes('admin.html');
        // Pages that are for guests only
        const isGuestRoute = path.includes('login.html') || path.includes('register.html');

        if (isProtectedRoute) {
            if (!token || !user) {
                this.clearSession();
                window.location.href = 'login.html';
                return;
            }

            if (isAdminRoute && user.role !== 'ADMIN') {
                window.location.href = 'dashboard.html';
                return;
            }
        }

        if (isGuestRoute && token && user) {
            window.location.href = 'dashboard.html';
        }
    }
};

// Check authentication status automatically on script load
document.addEventListener('DOMContentLoaded', () => {
    auth.checkAuth();

    // Hook up logout button if present
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            auth.logout();
        });
    }
});
