const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

/**
* Generate SHA-256 hash of certificate to be used for certificate pinning
* This hash can be provided to Android developers for SSL pinning implementation
*/
exports.generateCertificateHash = () => {
  try {
    // Read certificate file
    const certificatePath = path.join(__dirname, '..', 'certificates', 'certificate.pem');
    
    // Run the OpenSSL command and capture the output
    const command = `openssl x509 -in ${certificatePath} -fingerprint -sha256 | sed 's/SHA256 Fingerprint=//' | xxd -r -p | openssl base64`;
    const output = execSync(command, { encoding: 'utf8' }).trim();
    
    return "sha256/"+output;
  } catch (error) {
    console.error('Error executing OpenSSL command:', error.message);
    throw error;
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