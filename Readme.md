# Secure Notes API

- Backend for secure note-taking app.
- Built with Node.js, Express, MongoDB.

## Features

- User registration & login.
- Short-lived access tokens (5 mins).
- Refresh tokens (30 days) for continued access.
- Create, read, update, delete notes.
- Secure API with rate limiting.
- GZIP compression for faster responses.
- HTTPS recommended for production.
- Swagger API documentation (`/api-docs`).

## Setup

1. **Clone:** `git clone <repo_url>`
2. **Install:** `npm install` or `yarn install`
3. **`.env`:** Configure `MONGO_URI`, `JWT_SECRET`, etc.
4. **Run Dev:** `npm run dev`
5. **Run Prod:** `NODE_ENV=production npm start`

## API Endpoints

- `/api/auth`: Registration, login, refresh, logout, user info.
- `/api/notes`: Create, read, update, delete notes (requires auth).
- `/api/config`: API configuration.
- `/api/status`: API status.

### `/api/auth`

- **`POST /api/auth/register`**: Register new user (`{ email, password }` -> `{ accessToken, refreshToken, expiresIn }`).
- **`POST /api/auth/login`**: Login user (`{ email, password }` -> `{ accessToken, refreshToken, expiresIn }`, sets cookie).
- **`POST /api/auth/refresh`**: Get new access token (`{ refreshToken }` or cookie -> `{ accessToken, expiresIn }`).
- **`POST /api/auth/logout`**: Logout user (optional `{ refreshToken }` or cookie -> `{ msg }`, clears cookie).
- **`GET /api/auth/user`**: Get logged-in user (requires Bearer token -> `{ _id, email, createdAt }`).

### `/api/notes` (Requires Bearer Token)

- **`GET /api/notes`**: Get all user notes (`[]`).
- **`GET /api/notes/:id`**: Get specific note (`{ _id, title, content, ... }`).
- **`POST /api/notes`**: Create new note (`{ title, content }` -> `{ _id, title, content, ... }`).
- **`PUT /api/notes/:id`**: Update note (`{ title, content }` -> `{ _id, title, content, ... }`).
- **`DELETE /api/notes/:id`**: Delete note (`{ msg }`).

### `/api/config`

- **`GET /api/config`**: Get API configuration (`{ apiUrl, accessTokenTtl, refreshTokenTtl, ... }`).

### `/api/status`

- **`GET /api/status`**: Get API status (`{ status, environment, timestamp, ... }`).

## Security Features

- **Short-Lived Access Tokens:** Access tokens expire quickly (5 minutes) to limit damage if compromised.
- **Refresh Tokens:** Long-lived tokens (30 days) allow clients to get new access tokens without re-login.
- **HTTP-only Cookies:** Refresh tokens for web clients are in HTTP-only cookies for better protection.
- **Rate Limiting:** Prevents abuse with limits on API requests.
- **HTTPS:** Strongly recommended for production to encrypt all communication.
- **Considerations for MITM Prevention:**
    - **SSL/TLS:** Ensure proper server setup with a valid certificate.
    - **Certificate Pinning (Android):** Instructions and helper to generate certificate hashes for client-side pinning.
- **GZIP Compression:** While not directly a security feature, it reduces data transfer. For mobile (like Android with Retrofit), this saves bandwidth.
- **HTTP Security Headers:** Implemented to enhance security against common web vulnerabilities.

## Refresh Token API
- **Dedicated /api/auth/refresh Endpoint:** This endpoint is specifically designed to issue new access tokens in exchange for a valid, non-revoked, and unexpired refresh token.
- **Refresh Token Storage:** Refresh tokens are stored in a dedicated RefreshToken MongoDB collection, associated with the user and an expiration date (30 days).
- **Refresh Token Revocation:** The /api/auth/logout endpoint revokes the refresh token, preventing it from being used again.
- **HTTP-only Cookies (for Web Clients):** For web browsers, the refresh token is also set as an HTTP-only cookie. This helps protect it from client-side JavaScript access, mitigating the risk of XSS attacks. However, for non-web clients like Android, the refresh token is also sent in the response body.


