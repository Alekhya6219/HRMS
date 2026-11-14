
import React, { useState } from 'react';
import api from './api';

export default function Login({ onLogin }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', {
        email,
        password,
      });

      const user = response.data;

      sessionStorage.setItem('peoplepulse_token', user.token);
      sessionStorage.setItem(
        'peoplepulse_user',
        JSON.stringify({
          email: user.email,
          role: user.role,
          employeeId: user.employeeId,
        })
      );

      onLogin(user);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Invalid email or password. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-brand">
          <div className="brand-mark">P</div>
          <div>
            <h2>peoplepulse</h2>
            <p>PEOPLE & CULTURE</p>
          </div>
        </div>

        <div className="login-heading">
          <h1>Welcome back</h1>
          <p>Sign in to your PeoplePulse workspace.</p>
        </div>

        <form onSubmit={handleSubmit}>
          <label className="field">
            <span>Email address</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Enter your email"
              required
            />
          </label>

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </label>

          {error && <div className="login-error">{error}</div>}

          <button
            className="primary login-button"
            type="submit"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="login-footer">
          Secure HR management for better workplaces.
        </p>
      </div>
    </div>
  );
}
