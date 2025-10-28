import React, { createContext, useState, useContext, useEffect } from 'react';
import { loginUser as loginApi, registerUser as registerApi } from '../api/authService';
import { parseJwt } from '../utils/jwtUtils';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  // Login function
  const login = async (credentials) => {
    try {
      const response = await loginApi(credentials);
      
      // Check if response contains both token and user data
      let jwtToken, userData;
      
      if (typeof response === 'string') {
        // Response is just the token
        jwtToken = response;
        userData = parseJwt(jwtToken);
      } else if (response.token) {
        // Response is an object with token and possibly user data
        jwtToken = response.token;
        userData = response.user || parseJwt(jwtToken);
      } else {
        throw new Error('Invalid response format');
      }
      
      if (userData) {
        // If no ID in token, use username as fallback (NOT IDEAL)
        if (!userData.id) {
          console.warn('⚠️ No user ID in JWT token. Using username as recruiterId. Please fix backend!');
          userData.id = userData.username;
        }
        
        setToken(jwtToken);
        setUser(userData);
        localStorage.setItem('token', jwtToken);
        localStorage.setItem('user', JSON.stringify(userData));
        return { success: true };
      } else {
        throw new Error('Invalid token');
      }
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login failed. Please check your credentials.' 
      };
    }
  };

  // Register function
  const register = async (userData) => {
    try {
      await registerApi(userData);
      return { success: true };
    } catch (error) {
      console.error('Registration error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed. Please try again.' 
      };
    }
  };

  // Logout function
  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const value = {
    user,
    token,
    login,
    register,
    logout,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Custom hook to use auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
