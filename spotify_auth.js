const SpotifyAuth = {
    async login() {
        // Generate code verifier
        const verifier = this.generateCodeVerifier();
        sessionStorage.setItem('code_verifier', verifier);
        
        // Generate code challenge
        const challenge = await this.generateCodeChallenge(verifier);
        
        const url = new URL(SPOTIFY_CONFIG.authEndpoint);
        url.searchParams.append('client_id', SPOTIFY_CONFIG.clientId);
        url.searchParams.append('redirect_uri', SPOTIFY_CONFIG.redirectUri);
        url.searchParams.append('scope', SPOTIFY_CONFIG.scopes.join(' '));
        url.searchParams.append('response_type', 'code'); // Not 'token'
        url.searchParams.append('code_challenge_method', 'S256');
        url.searchParams.append('code_challenge', challenge);
        
        window.location.href = url.toString();
    },
    
    generateCodeVerifier() {
        const array = new Uint8Array(32);
        crypto.getRandomValues(array);
        return btoa(String.fromCharCode(...array))
            .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    },
    
    async generateCodeChallenge(verifier) {
        const data = new TextEncoder().encode(verifier);
        const hash = await crypto.subtle.digest('SHA-256', data);
        return btoa(String.fromCharCode(...new Uint8Array(hash)))
            .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }
};
