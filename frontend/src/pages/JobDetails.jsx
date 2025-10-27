import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getJobById, applyForJob } from '../api/jobService';

const JobDetails = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [applying, setApplying] = useState(false);
  const [applicationSuccess, setApplicationSuccess] = useState(false);

  useEffect(() => {
    const fetchJobDetails = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await getJobById(id);
        setJob(data);
      } catch (err) {
        setError('Failed to load job details. Please try again.');
        console.error('Error fetching job details:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchJobDetails();
  }, [id]);

  const handleApply = async () => {
    if (!user) {
      navigate('/login', { state: { from: { pathname: `/jobs/${id}` } } });
      return;
    }

    setApplying(true);
    setError('');
    try {
      await applyForJob(id);
      setApplicationSuccess(true);
      setTimeout(() => setApplicationSuccess(false), 5000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to apply for the job. Please try again.');
      console.error('Error applying for job:', err);
    } finally {
      setApplying(false);
    }
  };

  if (loading) {
    return (
      <div className="container mt-4">
        <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error && !job) {
    return (
      <div className="container mt-4">
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/jobs')}>
          Back to Jobs
        </button>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="row">
        <div className="col-lg-8 mx-auto">
          <div className="card shadow">
            <div className="card-body p-4">
              <h2 className="card-title text-primary mb-3">{job.role}</h2>
              
              {applicationSuccess && (
                <div className="alert alert-success" role="alert">
                  <i className="bi bi-check-circle me-2"></i>
                  Application submitted successfully!
                </div>
              )}
              
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <div className="mb-4">
                <h5 className="mb-3">Job Description</h5>
                <p className="text-muted">{job.description}</p>
              </div>

              <div className="mb-4">
                <h6 className="mb-2">
                  <i className="bi bi-briefcase me-2"></i>
                  Experience Required
                </h6>
                <p className="ms-4">
                  {job.experience} {job.experience === 1 ? 'year' : 'years'}
                </p>
              </div>

              <div className="mb-4">
                <h6 className="mb-2">
                  <i className="bi bi-gear me-2"></i>
                  Required Skills
                </h6>
                <div className="ms-4 d-flex flex-wrap gap-2">
                  {job.skillSet && job.skillSet.map((skill, index) => (
                    <span key={index} className="badge bg-primary">
                      {skill}
                    </span>
                  ))}
                </div>
              </div>

              <div className="d-flex gap-2 mt-4">
                {user && user.role === 'JOB_SEEKER' && (
                  <button
                    className="btn btn-success"
                    onClick={handleApply}
                    disabled={applying || applicationSuccess}
                  >
                    {applying ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                        Applying...
                      </>
                    ) : applicationSuccess ? (
                      <>
                        <i className="bi bi-check-circle me-2"></i>
                        Applied
                      </>
                    ) : (
                      <>
                        <i className="bi bi-send me-2"></i>
                        Apply Now
                      </>
                    )}
                  </button>
                )}
                <button className="btn btn-outline-secondary" onClick={() => navigate('/jobs')}>
                  <i className="bi bi-arrow-left me-2"></i>
                  Back to Jobs
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default JobDetails;
