import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Home = () => {
  const { user } = useAuth();

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-lg-8 mx-auto text-center">
          <h1 className="display-4 fw-bold mb-4">
            Welcome to Hiring Platform
          </h1>
          <p className="lead mb-4">
            Connect recruiters with talented job seekers. Find your dream job or hire the perfect candidate.
          </p>
          <div className="d-flex justify-content-center gap-3 mb-5">
            {!user && (
              <>
                <Link to="/register" className="btn btn-primary btn-lg">
                  Get Started
                </Link>
                <Link to="/login" className="btn btn-outline-primary btn-lg">
                  Sign In
                </Link>
              </>
            )}
            <Link to="/jobs" className="btn btn-success btn-lg">
              Browse Jobs
            </Link>
          </div>
        </div>
      </div>
      
      <div className="row mt-5">
        <div className="col-md-4 mb-4">
          <div className="card h-100 text-center shadow-sm">
            <div className="card-body">
              <i className="bi bi-person-workspace display-4 text-primary mb-3"></i>
              <h5 className="card-title">For Job Seekers</h5>
              <p className="card-text">
                Create your profile, browse jobs, and apply to positions that match your skills.
              </p>
            </div>
          </div>
        </div>
        <div className="col-md-4 mb-4">
          <div className="card h-100 text-center shadow-sm">
            <div className="card-body">
              <i className="bi bi-briefcase display-4 text-success mb-3"></i>
              <h5 className="card-title">For Recruiters</h5>
              <p className="card-text">
                Post job openings, search for candidates, and build your dream team.
              </p>
            </div>
          </div>
        </div>
        <div className="col-md-4 mb-4">
          <div className="card h-100 text-center shadow-sm">
            <div className="card-body">
              <i className="bi bi-search display-4 text-info mb-3"></i>
              <h5 className="card-title">Easy Search</h5>
              <p className="card-text">
                Powerful search functionality to find exactly what you're looking for.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
