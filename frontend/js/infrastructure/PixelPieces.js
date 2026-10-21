const SPRITES = {
    p: [
        "................",
        "......oooo......",
        ".....ohhffo.....",
        "....ohffffso....",
        "....offffffo....",
        "....offfffso....",
        ".....offffo.....",
        "......oooo......",
        ".....offffo.....",
        "......offo......",
        ".....offfso.....",
        "....offfffso....",
        "...offffffffo...",
        "..oooooooooooo..",
        "..ohffffffffso..",
        "..oooooooooooo.."
    ],
    r: [
        "................",
        "..ooo.oooo.ooo..",
        "..ofoooffoooso..",
        "..ohffffffffso..",
        "..oooooooooooo..",
        "...ohffffffso...",
        "...ohffffffso...",
        "...ohffffffso...",
        "...ohffffffso...",
        "...ohffffffso...",
        "..oooooooooooo..",
        "..ohffffffffso..",
        ".oooooooooooooo.",
        ".ohffffffffffso.",
        ".ohffffffffffso.",
        ".oooooooooooooo."
    ],
    n: [
        "................",
        "......oo.oo.....",
        ".....ohfoofo....",
        "....ohffffffo...",
        "...ohfofffffso..",
        "..ohffffffffso..",
        ".ohfffffffffso..",
        ".offfoooffffso..",
        "..ooo..offffso..",
        "......offfffso..",
        ".....offffffso..",
        "....offffffffo..",
        "..oooooooooooo..",
        ".oooooooooooooo.",
        ".ohffffffffffso.",
        ".oooooooooooooo."
    ],
    b: [
        "................",
        ".......oo.......",
        "......ohso......",
        ".......oo.......",
        "......oooo......",
        ".....ohffso.....",
        "....ohffoffo....",
        "...ohffoffffo...",
        "...ohfoffffso...",
        "...ohffffffso...",
        "....offffffo....",
        ".....oooooo.....",
        "...offffffffo...",
        ".oooooooooooooo.",
        ".ohffffffffffso.",
        ".oooooooooooooo."
    ],
    q: [
        "................",
        "..o....oo....o..",
        ".ofo..offo..ofo.",
        ".oho..ohfo..oho.",
        ".ohfo.ohfo.offo.",
        ".ohffoohffoofso.",
        ".ohffffffffffso.",
        "..ohffffffffso..",
        "...ohfffffso....",
        "....oooooooo....",
        ".....offfso.....",
        "....offffffo....",
        "...offffffffo...",
        ".oooooooooooooo.",
        ".ohffffffffffso.",
        ".oooooooooooooo."
    ],
    k: [
        "......oooo......",
        "....ooohfooo....",
        "....ohffffso....",
        "....ooohsooo....",
        "..oooo.oo.oooo..",
        ".ohffooffooffso.",
        ".ohffffffffffso.",
        ".ohffffffffffso.",
        "..ohffffffffso..",
        "...oooooooooo...",
        "....ohffffso....",
        "...ohffffffso...",
        "..ohffffffffso..",
        ".oooooooooooooo.",
        ".ohffffffffffso.",
        ".oooooooooooooo."
    ]
};

const PALETTES = {
    white: { o: "#1b1b24", f: "#f4e9d4", h: "#ffffff", s: "#c9b79c" },
    black: { o: "#0b0b10", f: "#3a3a4a", h: "#5d5d73", s: "#23232e" }
};

const cache = new Map();

export function pieceSvg(symbol, className = "piece") {
    if (!cache.has(symbol)) cache.set(symbol, buildSvg(symbol));
    return `<svg class="${className}" viewBox="0 0 16 16" shape-rendering="crispEdges" aria-hidden="true">${cache.get(symbol)}</svg>`;
}

function buildSvg(symbol) {
    const palette = symbol === symbol.toUpperCase() ? PALETTES.white : PALETTES.black;
    const rows = SPRITES[symbol.toLowerCase()];
    const paths = { o: "", f: "", h: "", s: "" };
    rows.forEach((row, y) => {
        [...row].forEach((pixel, x) => {
            if (pixel !== ".") paths[pixel] += `M${x} ${y}h1v1h-1z`;
        });
    });
    return Object.entries(paths)
        .map(([pixel, d]) => `<path fill="${palette[pixel]}" d="${d}"/>`)
        .join("");
}
