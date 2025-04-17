const rateLimit = require('express-rate-limit');

// Rate limit for API endpoints
exports.apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // Limit each IP to 100 requests per windowMs
  standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
  legacyHeaders: false, // Disable the `X-RateLimit-*` headers
  message: {
    status: 429,
    msg: 'Too many requests, please try again later.'
  }
});

// More aggressive rate limiting for auth routes to prevent brute force
exports.authLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 10, // Limit each IP to 10
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    status: 429,
    msg: 'Too many authentication attempts, please try again later.'
  }
});