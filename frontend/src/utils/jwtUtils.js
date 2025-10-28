import { jwtDecode } from 'jwt-decode';

export const parseJwt = (token) => {
  try {
    const decoded = jwtDecode(token);
    
    // Debug: Log the decoded token to see what fields are available
    console.log('Decoded JWT:', decoded);

    // Extract role safely
    let role = 'JOB_SEEKER'; // default
    if (decoded.roles && decoded.roles.length > 0) {
      role = decoded.roles[0].replace('ROLE_', '');
    } else if (decoded.role) {
      role = decoded.role.replace('ROLE_', '');
    } else if (decoded.authorities && decoded.authorities.length > 0) {
      role = decoded.authorities[0].replace('ROLE_', '');
    }

    // Extract user ID - try multiple possible field names
    const userId = decoded.userId || decoded.id || decoded._id || decoded.user_id;
    
    if (!userId) {
      console.warn('No user ID found in JWT token. Available fields:', Object.keys(decoded));
    }

    return {
      id: userId,
      username: decoded.username || decoded.sub,
      role: role,
    };
  } catch (error) {
    console.error('Error parsing JWT:', error);
    return null;
  }
};
