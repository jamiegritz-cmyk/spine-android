/**
 * Audio Engine for Spine:
 * 1. High-fidelity Web Audio synthesis for curated physical CD releases.
 * 2. HTML5 Audio element integration for user-loaded local audio files.
 * 3. Tactile physical sound effects for CD spine sliding and jewel case clicks.
 */

class AudioEngine {
  private ctx: AudioContext | null = null;
  private currentSource: AudioBufferSourceNode | null = null;
  private gainNode: GainNode | null = null;
  private audioElement: HTMLAudioElement | null = null;
  private isSynthesizing = false;
  private synthInterval: number | null = null;
  private synthGain: GainNode | null = null;
  private startTime = 0;
  private pauseOffset = 0;
  private currentDuration = 180; // default seconds
  private isPlaying = false;
  private onTimeUpdateCallback: ((currentSec: number, durationSec: number) => void) | null = null;
  private onEndedCallback: (() => void) | null = null;

  constructor() {
    this.audioElement = new Audio();
    this.audioElement.addEventListener('timeupdate', () => {
      if (this.audioElement && this.onTimeUpdateCallback) {
        this.onTimeUpdateCallback(
          this.audioElement.currentTime,
          this.audioElement.duration || this.currentDuration
        );
      }
    });
    this.audioElement.addEventListener('ended', () => {
      this.isPlaying = false;
      if (this.onEndedCallback) {
        this.onEndedCallback();
      }
    });
  }

  private initContext() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioCtx();
      this.gainNode = this.ctx.createGain();
      this.gainNode.gain.setValueAtTime(0.7, this.ctx.currentTime);
      this.gainNode.connect(this.ctx.destination);
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  public setCallbacks(
    onTimeUpdate: (currentSec: number, durationSec: number) => void,
    onEnded: () => void
  ) {
    this.onTimeUpdateCallback = onTimeUpdate;
    this.onEndedCallback = onEnded;
  }

  /**
   * Plays subtle physical tactile sound when a CD spine is pulled or slid
   */
  public playSpineSlideSound() {
    try {
      this.initContext();
      if (!this.ctx) return;
      const t = this.ctx.currentTime;
      // Filtered noise click mimicking jewel case plastic touching
      const bufferSize = this.ctx.sampleRate * 0.04;
      const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (bufferSize * 0.25));
      }
      const noise = this.ctx.createBufferSource();
      noise.buffer = buffer;

      const filter = this.ctx.createBiquadFilter();
      filter.type = 'bandpass';
      filter.frequency.setValueAtTime(1400, t);
      filter.Q.setValueAtTime(3.0, t);

      const gain = this.ctx.createGain();
      gain.gain.setValueAtTime(0.04, t);
      gain.gain.exponentialRampToValueAtTime(0.001, t + 0.04);

