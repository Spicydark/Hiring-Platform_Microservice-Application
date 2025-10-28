import { jwtDecode } from 'jwt-decode';

export const parseJwt = (token) => {
  try {
    const decoded = jwtDecode(token);

    // Extract role safely
    let role = 'JOB_SEEKER'; // default
    if (decoded.roles && decoded.roles.length > 0) {
      role = decoded.roles[0].replace('ROLE_', '');
    } else if (decoded.role) {
      role = decoded.role.replace('ROLE_', '');
    } else if (decoded.authorities && decoded.authorities.length > 0) {
      role = decoded.authorities[0].replace('ROLE_', '');
    }

    return {
      id: decoded.userId || decoded.sub,
      username: decoded.username || decoded.sub,
      role: role,
    };
  } catch (error) {
    console.error('Error parsing JWT:', error);
    return null;
  }
};
