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
const certUtils = require('./utils/certificatePinning');
const { logNgrokUrl } = require('./utils/ngrokHelper');
const { detectNgrokUrl } = require('./utils/ngrokHelper');
const { getLocalIP } = require('./utils/ngrokHelper');

// Import middleware
const gzipCompression = require('./middleware/gzipCompression');
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
app.use(gzipCompression);

// Setup body parser
app.use(express.json({ limit: '1mb' }));
app.use(express.urlencoded({ extended: false, limit: '1mb' }));

// Rate limiting - less strict in development mode
if (process.env.NODE_ENV === 'development') {
    console.log('⚠️ Rate limiting disabled in development mode');
} else {
    app.use('/api/', apiLimiter);
}

app.set('trust proxy', true);

// Swagger documentation
app.use('/api-docs', swaggerUI.serve, swaggerUI.setup(swaggerSpec));


app.get('/api/ip', (req, res) => {
    try {
        const ip = getLocalIP();
        res.send(ip);
    } catch (error) {
        console.error('Error:', error);
        res.status(500).json({ error: 'Failed', details: error.message });
    }
});

app.get('/api/getUrl', (req, res) => {
    if (process.env.NODE_ENV === 'development') {
        detectNgrokUrl()
        .then(ngrokUrl => {
            if (ngrokUrl) {
                res.json({ ngrokUrl });
            } else {
                res.status(404).json({ error: 'Ngrok URL not found. Is ngrok running and accessible?' });
            }
        })
        .catch(error => {
            console.error('Error fetching ngrok URL:', error);
            res.status(500).json({ error: 'Failed to retrieve ngrok URL.' });
        });
    } else {
        res.status(400).json({ error: 'This endpoint is only available in development mode.' });
    }
});


// Only in development mode!
if (process.env.NODE_ENV !== 'production') {
    app.get('/api/cert-info', (req, res) => {
        res.json(certUtils.getCertificateInfo());
    });
}

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
    detectNgrokUrl()
    .then(ngrokUrl => {
        const ngrokInfo = ngrokUrl || 'Not detected yet'; // use the value from detectNgrokUrl
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
                  <p>Ngrok URL: ${ngrokInfo}</p>
                  <div class="endpoints">
                    <h2>Available Endpoints:</h2>
                    <ul>
                      <li><a href="/api-docs">API Documentation (Swagger)</a></li>
                      <li><a href="/api/status">API Status</a></li>
                      <li><a href="/api/getUrl">Get Ngrok URL</a></li>
                    </ul>
                  </div>
                </div>
              </body>
            </html>
          `);
        })
        .catch(error => {
            console.error('Error fetching ngrok URL for base route:', error);
            const ngrokInfo = 'Not detected yet';
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
                  <p>Ngrok URL: ${ngrokInfo}</p>
                  <div class="endpoints">
                    <h2>Available Endpoints:</h2>
                    <ul>
                      <li><a href="/api-docs">API Documentation (Swagger)</a></li>
                      <li><a href="/api/status">API Status</a></li>
                      <li><a href="/api/getUrl">Get Ngrok URL</a></li>
                    </ul>
                  </div>
                </div>
              </body>
            </html>
          `);
            });
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
            
            // if (process.env.NODE_ENV === 'development') {
                console.log('⚠️ Development mode: Security features relaxed for local development');
                console.log('🔗 Waiting for ngrok to start...');
                setTimeout(logNgrokUrl, 5000);
            // }
        });
        
        // Handle uncaught exceptions
        process.on('unhandledRejection', (err) => {
            console.error('Unhandled Rejection:', err);
        });