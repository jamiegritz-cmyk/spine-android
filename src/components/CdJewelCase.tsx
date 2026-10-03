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
          className={`absolute inset-0 [backface-visibility:hidden] rounded-[4px] bg-[#0c0b0a] shadow-[0_22px_45px_rgba(0,0,0,0.95),0_8px_20px_rgba(0,0,0,0.85)] border border-white/30 overflow-hidden flex cursor-pointer transition-transform active:scale-[0.98] ${
            !isFlipped ? 'hover:border-white/50' : ''
          }`}
          title={isPlaying ? 'Click to Pause' : 'Click to Play'}
        >
          {/* LAYER 1 & 4: Left Clear Acrylic Jewel Case Hinge Section (Exaggerated, unmistakable physical construction) */}
          <div className="w-[20px] sm:w-[22px] h-full shrink-0 bg-gradient-to-r from-white/25 via-white/5 to-black/60 border-r-2 border-black/90 relative flex flex-col justify-between py-4 items-center z-10">
            {/* Top molded circular hinge pivot with bright white rim and deep dark hole */}
            <div className="w-3 h-3 rounded-full border border-white/60 bg-white/15 flex items-center justify-center shadow-inner">
              <div className="w-1.5 h-1.5 rounded-full bg-black/80" />
            </div>

            {/* Central molded acrylic vertical rib */}
            <div className="w-[2px] h-20 rounded-full bg-white/20 border-r border-black/50" />

            {/* Bottom molded circular hinge pivot with bright white rim and deep dark hole */}
            <div className="w-3 h-3 rounded-full border border-white/60 bg-white/15 flex items-center justify-center shadow-inner">
              <div className="w-1.5 h-1.5 rounded-full bg-black/80" />
            </div>

            {/* Distinct vertical hinge joint groove seam */}
            <div className="absolute right-0 inset-y-0 w-[1.5px] bg-black/95 pointer-events-none" />
            <div className="absolute right-[1.5px] inset-y-0 w-[0.5px] bg-white/25 pointer-events-none" />
          </div>

          {/* LAYER 1, 4 & 5: Case Tray Interior with Visible Plastic Borders (Top, Right, Bottom) */}
          <div className="relative flex-1 h-full p-[6px] bg-[#100f0e] overflow-hidden group/art flex">
            {/* LAYER 2: Printed Album Booklet Insert (Recessed INSIDE the case tray) */}
            <div className="relative w-full h-full rounded-[2px] overflow-hidden bg-[#181715] shadow-[0_2px_8px_rgba(0,0,0,0.9),inset_0_0_4px_rgba(0,0,0,0.85)] border border-black/80">
              <img
                src={release.artworkUrl}
                alt={release.title}
                className="w-full h-full object-cover select-none pointer-events-none"
                referrerPolicy="no-referrer"
              />

              {/* Matte printed paper booklet border & recess shadow */}
              <div className="absolute inset-0 pointer-events-none border border-black/50 shadow-inner" />

              {/* Play/Pause Overlay Icon on hover */}
              <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover/art:opacity-100 transition-opacity bg-black/35">
                <div className="w-11 h-11 rounded-full bg-black/80 border border-white/60 flex items-center justify-center text-white shadow-2xl backdrop-blur-xs">
                  {isPlaying ? (
                    <Pause className="w-5 h-5 fill-current" />
                  ) : (
                    <Play className="w-5 h-5 fill-current translate-x-0.5" />
                  )}
                </div>
              </div>
            </div>

            {/* LAYER 3 & 6: Transparent Plastic Front Lid & Physical Highlights Overlay */}
            {/* Clear acrylic front lid catching light across the entire face */}
            <div
              className="absolute inset-0 pointer-events-none"
              style={{
                background:
                  'linear-gradient(130deg, rgba(255,255,255,0.18) 0%, transparent 40%, rgba(255,255,255,0.05) 65%, transparent 100%)'
              }}
            />

            {/* Secondary diagonal glare streak */}
            <div
              className="absolute inset-0 pointer-events-none opacity-50"
              style={{
                background:
                  'linear-gradient(115deg, transparent 20%, rgba(255,255,255,0.15) 35%, transparent 45%)'
              }}
            />

            {/* Top edge bright specular highlight line */}
            <div className="absolute inset-x-0 top-0 h-[1.5px] bg-white/60 pointer-events-none" />

            {/* Right edge dark bevel & thumb tab opening notch */}
            <div className="absolute inset-y-0 right-0 w-[2px] bg-black/90 pointer-events-none" />
            <div className="absolute right-0 top-1/2 -translate-y-1/2 w-[3px] h-6 bg-black/90 border-l border-white/40 rounded-l-xs pointer-events-none" />

            {/* Bottom edge dark bevel shadow */}
            <div className="absolute inset-x-0 bottom-0 h-[2px] bg-black/90 pointer-events-none" />

            {/* Inner bevel establishing thick physical acrylic wall around booklet */}
            <div className="absolute inset-[3px] pointer-events-none rounded-[2px] border border-white/20" />

            {/* Hairline handling micro-scratches on front clear plastic */}
            <div className="absolute top-[16%] right-[20%] w-8 h-[0.75px] bg-white/25 transform rotate-12 pointer-events-none" />
            <div className="absolute bottom-[24%] left-[24%] w-6 h-[0.5px] bg-white/20 transform -rotate-6 pointer-events-none" />
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
