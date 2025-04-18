const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');
const fs = require('fs');
const https = require('https');
const path = require('path');
const swaggerUI = require('swagger-ui-express');
const swaggerSpec = require('./swagger');
const cookieParser = require('cookie-parser');
const helmet = require('helmet');
const os = require('os');

// Import middleware
const compression = require('./middleware/compression');
const securityHeaders = require('./middleware/security');
const { apiLimiter } = require('./middleware/rateLimit');
const ngrokCors = require('./middleware/ngrokCors');

// Load environment variables
dotenv.config();

// Import routes
const configRoutes = require('./routes/config');
const authRoutes = require('./routes/auth');
const noteRoutes = require('./routes/notes');

// Initialize Express app
const app = express();

// Apply custom CORS middleware for ngrok
app.use(ngrokCors);

// Security middleware
app.use(helmet({
  contentSecurityPolicy: process.env.NODE_ENV === 'development' ? false : undefined,
}));

// In development mode, we'll use less strict security headers
if (process.env.NODE_ENV !== 'development') {
  app.use(securityHeaders);
}

// Parse cookies
app.use(cookieParser());

// Apply GZIP compression to all responses
app.use(compression);

// Setup body parser
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: false, limit: '1mb' }));

// Rate limiting - less strict in development mode
if (process.env.NODE_ENV === 'development') {
  console.log('⚠️ Rate limiting disabled in development mode');
} else {
  app.use('/api/', apiLimiter);
}

// Swagger documentation
app.use('/api-docs', swaggerUI.serve, swaggerUI.setup(swaggerSpec));


function getLocalIP() {
    const networkInterfaces = os.networkInterfaces();
    for (const interfaceName in networkInterfaces) {
      for (const iface of networkInterfaces[interfaceName]) {
        // Skip over internal (i.e. 127.0.0.1) and non-IPv4 addresses
        if (iface.family === 'IPv4' && !iface.internal) {
          return iface.address;
        }
      }
    }
    return '127.0.0.1';
  }


app.get('/api/ip', (req, res) => {
    try {
      const ip = getLocalIP();
      res.send(ip);
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Failed', details: error.message });
    }
  });

// API Status endpoint
app.get('/api/status', (req, res) => {
  res.json({
    status: 'online',
    environment: process.env.NODE_ENV,
    timestamp: new Date().toISOString(),
    ngrokUrl: process.env.NGROK_URL || 'Not detected'
  });
});

// Routes
app.use('/api/config', configRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/notes', noteRoutes);

// Base route for testing
app.get('/', (req, res) => {
  res.send(`
    <html>
      <head>
        <title>Secure Notes API</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
          h1 { color: #333; }
          .container { max-width: 800px; margin: 0 auto; }
          .endpoints { background: #f4f4f4; padding: 20px; border-radius: 5px; }
          code { background: #e0e0e0; padding: 2px 5px; border-radius: 3px; }
        </style>
      </head>
      <body>
        <div class="container">
          <h1>Secure Notes API is running</h1>
          <p>Environment: ${process.env.NODE_ENV}</p>
          <p>Ngrok URL: ${process.env.NGROK_URL || 'Not detected yet'}</p>
          <div class="endpoints">
            <h2>Available Endpoints:</h2>
            <ul>
              <li><a href="/api-docs">API Documentation (Swagger)</a></li>
              <li><a href="/api/status">API Status</a></li>
            </ul>
          </div>
        </div>
      </body>
    </html>
  `);
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({
    status: 'error',
    message: process.env.NODE_ENV === 'production'
      ? 'Something went wrong'
      : err.message
  });
});

// Connect to MongoDB
mongoose
  .connect(process.env.MONGO_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 5000
  })
  .then(() => console.log('✅ Connected to MongoDB'))
  .catch((err) => console.error('❌ Could not connect to MongoDB', err));

// Start server
const PORT = process.env.PORT || 3000;

// Listen for connections
app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT} in ${process.env.NODE_ENV} mode`);
  console.log(`📚 API Documentation: http://localhost:${PORT}/api-docs`);

  if (process.env.NODE_ENV === 'development') {
    console.log('⚠️ Development mode: Security features relaxed for local development');
    console.log('🔗 Waiting for ngrok to start...');
  }
});

// Listen for ngrok subprocess output to detect URL
if (process.env.NODE_ENV === 'development') {
  process.stdout.on('data', (data) => {
    const output = data.toString();
    // Look for ngrok URL in the output
    const match = output.match(/https:\/\/[a-z0-9]+\.ngrok\.io/);
    if (match && match[0] && !process.env.NGROK_URL) {
      process.env.NGROK_URL = match[0];
      console.log(`🔗 Ngrok URL detected: ${process.env.NGROK_URL}`);
    }
  });
}

// Handle uncaught exceptions
process.on('unhandledRejection', (err) => {
  console.error('Unhandled Rejection:', err);
});