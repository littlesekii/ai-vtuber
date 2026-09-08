const DEFAULT_AVATAR = "/shared/assets/default-avatar.svg";

const likesView = document.getElementById("likes-view");
const giftView = document.getElementById("gift-view");
const giftRankingView = document.getElementById("gift-ranking-view");

const likesRankingList = document.getElementById("likes-ranking-list");
const giftRankingList = document.getElementById("gift-ranking-list");

const giftImage = document.getElementById("gift-image");
const giftUserAvatar = document.getElementById("gift-user-avatar");
const giftUsername = document.getElementById("gift-username");
const giftDescription = document.getElementById("gift-description");

let hasGift = false;
let hasGiftRanking = false;

const LIKES_DURATION = 7000;
const GIFT_RANKING_DURATION = 7000;
const LAST_GIFT_DURATION = 7000;


function setImage(element, url) {
    element.onerror = () => {
        element.onerror = null;
        element.src = DEFAULT_AVATAR;
    };

    element.src = url || DEFAULT_AVATAR;
}


function formatNumber(value) {
    return Number(value || 0).toLocaleString("pt-BR");
}


function createRankingRow(viewer, index, scoreText) {
    const row = document.createElement("div");
    row.className = "ranking-item";

    const position = document.createElement("div");
    position.className = "ranking-position";
    position.textContent = `#${index + 1}`;

    const avatar = document.createElement("img");
    avatar.className = "ranking-avatar";
    avatar.alt = "";

    setImage(
        avatar,
        viewer.profileImageUrl
    );

    const name = document.createElement("div");
    name.className = "ranking-name";
    name.textContent = viewer.name || "Viewer";

    const score = document.createElement("div");
    score.className = "ranking-score";
    score.textContent = scoreText;

    row.append(
        position,
        avatar,
        name,
        score
    );

    return row;
}


function updateLikesRanking(viewers) {
    likesRankingList.innerHTML = "";

    if (!Array.isArray(viewers) || viewers.length === 0) {
        likesRankingList.innerHTML =
            '<div class="empty-state">Aguardando likes...</div>';
        return;
    }

    viewers
        .slice()
        .sort((a, b) => Number(b.amount || 0) - Number(a.amount || 0))
        .slice(0, 5)
        .forEach((viewer, index) => {
            likesRankingList.appendChild(
                createRankingRow(
                    viewer,
                    index,
                    `${formatNumber(viewer.amount)} ❤️‍🔥`
                )
            );
        });
}


function updateGiftRanking(viewers) {
    giftRankingList.innerHTML = "";

    if (!Array.isArray(viewers) || viewers.length === 0) {
        hasGiftRanking = false;

        giftRankingList.innerHTML =
            '<div class="empty-state">Aguardando presentes...</div>';
        return;
    }

    hasGiftRanking = true;

    viewers
        .slice()
        .sort((a, b) => Number(b.amount || 0) - Number(a.amount || 0))
        .slice(0, 5)
        .forEach((viewer, index) => {
            giftRankingList.appendChild(
                createRankingRow(
                    viewer,
                    index,
                    `${formatNumber(viewer.amount)} 🪙`
                )
            );
        });
}


function updateLastGift(data) {
    if (!data) {
        return;
    }

    hasGift = true;

    const giftName = data.giftName || "presente";
    const amount = Number(data.amount || 1);

    giftUsername.textContent = data.username || "Viewer";
    giftDescription.textContent = `${amount}x ${giftName}`;

    setImage(giftUserAvatar, data.profileImageUrl);
    setImage(giftImage, data.giftImageUrl);
}


function showView(view) {
    likesView.classList.toggle("active", view === likesView);
    giftView.classList.toggle("active", view === giftView);
    giftRankingView.classList.toggle("active", view === giftRankingView);
}


function scheduleRotation() {
    showView(likesView);

    setTimeout(() => {
        if (hasGift) {
            showView(giftView);

            setTimeout(() => {
                showGiftRankingOrRestart();
            }, LAST_GIFT_DURATION);

            return;
        }

        showGiftRankingOrRestart();

    }, LIKES_DURATION);
}


function showGiftRankingOrRestart() {
    // if (!hasGiftRanking) {
    //     scheduleRotation();
    //     return;
    // }

    showView(giftRankingView);

    setTimeout(() => {
        scheduleRotation();
    }, GIFT_RANKING_DURATION);
}


function createSocket() {
    const isLocal = true;
        // location.hostname === "127.0.0.1" ||
        // location.hostname === "localhost";

    const url = isLocal
        ? "ws://127.0.0.1:8096/ws"
        : `wss://${location.host}/ws`;

    return new WebSocket(url);
}


const socket = createSocket();

socket.onmessage = (event) => {
    try {
        const packet = JSON.parse(event.data);

        switch (packet.type) {
            case "RANKING":
            case "LIKE_RANKING":
                updateLikesRanking(packet.data);
                break;
                
            case "GIFT_RANKING":
            case "COIN_RANKING":
                updateGiftRanking(packet.data);
                break;

            case "LAST_GIFT":
            case "GIFT":
                updateLastGift(packet.data);
                break;

        }

    } catch (error) {
        console.error("[RANKING WS] Erro:", error);
    }
};


scheduleRotation();
