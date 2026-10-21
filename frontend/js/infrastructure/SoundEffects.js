const NOTE_GAP = 0.02;
const VOLUME = 0.05;

export class SoundEffects {
    #context = null;
    #muted;

    constructor(muted) {
        this.#muted = muted;
    }

    get muted() {
        return this.#muted;
    }

    toggle() {
        this.#muted = !this.#muted;
        return this.#muted;
    }

    move() {
        this.#play([[523, 0.05]]);
    }

    capture() {
        this.#play([[392, 0.05], [196, 0.09]]);
    }

    check() {
        this.#play([[880, 0.06], [988, 0.09]]);
    }

    start() {
        this.#play([[440, 0.06], [660, 0.1]]);
    }

    end() {
        this.#play([[523, 0.08], [659, 0.08], [784, 0.08], [1047, 0.18]]);
    }

    notify() {
        this.#play([[1175, 0.04], [1568, 0.05]]);
    }

    #play(notes) {
        if (this.#muted) return;
        try {
            this.#context ??= new AudioContext();
            if (this.#context.state === "suspended") this.#context.resume();
            let time = this.#context.currentTime;
            for (const [frequency, duration] of notes) {
                const oscillator = this.#context.createOscillator();
                const gain = this.#context.createGain();
                oscillator.type = "square";
                oscillator.frequency.setValueAtTime(frequency, time);
                gain.gain.setValueAtTime(VOLUME, time);
                gain.gain.exponentialRampToValueAtTime(0.0001, time + duration);
                oscillator.connect(gain).connect(this.#context.destination);
                oscillator.start(time);
                oscillator.stop(time + duration);
                time += duration + NOTE_GAP;
            }
        } catch {
        }
    }
}
