import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { createOrUpdateProfile, getProfileByUserId } from '../api/candidateService';

const Profile = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    totalExperience: '',
    skills: '',
    resumeUrl: '',
  });
  const [loading, setLoading] = useState(false);
  const [fetchingProfile, setFetchingProfile] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      setFetchingProfile(true);
      try {
        const profile = await getProfileByUserId(user.id);
        if (profile) {
          setFormData({
            fullName: profile.fullName || '',
            email: profile.email || '',
            totalExperience: profile.totalExperience || '',
            skills: profile.skills ? profile.skills.join(', ') : '',
            resumeUrl: profile.resumeUrl || '',
          });
        }
      } catch (err) {
        // Profile doesn't exist yet, which is fine
        console.log('No existing profile found');
      } finally {
        setFetchingProfile(false);
      }
    };

    fetchProfile();
  }, [user.id]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
    setSuccess(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    // Validation
    if (!formData.fullName || !formData.email || !formData.totalExperience || !formData.skills || !formData.resumeUrl) {
      setError('Please fill in all fields');
      setLoading(false);
      return;
    }

    if (formData.totalExperience < 0) {
      setError('Experience cannot be negative');
      setLoading(false);
      return;
    }

    // Convert skills from comma-separated string to array
    const skillsArray = formData.skills
      .split(',')
      .map(skill => skill.trim())
      .filter(skill => skill.length > 0);

    if (skillsArray.length === 0) {
      setError('Please enter at least one skill');
      setLoading(false);
      return;
    }

    const profileData = {
      fullName: formData.fullName,
      email: formData.email,
      totalExperience: parseInt(formData.totalExperience),
      skills: skillsArray,
      resumeUrl: formData.resumeUrl,
    };

    try {
      await createOrUpdateProfile(profileData);
      setSuccess(true);
      setTimeout(() => setSuccess(false), 5000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save profile. Please try again.');
      console.error('Error saving profile:', err);
    } finally {
      setLoading(false);
    }
  };

  if (fetchingProfile) {
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

  return (
    <div className="container mt-4">
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card shadow">
            <div className="card-body p-4">
              <h2 className="card-title mb-4">My Profile</h2>

              {success && (
                <div className="alert alert-success" role="alert">
                  <i className="bi bi-check-circle me-2"></i>
                  Profile saved successfully!
                </div>
              )}

              {error && (
                <div className="alert alert-danger" role="alert">
                  {error}
                </div>
              )}

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="fullName" className="form-label">
                    Full Name <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="fullName"
                    name="fullName"
                    value={formData.fullName}
                    onChange={handleChange}
                    placeholder="Your full name"
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="email" className="form-label">
                    Email <span className="text-danger">*</span>
                  </label>
                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="your.email@example.com"
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="totalExperience" className="form-label">
                    Total Experience (Years) <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className="form-control"
                    id="totalExperience"
                    name="totalExperience"
                    min="0"
                    value={formData.totalExperience}
                    onChange={handleChange}
                    placeholder="e.g., 3"
                    required
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="skills" className="form-label">
                    Skills <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="skills"
                    name="skills"
                    value={formData.skills}
                    onChange={handleChange}
                    placeholder="Enter skills separated by commas (e.g., Java, Python, React)"
                    required
                  />
                  <small className="form-text text-muted">
                    Separate multiple skills with commas
                  </small>
                </div>

                <div className="mb-4">
                  <label htmlFor="resumeUrl" className="form-label">
                    Resume URL <span className="text-danger">*</span>
                  </label>
                  <input
                    type="url"
                    className="form-control"
                    id="resumeUrl"
                    name="resumeUrl"
                    value={formData.resumeUrl}
                    onChange={handleChange}
                    placeholder="https://example.com/your-resume.pdf"
                    required
                  />
                  <small className="form-text text-muted">
                    Link to your resume (e.g., Google Drive, Dropbox, or personal website)
                  </small>
                </div>

                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      Saving...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-save me-2"></i>
                      Save Profile
                    </>
                  )}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
