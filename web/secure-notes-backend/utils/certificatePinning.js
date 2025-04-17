const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

/**
 * Generate SHA-256 hash of certificate to be used for certificate pinning
 * This hash can be provided to Android developers for SSL pinning implementation
 */
exports.generateCertificateHash = () => {
  try {
    // Read certificate file
    const certPath = path.join(__dirname, '..', 'certificates', 'certificate.pem');
    const certContent = fs.readFileSync(certPath, 'utf8');

    // Hash the certificate
    const hash = crypto
      .createHash('sha256')
      .update(certContent)
      .digest('base64');

    return hash;
  } catch (err) {
    console.error('Error generating certificate hash:', err);
    return null;
  }
};

/**
 * Get certificate information for public display
 * (helps Android developers implement SSL pinning)
 */
exports.getCertificateInfo = () => {
  try {
    const certPath = path.join(__dirname, '..', 'certificates', 'certificate.pem');
    const certContent = fs.readFileSync(certPath, 'utf8');

    // Generate different hash formats
    const sha256Hash = crypto
      .createHash('sha256')
      .update(certContent)
      .digest('base64');

    const sha1Hash = crypto
      .createHash('sha1')
      .update(certContent)
      .digest('hex');

    return {
      sha256: sha256Hash,
      sha1: sha1Hash,
      format: 'Use these hashes for certificate pinning in your Android app'
    };
  } catch (err) {
    console.error('Error getting certificate info:', err);
    return null;
  }
};

// Add a route to the server to expose this information
// Note: Add this to your server.js if you want to expose this info
/**
 * Example usage:
 *
 * // In server.js
 * const certUtils = require('./utils/certificatePinning');
 *
 * // Only in development mode!
 * if (process.env.NODE_ENV !== 'production') {
 *   app.get('/api/cert-info', (req, res) => {
 *     res.json(certUtils.getCertificateInfo());
 *   });
 * }
 */