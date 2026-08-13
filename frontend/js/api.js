const API_BASE_URL = 'http://localhost:8080/api';

const api = {
    // Send standard JSON requests
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('token');
        const headers = {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
            ...options.headers
        };

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            const data = await response.json();

            if (!response.ok) {
                return {
                    success: false,
                    status: response.status,
                    message: data.message || 'Something went wrong'
                };
            }

            return data;
        } catch (error) {
            console.error('Fetch error:', error);
            return {
                success: false,
                message: 'Failed to connect to server. Please ensure the backend is running.'
            };
        }
    },

    // Authentication APIs
    register(userData) {
        return this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },

    login(credentials) {
        return this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },

    // User Profile API
    getProfile() {
        return this.request('/user/profile', {
            method: 'GET'
        });
    },

    // Admin Users Directory API
    getAdminUsers() {
        return this.request('/admin/users', {
            method: 'GET'
        });
    }
};