## MITM Prevention and SSL/TLS
- **HTTPS Enforcement (Production):** In a production environment, it is crucial to deploy the API over HTTPS. This encrypts the communication between the client and the server, preventing eavesdropping and man-in-the-middle (MITM) attacks. You will need to obtain and configure a valid SSL/TLS certificate for your domain.
- **Certificate Pinning (Android):** To further enhance security against MITM attacks, especially those involving rogue or compromised Certificate Authorities, your Android application should implement SSL certificate pinning.
- **Backend Support:** The utils/certificatePinning.js file provides helper functions to generate SHA-256 and SHA-1 hashes of your SSL certificate.
- **Android Implementation:** You will need to hardcode or securely deliver these certificate hashes within your Android application. During the SSL handshake, the app will verify that the server's certificate matches one of the pinned hashes.
- **Development Endpoint (Optional):** The commented-out /api/cert-info route in utils/certificatePinning.js shows how you could expose certificate information (only in development) to aid Android developers during implementation. Do not expose this in production.

## Strong Retrofit (Android) Considerations

When integrating this API with an Android application using Retrofit, consider these security best practices:

- **Implement SSL Certificate Pinning:** Use the certificate hashes (SHA-256 or SHA-1) to verify the server's identity and prevent MITM attacks. The backend provides tools in `utils/certificatePinning.js` to generate these hashes.
- **Enable GZIP Compression:** Configure Retrofit to accept and decompress GZIP responses from the server to reduce bandwidth usage and improve performance.
- **Secure Token Storage:** Store access and refresh tokens securely on the Android device (e.g., using EncryptedSharedPreferences or the Android Keystore).
- **Implement Token Refresh Logic:** Handle access token expiry gracefully by using the refresh token endpoint to obtain new access tokens before they expire.
- **Error Handling:** Implement robust error handling for network requests and API responses, especially for authentication failures.

## Important Request Headers

- **`Authorization: Bearer <accessToken>`**: Used for authenticating requests to protected routes. Replace `<accessToken>` with the actual JWT access token.
- **`Content-Type: application/json`**: Indicates that the request body is in JSON format (required for `POST` and `PUT` requests with JSON data).
- **`Cookie`**: For web clients, this header will automatically include cookies set by the server, such as the `refreshToken`.
- **`x-forwarded-host`**: (Typically set by proxies) Indicates the original host requested by the client.
- **`x-forwarded-proto`**: (Typically set by proxies) Indicates the original protocol (HTTP/HTTPS) used by the client.
- **`x-no-compression: true`**: (Optional) Sent by the client to request an uncompressed response.

## Docs

- Access Swagger UI at `/api-docs` (dev mode).

## Task:

### STEP 1: BACKEND NODEJS

- Create a simple Express server with user authentication
- Set up CRUD operations for notes
- Implement proper TLS/SSL for secure connections
- Deploy locally and expose with ngrok for testing

### STEP 2: ANDROID

- Set up a basic notes app UI
- Implement Retrofit for API communication
- Configure proper SSL/TLS handling
- Add SSL pinning to prevent MITM attacks

### STEP 3: ENHANCE SECURITY FEATURES

- Implement token-based authentication
- Add different security configurations for debug vs release
- Configure network security config for cleartext traffic rules
- Handle certificate validation properly


## How to generate certificate hashes for Android SSL pinning

- `mkdir certificates`
- `cd certificates`

### Generate a private key

`openssl genrsa -out private-key.pem 2048`

### Generate a certificate signing request

`openssl req -new -key private-key.pem -out csr.pem`

### Generate a self-signed certificate (valid for 365 days)

`openssl x509 -req -days 365 -in csr.pem -signkey private-key.pem -out certificate.pem`