      noise.connect(filter);
      filter.connect(gain);
      gain.connect(this.ctx.destination);
      noise.start(t);
    } catch {
      // Ignore audio autoplay restrictions
    }
  }

  /**
   * Plays a track: either user local file or procedural audio synthesis
   */
  public playTrack(audioUrl?: string, duration = 180, seed = 1) {
    this.initContext();
    this.stop();
    this.currentDuration = duration;
    this.isPlaying = true;

    if (audioUrl && audioUrl.startsWith('blob:')) {
      // Local user audio file
      if (this.audioElement) {
        this.audioElement.src = audioUrl;
        this.audioElement.play().catch(() => {});
      }
      return;
    }

    // Procedural synthesizer for default CD collection
    this.isSynthesizing = true;
    this.startTime = Date.now() - (this.pauseOffset * 1000);
    this.startProceduralMusic(seed);
  }

  public pause() {
    this.isPlaying = false;
    if (this.audioElement && this.audioElement.src) {
      this.audioElement.pause();
    }
    if (this.isSynthesizing) {
      this.pauseOffset = (Date.now() - this.startTime) / 1000;
      this.stopProcedural();
    }
  }

  public resume() {
    if (this.audioElement && this.audioElement.src) {
      this.isPlaying = true;
      this.audioElement.play().catch(() => {});
    } else if (this.isSynthesizing || this.pauseOffset > 0) {
      this.isPlaying = true;
      this.startTime = Date.now() - (this.pauseOffset * 1000);
      this.startProceduralMusic(1);
    }
  }

  public seek(seconds: number) {
    if (this.audioElement && this.audioElement.src) {
      this.audioElement.currentTime = seconds;
    } else {
      this.pauseOffset = seconds;
      this.startTime = Date.now() - (seconds * 1000);
      if (this.onTimeUpdateCallback) {
        this.onTimeUpdateCallback(seconds, this.currentDuration);
      }
    }
  }

  public stop() {
    this.isPlaying = false;
    this.pauseOffset = 0;
    if (this.audioElement) {
      this.audioElement.pause();
      this.audioElement.removeAttribute('src');
    }
    this.stopProcedural();
  }

  private stopProcedural() {
    if (this.synthInterval) {
      window.clearInterval(this.synthInterval);
      this.synthInterval = null;
    }
    if (this.synthGain && this.ctx) {
      try {
        this.synthGain.gain.setValueAtTime(0, this.ctx.currentTime);
        this.synthGain.disconnect();
      } catch {}
      this.synthGain = null;
    }
  }

  private startProceduralMusic(seed: number) {
    if (!this.ctx) return;
    this.stopProcedural();

    this.synthGain = this.ctx.createGain();
    this.synthGain.gain.setValueAtTime(0.18, this.ctx.currentTime);
    this.synthGain.connect(this.gainNode || this.ctx.destination);

    // Progression notes based on seed
    const scales: number[][] = [
      // D minor pentatonic / modal jazz
      [146.83, 174.61, 196.00, 220.00, 261.63, 293.66, 349.23, 392.00],
      // Eb major 7 ambient
      [155.56, 196.00, 233.08, 293.66, 311.13, 392.00, 466.16, 587.33],
      // A minor neo-classical
      [110.00, 164.81, 220.00, 261.63, 329.63, 440.00, 523.25, 659.25],
      // F# minor atmospheric
      [92.50, 138.59, 185.00, 220.00, 277.18, 369.99, 440.00, 554.37],
    ];
    const scale = scales[Math.abs(seed) % scales.length];

    let step = 0;
    const playChordStep = () => {
      if (!this.ctx || !this.synthGain || !this.isPlaying) return;
      const t = this.ctx.currentTime;

      // Bass drone note
      const bassFreq = scale[step % 3] / 2;
      const bassOsc = this.ctx.createOscillator();
      bassOsc.type = 'triangle';
      bassOsc.frequency.setValueAtTime(bassFreq, t);

      const bassFilter = this.ctx.createBiquadFilter();
      bassFilter.type = 'lowpass';
      bassFilter.frequency.setValueAtTime(260, t);

      const bassEnv = this.ctx.createGain();
      bassEnv.gain.setValueAtTime(0.001, t);
      bassEnv.gain.linearRampToValueAtTime(0.22, t + 0.3);
      bassEnv.gain.exponentialRampToValueAtTime(0.001, t + 3.8);

      bassOsc.connect(bassFilter);
      bassFilter.connect(bassEnv);
      bassEnv.connect(this.synthGain);
      bassOsc.start(t);
      bassOsc.stop(t + 4.0);

      // Warm rhodes-like melody triad
      const note1 = scale[(step + 1) % scale.length];
      const note2 = scale[(step + 3) % scale.length];
      const note3 = scale[(step + 5) % scale.length];

      [note1, note2, note3].forEach((freq, idx) => {
        if (!this.ctx || !this.synthGain) return;
        const noteOsc = this.ctx.createOscillator();
        noteOsc.type = idx === 0 ? 'sine' : 'triangle';
        noteOsc.frequency.setValueAtTime(freq, t + idx * 0.15);

        const noteFilter = this.ctx.createBiquadFilter();
        noteFilter.type = 'lowpass';
        noteFilter.frequency.setValueAtTime(1200, t);

        const noteGain = this.ctx.createGain();
        const noteStart = t + idx * 0.15;
        noteGain.gain.setValueAtTime(0.0001, noteStart);
        noteGain.gain.linearRampToValueAtTime(0.12, noteStart + 0.08);
        noteGain.gain.exponentialRampToValueAtTime(0.0001, noteStart + 2.5);

        noteOsc.connect(noteFilter);
        noteFilter.connect(noteGain);
        noteGain.connect(this.synthGain);
        noteOsc.start(noteStart);
        noteOsc.stop(noteStart + 2.8);
      });

      step = (step + 1) % scale.length;
    };

    // Play immediate first chord
    playChordStep();

    // Loop steps every 3.2 seconds
    this.synthInterval = window.setInterval(() => {
      if (this.isPlaying) {
        const elapsed = (Date.now() - this.startTime) / 1000;
        if (this.onTimeUpdateCallback) {
          this.onTimeUpdateCallback(elapsed, this.currentDuration);
        }
        if (elapsed >= this.currentDuration) {
          this.isPlaying = false;
          this.stopProcedural();
          if (this.onEndedCallback) {
            this.onEndedCallback();
          }
          return;
        }
        playChordStep();
      }
    }, 3200);
  }
}

export const audioEngine = new AudioEngine();
