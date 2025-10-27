import axiosInstance from './axiosConfig';

// Get all jobs
export const getAllJobs = async () => {
  const response = await axiosInstance.get('/posts/all');
  return response.data;
};

// Search jobs by keyword
export const searchJobs = async (keyword) => {
  const response = await axiosInstance.get(`/posts/search/${keyword}`);
  return response.data;
};

// Get job by ID
export const getJobById = async (jobId) => {
  const response = await axiosInstance.get(`/posts/${jobId}`);
  return response.data;
};

// Create a new job posting (Recruiter only)
export const createJob = async (jobData) => {
  const response = await axiosInstance.post('/posts/add', jobData);
  return response.data;
};

// Apply for a job (Job Seeker only)
export const applyForJob = async (jobId) => {
  const response = await axiosInstance.post(`/posts/apply/${jobId}`);
  return response.data;
};
