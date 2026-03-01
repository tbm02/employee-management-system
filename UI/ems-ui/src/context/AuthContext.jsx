import { createContext, useContext, useState, useEffect } from 'react';
import { login as loginApi } from '../services/authService';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const savedToken = localStorage.getItem('accessToken');
        const savedUser = localStorage.getItem('user');
        if (savedToken && savedUser) {
            const parsedUser = JSON.parse(savedUser);
            const normalizedUser = {
                ...parsedUser,
                firstLogin: Boolean(parsedUser.firstLogin),
                passwordUpdated: parsedUser.passwordUpdated ?? true,
                personalDetailsUpdated: parsedUser.personalDetailsUpdated ?? true,
            };
            setToken(savedToken);
            setUser(normalizedUser);
            localStorage.setItem('user', JSON.stringify(normalizedUser));
        }
        setLoading(false);
    }, []);

    const login = async (identifier, password) => {
        const res = await loginApi(identifier, password);
        if (res.error) {
            throw new Error(res.message || 'Login failed');
        }
        const data = res.data;
        const userData = {
            userId: data.userId,
            email: data.email,
            empId: data.empId,
            role: data.role,
            firstLogin: Boolean(data.first_login),
            passwordUpdated: Boolean(data.is_password_updated),
            personalDetailsUpdated: Boolean(data.is_personal_details_updated),
        };
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('user', JSON.stringify(userData));
        setToken(data.accessToken);
        setUser(userData);
        return userData;
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    };

    const markPasswordUpdated = () => {
        if (user) {
            const updated = { ...user, firstLogin: false, passwordUpdated: true };
            localStorage.setItem('user', JSON.stringify(updated));
            setUser(updated);
        }
    };

    const markOnboardingComplete = () => {
        if (user) {
            const updated = { ...user, personalDetailsUpdated: true };
            localStorage.setItem('user', JSON.stringify(updated));
            setUser(updated);
        }
    };

    const value = {
        user,
        token,
        loading,
        login,
        logout,
        markPasswordUpdated,
        markOnboardingComplete,
        isAuthenticated: !!token,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
