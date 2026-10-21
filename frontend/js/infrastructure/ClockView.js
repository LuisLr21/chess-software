const TICK_MS = 200;
const LOW_TIME_MS = 20000;

export class ClockView {
    #element;
    #onTimeUp;
    #clock = null;
    #receivedAt = 0;
    #timer = null;
    #notified = false;

    constructor(element, onTimeUp) {
        this.#element = element;
        this.#onTimeUp = onTimeUp;
    }

    render(clock) {
        if (clock === this.#clock) return;
        this.stop();
        this.#clock = clock;
        this.#receivedAt = performance.now();
        this.#notified = false;
        if (clock?.running) this.#timer = setInterval(() => this.#draw(), TICK_MS);
        this.#draw();
    }

    stop() {
        clearInterval(this.#timer);
        this.#timer = null;
        this.#clock = null;
        this.#draw();
    }

    #draw() {
        const clock = this.#clock;
        for (const color of ["White", "Black"]) {
            const box = this.#element.querySelector(`.clock[data-color="${color}"]`);
            const millis = clock ? this.#remaining(color) : 0;
            box.querySelector(".clock-time").textContent = clock ? format(millis) : "--:--";
            box.classList.toggle("active", clock?.running === color);
            box.classList.toggle("low", clock !== null && millis < LOW_TIME_MS);
        }
        if (clock?.running && this.#remaining(clock.running) === 0 && !this.#notified) {
            this.#notified = true;
            this.#onTimeUp();
        }
    }

    #remaining(color) {
        const base = color === "White" ? this.#clock.whiteMillis : this.#clock.blackMillis;
        const elapsed = this.#clock.running === color ? performance.now() - this.#receivedAt : 0;
        return Math.max(0, base - elapsed);
    }
}

function format(millis) {
    const seconds = Math.ceil(millis / 1000);
    return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
}
