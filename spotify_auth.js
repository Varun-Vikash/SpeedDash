// SPOTIFY AUTHENTICATION SERVICE
// ------------------------------------------------------

const SPOTIFY_CONFIG = {
    // Trimming ensures no copy-paste whitespace errors
    clientId: '852950465a424a9fb4f2cb105a5089a5'.trim(), 
    
    // Generates: "https://speeddash.vercel.app/" (Stripping hash/query)
    redirectUri: window.location.origin + window.location.pathname, 
    
    authEndpoint: 'https://accounts.spotify.com/authorize',
    scopes: [
        'user-read-currently-playing',
        'user-read-playback-state',
        'user-modify-playback-state'
    ]
};

const SpotifyAuth = {
    login() {
        // Construct URL manually to ensure parameter order and encoding
        const url = new URL(SPOTIFY_CONFIG.authEndpoint);
        url.searchParams.append('client_id', SPOTIFY_CONFIG.clientId);
        url.searchParams.append('redirect_uri', SPOTIFY_CONFIG.redirectUri);
        url.searchParams.append('scope', SPOTIFY_CONFIG.scopes.join(' '));
        url.searchParams.append('response_type', 'token'); // Implicit Grant
        url.searchParams.append('show_dialog', 'true');

        console.log("Redirecting to Spotify:", url.toString());
        window.location.href = url.toString();
    },

    getToken() {
        const hash = window.location.hash.substring(1);
        const params = new URLSearchParams(hash);
        const accessToken = params.get('access_token');
        const error = params.get('error');

        if (error) {
            console.error("Spotify Auth Error:", error);
            alert("Spotify Connection Failed: " + error);
            // Clean URL
            window.history.pushState("", document.title, window.location.pathname);
            return null;
        }
        
        if (accessToken) {
            console.log("Token Received:", accessToken.substring(0, 10) + "...");
            sessionStorage.setItem('spotify_token', accessToken);
            // Clean URL
            window.history.pushState("", document.title, window.location.pathname);
            return accessToken;
        }
        
        return sessionStorage.getItem('spotify_token');
    },

    isConnected() {
        return !!sessionStorage.getItem('spotify_token');
    },

    logout() {
        sessionStorage.removeItem('spotify_token');
        window.location.reload();
    }
};

// Expose to global window
window.SpotifyAuth = SpotifyAuth;
