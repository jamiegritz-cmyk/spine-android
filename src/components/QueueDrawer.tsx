import React from 'react';
import { Release, Track } from '../types/music';
import { X, Play, Disc3, Music2 } from 'lucide-react';

interface QueueDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  currentRelease: Release;
  currentTrackIndex: number;
  isPlaying: boolean;
  onSelectTrack: (index: number) => void;
  queue: Track[];
}

export const QueueDrawer: React.FC<QueueDrawerProps> = ({
  isOpen,
  onClose,
  currentRelease,
  currentTrackIndex,
  isPlaying,
  onSelectTrack,
  queue
}) => {
  if (!isOpen) return null;

  const formatTrackDuration = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/70 backdrop-blur-sm transition-opacity">
      <div className="w-full sm:max-w-md bg-neutral-900 border border-neutral-800 rounded-t-2xl sm:rounded-2xl max-h-[80vh] flex flex-col shadow-2xl overflow-hidden animate-in slide-in-from-bottom duration-200">
        {/* Drawer Header */}
        <div className="px-4 py-3 border-b border-neutral-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Music2 className="w-4 h-4 text-neutral-400" />
            <h3 className="text-sm font-semibold text-neutral-100">Playback Queue</h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded text-neutral-400 hover:text-white hover:bg-neutral-800"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Current Playing CD Info */}
        <div className="p-4 bg-neutral-950/60 border-b border-neutral-800/80 flex items-center gap-3">
          <img
            src={currentRelease.artworkUrl}
            alt={currentRelease.title}
            className="w-12 h-12 rounded object-cover border border-white/10 shrink-0"
          />
          <div className="min-w-0 flex-1">
            <p className="text-[10px] uppercase font-mono tracking-wider text-neutral-500">
              NOW PLAYING DISC
            </p>
            <h4 className="text-xs font-semibold text-neutral-100 truncate">
              {currentRelease.title}
            </h4>
            <p className="text-[11px] text-neutral-400 truncate">{currentRelease.artist}</p>
          </div>
        </div>

        {/* Tracks List */}
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {currentRelease.tracks.map((track, idx) => {
            const isCurrent = idx === currentTrackIndex;
            return (
              <button
                key={track.id}
                onClick={() => {
                  onSelectTrack(idx);
                  onClose();
                }}
                className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-left transition-colors ${
                  isCurrent
                    ? 'bg-neutral-800 text-white font-medium'
                    : 'text-neutral-300 hover:bg-neutral-800/50 hover:text-white'
                }`}
              >
                <div className="flex items-center gap-3 truncate">
                  <span className="w-5 text-xs font-mono text-neutral-500 text-center">
                    {isCurrent && isPlaying ? (
                      <Disc3 className="w-3.5 h-3.5 text-neutral-200 animate-spin mx-auto" />
                    ) : (
                      track.trackNumber
                    )}
                  </span>
                  <div className="truncate">
                    <p className="text-xs truncate">{track.title}</p>
                    <p className="text-[10px] text-neutral-500 truncate">{track.artist}</p>
                  </div>
                </div>
                <span className="text-[11px] font-mono text-neutral-500 shrink-0 ml-2">
                  {formatTrackDuration(track.duration)}
                </span>
              </button>
            );
          })}
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-neutral-800 bg-neutral-950/40 flex justify-between items-center text-[11px] text-neutral-500 font-mono">
          <span>{currentRelease.tracks.length} tracks on release</span>
          <span>{currentRelease.catalogNumber}</span>
        </div>
      </div>
    </div>
  );
};
