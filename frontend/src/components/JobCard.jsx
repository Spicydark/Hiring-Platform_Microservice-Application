import React from 'react';
import { Link } from 'react-router-dom';

const JobCard = ({ job }) => {
  return (
    <div className="card h-100 shadow-sm">
      <div className="card-body">
        <h5 className="card-title text-primary">{job.role}</h5>
        <p className="card-text text-muted">
          {job.description && job.description.length > 100
            ? `${job.description.substring(0, 100)}...`
            : job.description}
        </p>
        <div className="mb-2">
          <small className="text-muted">
            <i className="bi bi-briefcase me-1"></i>
            Experience: {job.experience} {job.experience === 1 ? 'year' : 'years'}
          </small>
        </div>
        <div className="mb-3">
          <small className="text-muted d-block mb-1">
            <i className="bi bi-gear me-1"></i>Skills:
          </small>
          <div className="d-flex flex-wrap gap-1">
            {job.skillSet && job.skillSet.slice(0, 3).map((skill, index) => (
              <span key={index} className="badge bg-secondary">
                {skill}
              </span>
            ))}
            {job.skillSet && job.skillSet.length > 3 && (
              <span className="badge bg-light text-dark">
                +{job.skillSet.length - 3} more
              </span>
            )}
          </div>
        </div>
        <Link to={`/jobs/${job.id}`} className="btn btn-primary btn-sm">
          View Details
        </Link>
      </div>
    </div>
  );
};

export default JobCard;
