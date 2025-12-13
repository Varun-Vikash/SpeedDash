// SPOTIFY REAL API INTEGRATION
// ------------------------------------------------------

const SPOTIFY_CONFIG = {
    clientId: '852950465a424a9fb4f2cb105a5089a5'.trim(), 
    // Uses the current page root as redirect. Ensure this EXACT URL is in Spotify Dashboard.
    redirectUri: window.location.origin + window.location.pathname, 
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
        const url = new URL(SPOTIFY_CONFIG.authEndpoint);
        url.searchParams.append('client_id', SPOTIFY_CONFIG.clientId);
        url.searchParams.append('redirect_uri', SPOTIFY_CONFIG.redirectUri);
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
            // Save to storage
            sessionStorage.setItem('spotify_token', accessToken);
            // Expire logic could go here, but keeping it simple
            window.history.pushState("", document.title, window.location.pathname); // Clean URL
            return accessToken;
        }
        
        // Fallback to storage
        return sessionStorage.getItem('spotify_token');
    },

    isConnected() {
        return !!this.getToken();
    },

    // 3. API Calls
    async fetchNowPlaying() {
        const token = this.getToken();
        if (!token) return null;

        try {
            const res = await fetch('https://api.spotify.com/v1/me/player/currently-playing', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (res.status === 204 || res.status > 400) return null; // No content or error

            const data = await res.json();
            return {
                name: data.item.name,
                artist: data.item.artists.map(a => a.name).join(', '),
                image: data.item.album.images[0]?.url,
                progress: data.progress_ms,
                duration: data.item.duration_ms,
                isPlaying: data.is_playing
            };
        } catch (e) {
            console.error("Spotify Fetch Error", e);
            return null;
        }
    },

    async next() {
        await this._post('next');
    },

    async previous() {
        await this._post('previous');
    },

    async playPause(currentState) {
        // currentState is boolean (true = playing)
        const endpoint = currentState ? 'pause' : 'play';
        await this._put(endpoint);
    },

    async _post(endpoint) {
        const token = this.getToken();
        if(!token) return;
        await fetch(`https://api.spotify.com/v1/me/player/${endpoint}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
    },

    async _put(endpoint) {
        const token = this.getToken();
        if(!token) return;
        await fetch(`https://api.spotify.com/v1/me/player/${endpoint}`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${token}` }
        });
    }
};

window.SpotifyAuth = SpotifyAuth;
