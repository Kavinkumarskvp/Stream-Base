const params = new URLSearchParams(window.location.search);
const videoId = params.get('v') || '1';
document.getElementById('video-id-label').textContent = videoId;

let ws = null;
let currentUserId = null;
let heartbeatTimer = null;

// ─── Join ────────────────────────────────────────────────
function join() {
    const nameInput = document.getElementById('name-input');
    const name = nameInput.value.trim();

    if (!name || name.length < 2) {
        alert('Name must be at least 2 characters.');
        return;
    }
    if (/\s/.test(name)) {
        alert('Name cannot contain spaces.');
        return;
    }

    currentUserId = name;

    // Lock the name — cannot change after joining
    nameInput.disabled = true;
    document.getElementById('join-btn').disabled = true;
    document.getElementById('name-display').textContent = '👤 ' + name;
    document.getElementById('name-display').style.display = 'inline';

    connect();
}

// ─── WebSocket connect ───────────────────────────────────
function connect() {
    ws = new WebSocket(`ws://localhost/ws/comments/${videoId}`);

    setStatus('connecting...', '#999');

    ws.onopen = () => {
        setStatus('🟢 connected', 'green');
        document.getElementById('input-row').classList.add('visible');
        document.getElementById('reconnect-btn').style.display = 'none';

        // Immediately tell server who joined
        ws.send(JSON.stringify({ type: 'join', userId: currentUserId }));

        // Heartbeat every 10s to stay in viewer count
        heartbeatTimer = setInterval(() => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({ type: 'heartbeat', userId: currentUserId }));
            }
        }, 10000);
    };

    ws.onclose = () => {
        setStatus('🔴 disconnected', 'red');
        document.getElementById('input-row').classList.remove('visible');
        document.getElementById('reconnect-btn').style.display = 'inline';
        clearInterval(heartbeatTimer);
    };

    ws.onerror = () => {
        setStatus('⚠️ error', 'orange');
    };

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);

        if (msg.type === 'viewer_count') {
            document.getElementById('viewer-count').textContent = msg.count;
            return;
        }

        if (msg.type === 'user_event') {
            appendUserEvent(msg);
            return;
        }

        // Default: comment
        appendComment(msg);
    };
}

// ─── Reconnect ───────────────────────────────────────────
function reconnect() {
    if (currentUserId) connect();
}

// ─── Messages ────────────────────────────────────────────
function appendComment(comment) {
    const div = document.createElement('div');
    div.className = 'comment';
    div.innerHTML = `
        <span class="username">${comment.userId}</span>:
        ${comment.text}
        <span class="time">${formatTime(comment.timestamp)}</span>
    `;
    appendToFeed(div);
}

function appendUserEvent(msg) {
    const div = document.createElement('div');
    div.className = 'user-event';
    const label = msg.userId === currentUserId ? 'You' : msg.userId;
    div.textContent = `${label} ${msg.event}`;  // e.g. "alice joined"
    appendToFeed(div);
}

function appendToFeed(element) {
    const feed = document.getElementById('comment-feed');
    feed.appendChild(element);
    feed.scrollTop = feed.scrollHeight;
}

function formatTime(timestamp) {
    if (!timestamp) return '';
    return new Date(timestamp).toLocaleTimeString();
}

// ─── Send comment ────────────────────────────────────────
function sendComment() {
    const text = document.getElementById('message-input').value.trim();
    if (!text || !currentUserId) return;
    if (ws.readyState !== WebSocket.OPEN) {
        alert('Not connected. Please reconnect.');
        return;
    }
    ws.send(JSON.stringify({ type: 'comment', userId: currentUserId, text }));
    document.getElementById('message-input').value = '';
}

document.getElementById('message-input')
    .addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendComment();
    });

// ─── Helpers ─────────────────────────────────────────────
function setStatus(text, color) {
    const el = document.getElementById('status');
    el.textContent = text;
    el.style.color = color;
}