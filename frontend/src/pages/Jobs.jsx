import React, { useState, useEffect } from 'react';
import JobCard from '../components/JobCard';
import { getAllJobs, searchJobs } from '../api/jobService';

const Jobs = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');

  // Fetch all jobs on component mount
  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getAllJobs();
      setJobs(data);
    } catch (err) {
      setError('Failed to load jobs. Please try again later.');
      console.error('Error fetching jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchKeyword.trim()) {
      fetchJobs();
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await searchJobs(searchKeyword);
      setJobs(data);
    } catch (err) {
      setError('Failed to search jobs. Please try again.');
      console.error('Error searching jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchKeyword('');
    fetchJobs();
  };

  return (
    <div className="container mt-4">
      <div className="row mb-4">
        <div className="col">
          <h2 className="mb-3">Job Vacancies</h2>
          <form onSubmit={handleSearch}>
            <div className="input-group mb-3">
              <input
                type="text"
                className="form-control"
                placeholder="Search jobs by keywords..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
              />
              <button className="btn btn-primary" type="submit">
                <i className="bi bi-search me-1"></i>
                Search
              </button>
              {searchKeyword && (
                <button
                  className="btn btn-outline-secondary"
                  type="button"
                  onClick={handleClearSearch}
                >
                  Clear
                </button>
              )}
            </div>
          </form>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '300px' }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : jobs.length === 0 ? (
        <div className="alert alert-info text-center">
          <i className="bi bi-info-circle me-2"></i>
          No jobs found. {searchKeyword && 'Try a different search term.'}
        </div>
      ) : (
        <>
          <p className="text-muted mb-3">
            Showing {jobs.length} {jobs.length === 1 ? 'job' : 'jobs'}
          </p>
          <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
            {jobs.map((job) => (
              <div className="col" key={job.id}>
                <JobCard job={job} />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default Jobs;
