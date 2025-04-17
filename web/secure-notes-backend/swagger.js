const swaggerJSDoc = require('swagger-jsdoc');
require('dotenv').config(); // Load environment variables

const swaggerDefinition = {
  openapi: '3.0.0',
  info: {
    title: 'Secure Notes API',
    version: '1.0.0',
    description: 'API for secure notes application',
  },
  servers: [], // Initialize an empty servers array
  components: {
    securitySchemes: {
      ApiKeyAuth: {
        type: 'apiKey',
        in: 'header',
        name: 'x-auth-token',
        description: 'JWT token in x-auth-token header'
      }
    },
    security: [{
      ApiKeyAuth: []
    }]
  }
};

// Dynamically add server based on NODE_ENV
if (process.env.NODE_ENV === 'production') {
  swaggerDefinition.servers.push({
    url: process.env.PRODUCTION_URL || 'https://your-production-domain.com/api', // Replace with your actual production URL
    description: 'Production server',
  });
} else {
  swaggerDefinition.servers.push({
    url: `http://localhost:${process.env.PORT || 3000}`,
    description: 'Development server',
  });
}

const options = {
  swaggerDefinition,
  apis: ['./routes/*.js'], // Path to the API routes in your Node.js application
};

const swaggerSpec = swaggerJSDoc(options);

module.exports = swaggerSpec;