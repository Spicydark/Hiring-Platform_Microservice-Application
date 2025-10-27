import { jwtDecode } from 'jwt-decode';

// Parse JWT token to extract user information
export const parseJwt = (token) => {
  try {
    const decoded = jwtDecode(token);
    return {
      id: decoded.userId || decoded.sub,
      username: decoded.username || decoded.sub,
      role: decoded.role || decoded.authorities?.[0] || 'JOB_SEEKER',
    };
  } catch (error) {
    console.error('Error parsing JWT:', error);
    return null;
  }
};
