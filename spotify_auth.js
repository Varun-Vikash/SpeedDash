// SPOTIFY REAL API INTEGRATION
// ------------------------------------------------------

const SPOTIFY_CONFIG = {
    clientId: '852950465a424a9fb4f2cb105a5089a5'.trim(), 
    // Uses the current page URL as redirect. Ensure this EXACT URL is in Spotify Dashboard.
    // Using href without hash/search to ensure consistent redirect URL
    get redirectUri() {
        const url = new URL(window.location.href);
        // Remove hash and search params to get clean URL
        url.hash = '';
        url.search = '';
        return url.toString();
    },
    authEndpoint: 'https://accounts.spotify.com/authorize',
    scopes: [
        'user-read-currently-playing',
        'user-read-playback-state',
        'user-modify-playback-state'
    ]
};

const SpotifyAuth = {
    // 1. Login Redirect
    login() {
        const redirectUri = SPOTIFY_CONFIG.redirectUri;
        console.log("Spotify Login - Redirect URI:", redirectUri);
        
        const url = new URL(SPOTIFY_CONFIG.authEndpoint);
        url.searchParams.append('client_id', SPOTIFY_CONFIG.clientId);
        url.searchParams.append('redirect_uri', redirectUri);
        url.searchParams.append('scope', SPOTIFY_CONFIG.scopes.join(' '));
        url.searchParams.append('response_type', 'token'); 
        url.searchParams.append('show_dialog', 'false');

        console.log("Redirecting to Spotify Auth:", url.toString());
        window.location.href = url.toString();
    },

    // 2. Parse Token from URL
    getToken() {
        // Check URL Hash first
        const hash = window.location.hash.substring(1);
        const params = new URLSearchParams(hash);
        const accessToken = params.get('access_token');
        
        if (accessToken) {
            console.log("Spotify token found in URL, saving to session storage");
            // Save to storage
            sessionStorage.setItem('spotify_token', accessToken);
            const expiresIn = params.get('expires_in');
            if (expiresIn) {
                const expiresAt = Date.now() + (parseInt(expiresIn) * 1000);
                sessionStorage.setItem('spotify_token_expires', expiresAt.toString());
            }
            // Clean URL
            window.history.replaceState(null, '', window.location.pathname);
            return accessToken;
        }
        
        // Fallback to storage (check if not expired)
        const storedToken = sessionStorage.getItem('spotify_token');
        if (storedToken) {
            const expiresAt = sessionStorage.getItem('spotify_token_expires');
            if (expiresAt && Date.now() > parseInt(expiresAt)) {
                console.log("Spotify token expired, clearing storage");
                sessionStorage.removeItem('spotify_token');
                sessionStorage.removeItem('spotify_token_expires');
                return null;
            }
            return storedToken;
        }
        
        return null;
    },

    isConnected() {
        return !!this.getToken();
    },

    // Logout / Clear Token
    logout() {
        sessionStorage.removeItem('spotify_token');
        sessionStorage.removeItem('spotify_token_expires');
        console.log("Spotify token cleared");
    },

    // 3. API Calls
    async fetchNowPlaying() {
        const token = this.getToken();
        if (!token) return null;

        try {
            const res = await fetch('https://api.spotify.com/v1/me/player/currently-playing', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (res.status === 204) {
                // No content - nothing playing
                return null;
            }
            
            if (res.status === 401) {
                // Token expired or invalid
                console.log("Spotify token invalid, clearing...");
                this.logout();
                return null;
            }
            
            if (res.status > 400) {
                console.error("Spotify API error:", res.status, res.statusText);
                return null;
            }

            const data = await res.json();
            // Return the full API response structure that updateSpotifyUI expects
            return data;
        } catch (e) {
            console.error("Spotify Fetch Error", e);
            return null;
        }
    },

    async next() {
        try {
            await this._post('next');
        } catch (e) {
            console.error("Spotify next error:", e);
        }
    },

    async previous() {
        try {
            await this._post('previous');
        } catch (e) {
            console.error("Spotify previous error:", e);
        }
    },

    async playPause(currentState) {
        try {
            // currentState is boolean (true = playing)
            const endpoint = currentState ? 'pause' : 'play';
            await this._put(endpoint);
        } catch (e) {
            console.error("Spotify playPause error:", e);
        }
    },

    async _post(endpoint) {
        const token = this.getToken();
        if(!token) return;
        const res = await fetch(`https://api.spotify.com/v1/me/player/${endpoint}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.status === 401) {
            this.logout();
        }
        return res;
    },

    async _put(endpoint) {
        const token = this.getToken();
        if(!token) return;
        const res = await fetch(`https://api.spotify.com/v1/me/player/${endpoint}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.status === 401) {
            this.logout();
        }
        return res;
    }
};

window.SpotifyAuth = SpotifyAuth;
