import React, { useState } from 'react';
import { Release, Track } from '../types/music';
import { RotateCw, Disc3, Play, Pause } from 'lucide-react';

interface CdJewelCaseProps {
  release: Release;
  currentTrackIndex: number;
  isPlaying: boolean;
  onPlayTrack: (index: number) => void;
  onTogglePlayPause?: () => void;
  size?: 'compact' | 'normal' | 'large';
}

export const CdJewelCase: React.FC<CdJewelCaseProps> = ({
  release,
  currentTrackIndex,
  isPlaying,
  onPlayTrack,
  onTogglePlayPause,
  size = 'normal'
}) => {
  const [isFlipped, setIsFlipped] = useState(false);

  const formatTrackDuration = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = Math.floor(sec % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  const dimensionsClass =
    size === 'large'
      ? 'w-[230px] h-[230px] sm:w-[260px] sm:h-[260px]'
      : size === 'normal'
      ? 'w-[180px] h-[180px] sm:w-[200px] sm:h-[200px]'
      : 'w-[160px] h-[160px] sm:w-[180px] sm:h-[180px]';

  return (
    <div className="relative group select-none flex flex-col items-center">
      {/* 3D Perspective Container */}
      <div
        className={`relative transition-transform duration-500 [transform-style:preserve-3d] ${
          isFlipped ? '[transform:rotateY(180deg)]' : ''
        } ${dimensionsClass}`}
      >
        {/* FRONT: Album Booklet Artwork inside Jewel Case - Clicking toggles playback! */}
        <div
          onClick={!isFlipped ? onTogglePlayPause : undefined}
          className={`absolute inset-0 [backface-visibility:hidden] rounded-[3px] bg-white/[0.06] shadow-[0_18px_40px_rgba(0,0,0,0.9),0_6px_16px_rgba(0,0,0,0.75)] border-t border-l border-white/40 border-b border-r border-black/80 overflow-hidden flex cursor-pointer transition-transform active:scale-[0.98] ${
            !isFlipped ? 'hover:border-white/50' : ''
          }`}
          title={isPlaying ? 'Click to Pause' : 'Click to Play'}
        >
          {/* 1. Left Clear Acrylic Jewel Case Hinge Strip */}
          <div className="w-[14px] sm:w-[16px] h-full shrink-0 bg-gradient-to-r from-white/20 via-transparent to-black/35 border-r border-white/20 relative flex flex-col justify-between py-5 items-center z-10">
            {/* Top molded circular hinge pivot with white rim and dark recess */}
            <div className="w-2.5 h-2.5 rounded-full border border-white/50 bg-white/10 flex items-center justify-center shadow-inner">
              <div className="w-1.5 h-1.5 rounded-full bg-black/50" />
            </div>

            {/* Central molded acrylic vertical rib */}
            <div className="w-[1.5px] h-16 rounded-full bg-white/15 border-r border-black/30" />

            {/* Bottom molded circular hinge pivot with white rim and dark recess */}
            <div className="w-2.5 h-2.5 rounded-full border border-white/50 bg-white/10 flex items-center justify-center shadow-inner">
              <div className="w-1.5 h-1.5 rounded-full bg-black/50" />
            </div>

            {/* Vertical hinge joint seam shadow */}
            <div className="absolute right-0 inset-y-0 w-[1px] bg-black/60 pointer-events-none" />
          </div>

          {/* 2. Inner Tray with Inset Booklet Insert (Artwork sits visibly INSIDE the case) */}
          <div className="relative flex-1 h-full p-[3.5px] bg-black/40 overflow-hidden group/art flex">
            {/* Booklet Paper Insert Container */}
            <div className="relative w-full h-full rounded-[1.5px] overflow-hidden bg-[#181715] shadow-[0_1px_4px_rgba(0,0,0,0.8),inset_0_0_0_1px_rgba(0,0,0,0.6)]">
              <img
                src={release.artworkUrl}
                alt={release.title}
                className="w-full h-full object-cover select-none pointer-events-none"
                referrerPolicy="no-referrer"
              />

              {/* Booklet paper edge shadow into tray recess */}
              <div className="absolute inset-0 pointer-events-none border border-black/40 shadow-inner" />

              {/* Subtle Play/Pause Overlay Icon on hover */}
              <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover/art:opacity-100 transition-opacity bg-black/30">
                <div className="w-10 h-10 rounded-full bg-black/70 border border-white/50 flex items-center justify-center text-white shadow-xl backdrop-blur-xs">
                  {isPlaying ? (
                    <Pause className="w-5 h-5 fill-current" />
                  ) : (
                    <Play className="w-5 h-5 fill-current translate-x-0.5" />
                  )}
                </div>
              </div>
            </div>

            {/* 3. Transparent Polystyrene Front Lid & Highlights Overlay */}
            {/* Soft diagonal specular reflection across the plastic face */}
            <div className="absolute inset-0 pointer-events-none bg-gradient-to-tr from-transparent via-white/[0.07] to-white/[0.14]" />

            {/* Secondary diagonal glare streak */}
            <div
              className="absolute inset-0 pointer-events-none opacity-40"
              style={{
                background:
                  'linear-gradient(115deg, transparent 20%, rgba(255,255,255,0.12) 35%, transparent 45%)'
              }}
            />

            {/* Inner bevel line establishing 1.5mm acrylic wall thickness */}
            <div className="absolute inset-[1px] pointer-events-none rounded-[2px] border border-white/15" />

            {/* Right-edge thumb opening notch */}
            <div className="absolute right-0 top-1/2 -translate-y-1/2 w-[2px] h-4 bg-black/70 border-l border-white/30 rounded-l-xs pointer-events-none" />

            {/* Hairline handling micro-scratches on front plastic */}
            <div className="absolute top-[18%] right-[22%] w-6 h-[0.5px] bg-white/20 transform rotate-12 pointer-events-none" />
            <div className="absolute bottom-[28%] left-[28%] w-5 h-[0.5px] bg-white/15 transform -rotate-6 pointer-events-none" />
          </div>
        </div>

        {/* BACK: Jewel Case Inlay Tray (Tracklist, Barcode, Catalog) */}
        <div className="absolute inset-0 [backface-visibility:hidden] [transform:rotateY(180deg)] rounded-[4px] bg-[#161514] shadow-2xl shadow-black/80 border border-white/10 overflow-hidden flex flex-col text-neutral-300 p-4 justify-between font-sans">
          {/* Back Inlay Header */}
          <div className="border-b border-neutral-700/60 pb-2">
            <div className="flex justify-between items-start">
              <div>
                <p className="text-[10px] tracking-widest uppercase font-mono text-neutral-400">
                  {release.catalogNumber} · {release.genre}
                </p>
                <h4 className="text-sm font-semibold text-neutral-100 truncate mt-0.5">
                  {release.title}
                </h4>
                <p className="text-xs text-neutral-400 truncate">{release.artist}</p>
              </div>
              <span className="text-[10px] font-mono text-neutral-400 bg-neutral-800/80 px-1.5 py-0.5 rounded">
                {release.year}
              </span>
            </div>
          </div>

          {/* Tracklist on Back Cover */}
          <div className="my-2 flex-1 overflow-y-auto pr-1 space-y-1 text-xs">
            {release.tracks.map((trk, idx) => {
              const isCurrent = idx === currentTrackIndex;
              return (
                <button
                  key={trk.id}
                  onClick={() => onPlayTrack(idx)}
                  className={`w-full flex items-center justify-between px-2 py-1 rounded transition-colors text-left group/trk ${
                    isCurrent
                      ? 'bg-neutral-800 text-white font-medium'
                      : 'text-neutral-400 hover:text-neutral-200 hover:bg-neutral-800/40'
                  }`}
                >
                  <div className="flex items-center gap-2 truncate">
                    <span className="w-4 text-[10px] font-mono text-neutral-500">
                      {isCurrent && isPlaying ? (
                        <Disc3 className="w-3 h-3 text-neutral-200 animate-spin" />
                      ) : (
                        trk.trackNumber
                      )}
                    </span>
                    <span className="truncate">{trk.title}</span>
                  </div>
                  <span className="text-[10px] font-mono text-neutral-500 ml-2 shrink-0">
                    {formatTrackDuration(trk.duration)}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Barcode & Physical Release Footer */}
          <div className="pt-2 border-t border-neutral-800 flex justify-between items-center text-[9px] font-mono text-neutral-500">
            <div className="flex flex-col">
              <span>COMPACT DISC DIGITAL AUDIO</span>
              <span className="tracking-widest">STEREO · DDD</span>
            </div>
            {/* Simulated Barcode */}
            <div className="flex items-center gap-[1px] h-5 bg-neutral-200 p-0.5 rounded-[1px]">
              {[1, 2, 1, 3, 1, 2, 2, 1, 3, 2, 1, 1, 2, 1, 2, 1].map((w, i) => (
                <div
                  key={i}
                  className="h-full bg-neutral-900"
                  style={{ width: `${w}px` }}
                />
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Flip Case Button */}
      <button
        onClick={() => setIsFlipped(!isFlipped)}
        className="mt-2.5 flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-neutral-800/80 hover:bg-neutral-700/80 text-[11px] font-medium text-neutral-300 hover:text-white transition-colors border border-white/5 active:scale-95"
        title="Flip CD jewel case to view back inlay"
      >
        <RotateCw className="w-3 h-3" />
        <span>{isFlipped ? 'View Front Cover' : 'Inspect CD Inlay'}</span>
      </button>
    </div>
  );
};
