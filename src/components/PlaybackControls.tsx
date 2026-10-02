import React, { useRef, useState } from 'react';
import { Play, Pause, SkipBack, SkipForward, Shuffle, Repeat, Repeat1 } from 'lucide-react';
import { RepeatMode } from '../types/music';

interface PlaybackControlsProps {
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  isShuffle: boolean;
  repeatMode: RepeatMode;
  onPlayPause: () => void;
  onNext: () => void;
  onPrevious: () => void;
  onSeek: (seconds: number) => void;
  onToggleShuffle: () => void;
  onCycleRepeat: () => void;
  layout?: 'stacked' | 'horizontal';
}

export const PlaybackControls: React.FC<PlaybackControlsProps> = ({
  isPlaying,
  currentTime,
  duration,
  isShuffle,
  repeatMode,
  onPlayPause,
  onNext,
  onPrevious,
  onSeek,
  onToggleShuffle,
  onCycleRepeat
}) => {
  const [isSeeking, setIsSeeking] = useState(false);
  const [seekTime, setSeekTime] = useState(0);
  const progressBarRef = useRef<HTMLDivElement>(null);

  const displayTime = isSeeking ? seekTime : currentTime;
  const progressPercent = duration > 0 ? (displayTime / duration) * 100 : 0;
  const remainingTime = Math.max(0, duration - displayTime);

  const formatTime = (sec: number) => {
    const total = Math.max(0, Math.floor(sec));
    const m = Math.floor(total / 60);
    const s = total % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const handleSeekStart = (e: React.MouseEvent<HTMLDivElement> | React.TouchEvent<HTMLDivElement>) => {
    setIsSeeking(true);
    handleSeekMove(e);
  };

  const handleSeekMove = (e: React.MouseEvent<HTMLDivElement> | React.TouchEvent<HTMLDivElement>) => {
    if (!progressBarRef.current) return;
    const rect = progressBarRef.current.getBoundingClientRect();
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clickPos = Math.max(0, Math.min(rect.width, clientX - rect.left));
    const newProgress = (clickPos / rect.width) * duration;
    setSeekTime(newProgress);
  };

  const handleSeekEnd = () => {
    if (isSeeking) {
      setIsSeeking(false);
      onSeek(seekTime);
    }
  };

  return (
    <div className="w-full flex flex-col items-center">
      {/* Progress Scrubber Bar */}
      <div className="w-full px-4 flex flex-col gap-1.5">
        <div
          ref={progressBarRef}
          onMouseDown={handleSeekStart}
          onMouseMove={isSeeking ? handleSeekMove : undefined}
          onMouseUp={handleSeekEnd}
          onTouchStart={handleSeekStart}
          onTouchMove={isSeeking ? handleSeekMove : undefined}
          onTouchEnd={handleSeekEnd}
          className="relative w-full h-4 flex items-center cursor-pointer group"
        >
          {/* Background Track */}
          <div className="w-full h-[2.5px] bg-neutral-700/80 rounded-full overflow-hidden transition-all group-hover:h-1">
            {/* Active Filled Bar */}
            <div
              className="h-full bg-neutral-100 transition-[width] duration-75"
              style={{ width: `${Math.min(100, Math.max(0, progressPercent))}%` }}
            />
          </div>

          {/* Scrubber Knob */}
          <div
            className="absolute w-3 h-3 bg-white rounded-full shadow-md -translate-x-1/2"
            style={{ left: `${Math.min(100, Math.max(0, progressPercent))}%` }}
          />
        </div>

        {/* Timers: Left current time, Right negative remaining time (like reference image: 1:12 / -4:28) */}
        <div className="flex justify-between items-center text-[11px] font-mono text-neutral-400">
          <span>{formatTime(displayTime)}</span>
          <span>-{formatTime(remainingTime)}</span>
        </div>
      </div>

      {/* Main Buttons Row: Exactly matches reference image */}
      <div className="w-full max-w-xs flex items-center justify-between px-3 mt-1">
        {/* Shuffle */}
        <button
          onClick={onToggleShuffle}
          className={`p-2 transition-colors active:scale-95 ${
            isShuffle ? 'text-white' : 'text-neutral-400 hover:text-neutral-200'
          }`}
          title="Shuffle"
        >
          <Shuffle className="w-4 h-4" />
        </button>

        {/* Skip Previous */}
        <button
          onClick={onPrevious}
          className="p-2 text-neutral-200 hover:text-white transition-colors active:scale-95"
          title="Previous Track"
        >
          <SkipBack className="w-5 h-5 fill-current" />
        </button>

        {/* Prominent Play / Pause Outline/Filled Circle Button */}
        <button
          onClick={onPlayPause}
          className="w-12 h-12 rounded-full border-2 border-white/90 hover:border-white hover:bg-white/10 text-white flex items-center justify-center transition-all active:scale-95 cursor-pointer shadow-lg shadow-black/40"
          title={isPlaying ? 'Pause' : 'Play'}
        >
          {isPlaying ? (
            <Pause className="w-5 h-5 fill-current" />
          ) : (
            <Play className="w-5 h-5 fill-current translate-x-0.5" />
          )}
        </button>

        {/* Skip Next */}
        <button
          onClick={onNext}
          className="p-2 text-neutral-200 hover:text-white transition-colors active:scale-95"
          title="Next Track"
        >
          <SkipForward className="w-5 h-5 fill-current" />
        </button>

        {/* Repeat Cycle */}
        <button
          onClick={onCycleRepeat}
          className={`p-2 transition-colors active:scale-95 ${
            repeatMode !== 'off' ? 'text-white' : 'text-neutral-400 hover:text-neutral-200'
          }`}
          title={`Repeat: ${repeatMode}`}
        >
          {repeatMode === 'one' ? (
            <Repeat1 className="w-4 h-4 text-white" />
          ) : (
            <Repeat className="w-4 h-4" />
          )}
        </button>
      </div>
    </div>
  );
};
