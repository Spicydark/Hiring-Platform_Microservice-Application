# Hiring Platform - Frontend

This is the frontend React application for the Hiring Platform. It provides a user-friendly interface for both recruiters and job seekers to interact with the platform.

## Features

- **Authentication**: User registration and login with JWT-based authentication
- **Role-based Access**: Different views and capabilities for Recruiters and Job Seekers
- **Job Browsing**: View and search for job postings
- **Job Application**: Job seekers can apply for jobs
- **Job Posting**: Recruiters can create new job postings
- **Profile Management**: Job seekers can create and manage their professional profiles
- **Responsive Design**: Mobile-friendly UI using Bootstrap

## Tech Stack

- **React** 18
- **React Router DOM** for navigation
- **Bootstrap** 5 for styling
- **Bootstrap Icons** for icons
- **Axios** for API calls
- **JWT Decode** for token parsing

## Prerequisites

- Node.js 14+ and npm
- Backend API services running on http://localhost:8080

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

## Running the Application

### Development Mode

Start the development server:

```bash
npm start
```

The application will open at [http://localhost:3000](http://localhost:3000).

### Production Build

Create an optimized production build:

```bash
npm run build
```

The build files will be in the `build/` directory.

## Project Structure

```
frontend/
├── public/              # Static files
├── src/
│   ├── api/            # API service layer
│   │   ├── config.js           # API configuration
│   │   ├── axiosConfig.js      # Axios instance with interceptors
│   │   ├── authService.js      # Authentication API calls
│   │   ├── jobService.js       # Job-related API calls
│   │   └── candidateService.js # Candidate profile API calls
│   ├── components/     # Reusable components
│   │   ├── Navbar.jsx          # Navigation bar
│   │   ├── JobCard.jsx         # Job listing card
│   │   └── ProtectedRoute.jsx  # Route protection wrapper
│   ├── context/        # React Context
│   │   └── AuthContext.jsx     # Authentication context
│   ├── pages/          # Page components
│   │   ├── Home.jsx            # Landing page
│   │   ├── Login.jsx           # Login page
│   │   ├── Register.jsx        # Registration page
│   │   ├── Jobs.jsx            # Job listings page
│   │   ├── JobDetails.jsx      # Job details page
│   │   ├── PostJob.jsx         # Create job posting (Recruiter)
│   │   ├── Profile.jsx         # User profile (Job Seeker)
│   │   └── NotFound.jsx        # 404 page
│   ├── utils/          # Utility functions
│   │   └── jwtUtils.js         # JWT parsing utilities
│   ├── App.js          # Main app component with routes
│   ├── App.css         # App styles
│   ├── index.js        # App entry point
│   └── index.css       # Global styles
└── package.json        # Dependencies
```

## Configuration

The API base URL is configured in `src/api/config.js`. By default, it points to `http://localhost:8080`.

To change the API URL, update the `API_BASE_URL` constant:

```javascript
export const API_BASE_URL = 'http://your-api-url:port';
```

## User Flows

### Job Seeker Flow

1. **Register** as a Job Seeker
2. **Login** with credentials
3. **Browse Jobs** and search for opportunities
4. **View Job Details** and apply
5. **Create/Update Profile** with skills and experience

### Recruiter Flow

1. **Register** as a Recruiter
2. **Login** with credentials
3. **Post Jobs** with requirements
4. **View Job Listings** to see posted jobs

## Features in Detail

### Authentication

- JWT tokens are stored in localStorage
- Axios interceptor automatically adds the token to all requests
- Token expiration redirects users to login page

### Protected Routes

- Routes are protected based on authentication status
- Role-based access control (Recruiter vs Job Seeker)
- Unauthorized access redirects appropriately

### Error Handling

- API errors are caught and displayed to users
- Network failures show friendly error messages
- Form validation prevents invalid submissions

## Available Scripts

- `npm start` - Run development server
- `npm run build` - Create production build
- `npm test` - Run tests
- `npm run eject` - Eject from create-react-app (one-way operation)

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).
