// middleware/compression.js
const compression = require('compression');

// Configure compression middleware with high level
module.exports = compression({
  level: 6, // Default is 6, max is 9
  threshold: 0, // Compress all responses
  filter: (req, res) => {
    // Don't compress responses with this request header
    if (req.headers['x-no-compression']) {
      return false;
    }
    // Use compression filter function from the module
    return compression.filter(req, res);
  }
});
