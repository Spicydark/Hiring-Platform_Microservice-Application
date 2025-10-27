import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { createJob } from '../api/jobService';

const PostJob = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    role: '',
    description: '',
    experience: '',
    skillSet: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    // Validation
    if (!formData.role || !formData.description || !formData.experience || !formData.skillSet) {
      setError('Please fill in all fields');
      setLoading(false);
      return;
    }

    if (formData.experience < 0) {
      setError('Experience cannot be negative');
      setLoading(false);
      return;
    }

    // Convert skillSet from comma-separated string to array
    const skillsArray = formData.skillSet
      .split(',')
      .map(skill => skill.trim())
      .filter(skill => skill.length > 0);

    if (skillsArray.length === 0) {
      setError('Please enter at least one skill');
      setLoading(false);
      return;
    }

    const jobData = {
      role: formData.role,
      description: formData.description,
      experience: parseInt(formData.experience),
      skillSet: skillsArray,
      recruiterId: user.id,
    };

    try {
      await createJob(jobData);
      setSuccess(true);
      setFormData({
        role: '',
        description: '',
        experience: '',
        skillSet: '',
      });
      setTimeout(() => {
        navigate('/jobs');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create job posting. Please try again.');
      console.error('Error creating job:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card shadow">
            <div className="card-body p-4">
              <h2 className="card-title mb-4">Post a New Job</h2>

              {success && (
                <div className="alert alert-success" role="alert">
                  <i className="bi bi-check-circle me-2"></i>
                  Job posted successfully! Redirecting to jobs page...
                </div>
              )}

              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="role" className="form-label">
                    Job Title / Role <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="role"
                    name="role"
                    value={formData.role}
                    onChange={handleChange}
                    placeholder="e.g., Senior Software Engineer"
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="description" className="form-label">
                    Job Description <span className="text-danger">*</span>
                  </label>
                  <textarea
                    className="form-control"
                    id="description"
                    name="description"
                    rows="5"
                    value={formData.description}
                    onChange={handleChange}
                    placeholder="Describe the role, responsibilities, and requirements..."
                    required
                  ></textarea>
                </div>

                <div className="mb-3">
                  <label htmlFor="experience" className="form-label">
                    Experience Required (Years) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="experience"
                    name="experience"
                    min="0"
                    value={formData.experience}
                    onChange={handleChange}
                    placeholder="e.g., 3"
                    required
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="skillSet" className="form-label">
                    Required Skills <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="skillSet"
                    name="skillSet"
                    value={formData.skillSet}
                    onChange={handleChange}
                    placeholder="Enter skills separated by commas (e.g., Java, Spring Boot, React)"
                    required
                  />
                  <small className="form-text text-muted">
                    Separate multiple skills with commas
                  </small>
                </div>

                <div className="d-flex gap-2">
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading || success}
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                        Posting...
                      </>
                    ) : (
                      <>
                        <i className="bi bi-plus-circle me-2"></i>
                        Post Job
                      </>
                    )}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-secondary"
                    onClick={() => navigate('/jobs')}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PostJob;
