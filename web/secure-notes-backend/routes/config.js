// routes/config.js
const express = require('express');
const router = express.Router();

/**
 * @swagger
 * /api/config:
 *   get:
 *     summary: Get API configuration
 *     tags: [Config]
 *     responses:
 *       200:
 *         description: API configuration
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 apiUrl:
 *                   type: string
 *                   description: The base URL for API requests
 *                 accessTokenTtl:
 *                   type: integer
 *                   description: Access token time-to-live in seconds
 *                 version:
 *                   type: string
 *                   description: API version
 */
router.get('/', (req, res) => {
  // Get the base URL from request or environment
  const protocol = req.headers['x-forwarded-proto'] || req.protocol;
  const host = req.headers['x-forwarded-host'] || req.headers.host;
  const baseUrl = `${protocol}://${host}`;

  // Return configuration
  res.json({
    apiUrl: baseUrl,
    accessTokenTtl: 300, // 5 minutes in seconds
    refreshTokenTtl: 2592000, // 30 days in seconds
    version: '1.0.0',
    builtAt: new Date().toISOString()
  });
});

module.exports = router;