const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const dotenv = require('dotenv');
const fs = require('fs');
const https = require('https');
const path = require('path');
const swaggerUI = require('swagger-ui-express');
const swaggerSpec = require('./swagger');
// Load environment variables
dotenv.config();

// Import routes
const authRoutes = require('./routes/auth');
const noteRoutes = require('./routes/notes');

// Initialize Express app
const app = express();



app.use('/api-docs', swaggerUI.serve, swaggerUI.setup(swaggerSpec));

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/notes', noteRoutes);

// Base route for testing
app.get('/', (req, res) => {
  res.send('Secure Notes API is running');
});

// Connect to MongoDB
mongoose
  .connect(process.env.MONGO_URI)
  .then(() => console.log('Connected to MongoDB Atlas'))
  .catch((err) => console.error('Could not connect to MongoDB', err));

// Start server
const PORT = process.env.PORT || 3000;

// Check if we should use HTTPS
if (process.env.NODE_ENV === 'production') {
  try {
    // SSL Certificate paths
    const options = {
      key: fs.readFileSync(path.join(__dirname, 'certificates', 'private-key.pem')),
      cert: fs.readFileSync(path.join(__dirname, 'certificates', 'certificate.pem'))
    };
    
    https.createServer(options, app).listen(PORT, () => {
      console.log(`HTTPS Server running on port ${PORT} in production mode`);
    });
  } catch (err) {
    console.error('Could not start HTTPS server:', err);
    console.log('Falling back to HTTP server...');
    app.listen(PORT, () => {
      console.log(`HTTP Server running on port ${PORT} in production mode (fallback)`);
    });
  }
} else {
  app.listen(PORT, () => {
    console.log(`HTTP Server running on port ${PORT} in development mode`);
  });
}