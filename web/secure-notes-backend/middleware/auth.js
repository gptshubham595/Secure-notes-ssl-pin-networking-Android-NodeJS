const jwt = require('jsonwebtoken');

module.exports = function(req, res, next) {
  // Get token from header
  const authHeader = req.header('Authorization');
  const token = authHeader ? authHeader.replace('Bearer ', '') : null;

  // Check if no token
  if (!token) {
    return res.status(401).json({ msg: 'Access denied, no token provided' });
  }

  // Verify token
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded.user;
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({
        msg: 'Token expired',
        tokenExpired: true
      });
    }

    res.status(401).json({ msg: 'Invalid token' });
  }
};