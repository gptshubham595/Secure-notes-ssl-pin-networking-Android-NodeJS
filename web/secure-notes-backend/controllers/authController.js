// controllers/authController.js
const User = require('../models/User');
const RefreshToken = require('../models/RefreshToken');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');

// Helper functions for token generation
const generateAccessToken = (userId) => {
  const payload = {
    user: {
      id: userId
    }
  };

  // Generate JWT that expires in 5 minutes
  return jwt.sign(
    payload,
    process.env.JWT_SECRET,
    { expiresIn: '5m' } // Short-lived token (5 minutes)
  );
};

const generateRefreshToken = async (userId) => {
  // Generate random token
  const refreshToken = crypto.randomBytes(40).toString('hex');

  // Set expiry time (30 days)
  const expiresAt = new Date();
  expiresAt.setDate(expiresAt.getDate() + 30);

  // Save refresh token to database
  await RefreshToken.create({
    token: refreshToken,
    user: userId,
    expiresAt
  });

  return refreshToken;
};

// Register a new user
exports.register = async (req, res) => {
  const { email, password } = req.body;

  try {
    // Check if user already exists
    let user = await User.findOne({ email });
    if (user) {
      return res.status(400).json({ msg: 'User already exists' });
    }

    // Create new user
    user = new User({
      email,
      password
    });

    await user.save();

    // Generate tokens
    const accessToken = generateAccessToken(user.id);
    const refreshToken = await generateRefreshToken(user.id);

    // Set refresh token in HTTP-only cookie for added security
    res.cookie('refreshToken', refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
      sameSite: 'strict'
    });

    res.json({
      accessToken,
      refreshToken, // Also sending in response body for non-web clients like Android
      expiresIn: 300 // 5 minutes in seconds
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ msg: 'Server error', error: err.message });
  }
};

// Login user
exports.login = async (req, res) => {
  const { email, password } = req.body;

  try {
    // Check if user exists
    let user = await User.findOne({ email });
    if (!user) {
      return res.status(400).json({ msg: 'Invalid credentials' });
    }

    // Validate password
    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(400).json({ msg: 'Invalid credentials' });
    }

    // Generate tokens
    const accessToken = generateAccessToken(user.id);
    const refreshToken = await generateRefreshToken(user.id);

    // Set refresh token in HTTP-only cookie for added security
    res.cookie('refreshToken', refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      maxAge: 30 * 24 * 60 * 60 * 1000, // 30 days
      sameSite: 'strict'
    });

    res.json({
      accessToken,
      refreshToken, // Also sending in response body for non-web clients like Android
      expiresIn: 300 // 5 minutes in seconds
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ msg: 'Server error', error: err.message });
  }
};

// Refresh access token
exports.refreshToken = async (req, res) => {
  try {
    // Get refresh token from request body or cookie
    const refreshToken = req.body.refreshToken || req.cookies.refreshToken;

    if (!refreshToken) {
      return res.status(401).json({ msg: 'Refresh token not provided' });
    }

    // Find the refresh token in the database
    const storedToken = await RefreshToken.findOne({
      token: refreshToken,
      revoked: false,
      expiresAt: { $gt: new Date() }
    });

    if (!storedToken) {
      return res.status(401).json({ msg: 'Invalid or expired refresh token' });
    }

    // Generate new access token
    const accessToken = generateAccessToken(storedToken.user);

    // Return new access token
    res.json({
      accessToken,
      expiresIn: 300 // 5 minutes in seconds
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ msg: 'Server error', error: err.message });
  }
};

// Logout user
exports.logout = async (req, res) => {
  try {
    // Get refresh token from request body or cookie
    const refreshToken = req.body.refreshToken || req.cookies.refreshToken;

    if (refreshToken) {
      // Revoke the refresh token
      await RefreshToken.findOneAndUpdate(
        { token: refreshToken },
        { revoked: true }
      );
    }

    // Clear the cookie
    res.clearCookie('refreshToken');

    res.json({ msg: 'Logged out successfully' });
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ msg: 'Server error', error: err.message });
  }
};

// Get logged in user
exports.getUser = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-password');
    res.json(user);
  } catch (err) {
    console.error(err.message);
    res.status(500).json({ msg: 'Server error', error: err.message });
  }
};