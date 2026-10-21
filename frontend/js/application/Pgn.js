const DRAWS = ["Stalemate", "InsufficientMaterial", "FiftyMoveRule", "DrawByAgreement"];

export function toPgn(match, date = new Date()) {
    const result = resultOf(match.game);
    const headers = {
        Event: match.mode === "online" ? `Sala ${match.code}` : "Partida local",
        Site: "chess-software",
        Date: date.toISOString().slice(0, 10).replaceAll("-", "."),
        White: match.white.name ?? "?",
        Black: match.black.name ?? "?",
        Result: result
    };
    if (match.clock) headers.TimeControl = String(match.clock.initialMillis / 1000);
    const tags = Object.entries(headers).map(([key, value]) => `[${key} "${value.replaceAll("\"", "'")}"]`);
    const moves = [movesText(match.game.san), result].filter(part => part !== "").join(" ");
    return `${tags.join("\n")}\n\n${moves}\n`;
}

export function resultOf(game) {
    if (game.status === "InProgress") return "*";
    if (DRAWS.includes(game.status)) return "1/2-1/2";
    return game.winner === "White" ? "1-0" : "0-1";
}

function movesText(san) {
    const parts = [];
    for (let i = 0; i < san.length; i += 2) {
        parts.push(`${i / 2 + 1}. ${san[i]}${san[i + 1] ? " " + san[i + 1] : ""}`);
    }
    return parts.join(" ");
}
