import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
      <div className="container-fluid">
        <Link className="navbar-brand fw-bold" to="/">
          <i className="bi bi-briefcase-fill me-2"></i>
          Hiring Platform
        </Link>
        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarNav"
          aria-controls="navbarNav"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
          <ul className="navbar-nav me-auto">
            <li className="nav-item">
              <Link className="nav-link" to="/jobs">
                Job Vacancy
              </Link>
            </li>
            {user && user.role === 'RECRUITER' && (
              <li className="nav-item">
                <Link className="nav-link" to="/post-job">
                  Post a Job
                </Link>
              </li>
            )}
            {user && user.role === 'JOB_SEEKER' && (
              <li className="nav-item">
                <Link className="nav-link" to="/profile">
                  My Profile
                </Link>
              </li>
            )}
          </ul>
          <ul className="navbar-nav">
            {user ? (
              <>
                <li className="nav-item d-flex align-items-center me-3">
                  <span className="text-white">
                    <i className="bi bi-person-circle me-2"></i>
                    {user.username}
                    <span className="badge bg-light text-primary ms-2">
                      {user.role === 'RECRUITER' ? 'Recruiter' : 'Job Seeker'}
                    </span>
                  </span>
                </li>
                <li className="nav-item">
                  <button className="btn btn-outline-light" onClick={handleLogout}>
                    Logout
                  </button>
                </li>
              </>
            ) : (
              <>
                <li className="nav-item me-2">
                  <Link className="btn btn-outline-light" to="/login">
                    Sign In
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="btn btn-light" to="/register">
                    Sign Up
                  </Link>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
