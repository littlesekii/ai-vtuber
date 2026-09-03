const DEFAULT_AVATAR = "/shared/assets/default-avatar.svg";

const elements = {
    messageCard: document.getElementById("message-card"),
    viewerAvatar: document.getElementById("viewer-avatar"),
    viewerName: document.getElementById("viewer-name"),
    viewerMessage: document.getElementById("viewer-message"),
    viewerPriority: document.getElementById("viewer-priority")
};

function setImage(element, url) {
    element.onerror = () => {
        element.onerror = null;
        element.src = DEFAULT_AVATAR;
    };

    element.src = url || DEFAULT_AVATAR;
}

function showMessage(data) {
    elements.viewerName.textContent =
        data.username || "Viewer";

    elements.viewerMessage.textContent =
        data.message || "";

    setImage(
        elements.viewerAvatar,
        data.profileImageUrl
    );

    const priority = Number(
        data.priority || 0
    );

    if (priority > 0) {
        elements.viewerPriority.textContent =
            "PRIORIDADE " + priority;

        elements.viewerPriority.hidden = false;
    } else {
        elements.viewerPriority.hidden = true;
    }

    elements.messageCard.classList.remove(
        "message-in"
    );

    void elements.messageCard.offsetWidth;

    elements.messageCard.classList.add(
        "message-in"
    );
}

function createOverlaySocket() {
    const isLocal = true;
        // location.hostname === "127.0.0.1" ||
        // location.hostname === "localhost";

    const url = isLocal
        ? "ws://127.0.0.1:8096/ws"
        : `wss://${location.host}/ws`;

    console.log(
        "[MESSAGE WS] Conectando em:",
        url
    );

    return new WebSocket(url);
}

window.MioOverlay = {
    showMessage
};

const socket = createOverlaySocket();

socket.onopen = () => {
    console.log("[MESSAGE WS] CONECTADO");
};

socket.onmessage = (event) => {
    try {
        const packet =
            JSON.parse(event.data);

        if (packet.type === "MESSAGE") {
            MioOverlay.showMessage(
                packet.data
            );
        }

    } catch (error) {
        console.error(
            "[MESSAGE WS] Erro:",
            error
        );
    }
};

socket.onerror = (error) => {
    console.error(
        "[MESSAGE WS] ERRO:",
        error
    );
};

socket.onclose = (event) => {
    console.log(
        "[MESSAGE WS] FECHADO:",
        event.code,
        event.reason
    );
};
