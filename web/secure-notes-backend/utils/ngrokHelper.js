// scripts/ngrokHelper.js
const fs = require('fs');
const path = require('path');
const axios = require('axios');

// Try to get ngrok url from the API
async function detectNgrokUrl() {
  try {
    const response = await axios.get('http://localhost:4040/api/tunnels');
    const tunnels = response.data.tunnels;

    for (const tunnel of tunnels) {
      if (tunnel.proto === 'https') {
        return tunnel.public_url;
      }
    }

    return null;
  } catch (error) {
    console.error('Could not detect ngrok URL:', error.message);
    return null;
  }
}

// Main function to log and save the ngrok URL
async function logNgrokUrl() {
  try {
    const ngrokUrl = await detectNgrokUrl();

    if (ngrokUrl) {
      console.log('\n══════════════════════════════════════════════════');
      console.log('🔗 NGROK PUBLIC URL:');
      console.log(`   ${ngrokUrl}`);
      console.log('══════════════════════════════════════════════════\n');

      // Create a local file with the URL for reference
      const infoContent = `NGROK_URL=${ngrokUrl}\nLast updated: ${new Date().toISOString()}\n`;
      fs.writeFileSync(path.join(__dirname, '..', '.ngrok-url'), infoContent);

      return ngrokUrl;
    } else {
      console.log('\n⚠️ Could not detect ngrok URL. Is ngrok running?\n');
      return null;
    }
  } catch (error) {
    console.error('Error in ngrok helper:', error);
    return null;
  }
}

// If run directly
if (require.main === module) {
  logNgrokUrl();
}

module.exports = { detectNgrokUrl, logNgrokUrl };