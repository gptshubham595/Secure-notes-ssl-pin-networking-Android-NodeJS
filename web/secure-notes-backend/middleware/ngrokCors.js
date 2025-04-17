// middleware/ngrokCors.js
const cors = require('cors');

module.exports = (req, res, next) => {
  // Get the ngrok URL from the request headers or environment variables
  const ngrokUrl = req.headers['x-forwarded-host']
    ? `https://${req.headers['x-forwarded-host']}`
    : null;

  // Store the ngrok URL for logging/debugging
  if (ngrokUrl && !process.env.NGROK_URL) {
    console.log(`Detected ngrok URL: ${ngrokUrl}`);
    process.env.NGROK_URL = ngrokUrl;
  }

  // Create a dynamic CORS configuration based on environment
  const corsOptions = {
    origin: (origin, callback) => {
      // In development, allow any origin including ngrok URLs
      if (process.env.NODE_ENV !== 'production') {
        callback(null, true);
      } else {
        // In production, use the configured allowed origins
        const allowedOrigins = process.env.ALLOWED_ORIGINS?.split(',') || [];
        if (!origin || allowedOrigins.indexOf(origin) !== -1) {
          callback(null, true);
        } else {
          callback(new Error('Not allowed by CORS'));
        }
      }
    },
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'x-auth-token'],
    credentials: true,
    maxAge: 86400 // 24 hours
  };

  // Apply the dynamic CORS configuration
  cors(corsOptions)(req, res, next);
};