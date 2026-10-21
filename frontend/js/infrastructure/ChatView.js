export class ChatView {
    #messages;
    #input;
    #button;

    constructor(element, onSend) {
        this.#messages = element.querySelector(".chat-messages");
        this.#input = element.querySelector(".chat-input");
        this.#button = element.querySelector(".chat-form button");
        element.querySelector(".chat-form").addEventListener("submit", event => {
            event.preventDefault();
            const text = this.#input.value.trim();
            if (text === "") return;
            this.#input.value = "";
            onSend(text);
        });
    }

    render(match) {
        const enabled = match.mode === "online" && match.you !== null;
        this.#input.disabled = !enabled;
        this.#button.disabled = !enabled;
        this.#input.placeholder = match.mode === "online" ? "Escribir mensaje..." : "Chat solo en partidas online";
    }

    show(messages) {
        this.#messages.innerHTML = "";
        this.addSystem("¡Partida iniciada! Buena suerte.");
        messages.forEach(message => this.add(message));
    }

    add(message) {
        const line = document.createElement("div");
        const author = document.createElement("span");
        author.className = message.author === "White" ? "accent-gold" : "accent-pink";
        author.textContent = `[${message.authorName.toUpperCase()}]: `;
        line.append(author, message.text);
        this.#append(line);
    }

    addSystem(text) {
        const line = document.createElement("div");
        line.className = "muted";
        const author = document.createElement("span");
        author.className = "accent-gold";
        author.textContent = "[SISTEMA]: ";
        line.append(author, text);
        this.#append(line);
    }

    #append(line) {
        this.#messages.appendChild(line);
        this.#messages.scrollTop = this.#messages.scrollHeight;
    }
}
