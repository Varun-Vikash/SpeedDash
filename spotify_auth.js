// SPOTIFY AUTHENTICATION SERVICE
// ------------------------------------------------------
// This file handles the OAuth 2.0 Implicit Grant flow.
// It redirects the user to Spotify to login, and handles the
// access token returned in the URL hash.

const SPOTIFY_CONFIG = {
    clientId: '852950465a424a9fb4f2cb105a5089a5', // Your Client ID
    redirectUri: window.location.href.split('#')[0], // Dynamically grabs current page URL (e.g., http://127.0.0.1:5500/index.html)
    authEndpoint: 'https://accounts.spotify.com/authorize',
    scopes: [
        'user-read-currently-playing',
        'user-read-playback-state',
        'user-modify-playback-state' // Needed for Play/Pause/Skip
    ]
};

const SpotifyAuth = {
    // 1. Call this when the user clicks the music widget to connect
    login() {
        const url = `${SPOTIFY_CONFIG.authEndpoint}?client_id=${SPOTIFY_CONFIG.clientId}&redirect_uri=${encodeURIComponent(SPOTIFY_CONFIG.redirectUri)}&scope=${encodeURIComponent(SPOTIFY_CONFIG.scopes.join(' '))}&response_type=token&show_dialog=true`;
        window.location.href = url;
    },

    // 2. Call this on page load to check if we just returned from Spotify
    getToken() {
        const hash = window.location.hash.substring(1);
        const params = new URLSearchParams(hash);
        const accessToken = params.get('access_token');
        
        if (accessToken) {
            // Store token in session storage so it persists on refresh
            sessionStorage.setItem('spotify_token', accessToken);
            // Clean the URL so the ugly token doesn't stay in the address bar
            window.history.pushState("", document.title, window.location.pathname + window.location.search);
            return accessToken;
        }
        
        return sessionStorage.getItem('spotify_token');
    },

    // 3. Helper to check if we are connected
    isConnected() {
        return !!this.getToken();
    },

    // 4. Logout (Clear token)
    logout() {
        sessionStorage.removeItem('spotify_token');
        window.location.reload();
    }
};

// Expose to global window scope so index.html can access it
window.SpotifyAuth = SpotifyAuth;
