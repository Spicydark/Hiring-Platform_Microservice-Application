import axiosInstance from './axiosConfig';

// Register a new user
export const registerUser = async (userData) => {
  const response = await axiosInstance.post('/register', userData);
  return response.data;
};

// Login user
export const loginUser = async (credentials) => {
  const response = await axiosInstance.post('/login', credentials);
  return response.data;
};

// Get user details by ID
export const getUserById = async (userId) => {
  const response = await axiosInstance.get(`/users/${userId}`);
  return response.data;
};
