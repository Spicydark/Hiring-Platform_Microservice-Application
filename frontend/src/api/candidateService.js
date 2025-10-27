import axiosInstance from './axiosConfig';

// Create or update candidate profile
export const createOrUpdateProfile = async (profileData) => {
  const response = await axiosInstance.post('/candidate/profile', profileData);
  return response.data;
};

// Get candidate profile by user ID
export const getProfileByUserId = async (userId) => {
  const response = await axiosInstance.get(`/candidate/profile/${userId}`);
  return response.data;
};
