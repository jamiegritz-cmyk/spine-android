import React from 'react';
import { Release } from '../types/music';

interface PhysicalCdSpineProps {
  release: Release;
  isSelected: boolean;
  onClick: () => void;
}

/**
 * Authentic Standard Audio CD Jewel Case Spine (125mm x 10mm proportion).
 * Slim, narrow, with transparent clear polystyrene edges and realistic paper inlay.
 */
export const PhysicalCdSpine: React.FC<PhysicalCdSpineProps> = ({
  release,
  isSelected,
  onClick
}) => {
  const renderSpineContent = () => {
    switch (release.spineStyle) {
      case 'oasis_definitely_maybe':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#f6f0dd] text-[#1c1917] relative overflow-hidden font-sans">
            {/* Top Oasis Boxed Logo */}
            <div className="w-full flex items-center justify-center pt-1 pb-0.5 shrink-0">
              <div className="px-1 py-[1px] bg-black rounded-[1.5px] shadow-xs transform rotate-90">
                <span className="text-[6.5px] font-black tracking-tighter text-white font-sans lowercase leading-none">
                  oasis
                </span>
              </div>
            </div>

            {/* Script Definitely Maybe Title */}
            <div className="flex-1 w-full flex items-center justify-center relative overflow-hidden">
              <span
                className="whitespace-nowrap text-[7.5px] font-serif italic font-semibold text-black select-none"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  letterSpacing: '0.03em',
                  maxHeight: '120px'
                }}
              >
                Definitely Maybe
              </span>
            </div>

            {/* Bottom Living Room Photo Motif & Catalog */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-3 h-3.5 rounded-[1px] overflow-hidden border border-black/30 shadow-inner">
                <img
                  src={release.artworkUrl}
                  alt=""
                  draggable={false}
                  className="w-full h-full object-cover brightness-95 pointer-events-none"
                  referrerPolicy="no-referrer"
                />
              </div>
              <span className="text-[5.5px] font-mono text-black/60 scale-90">CRECD</span>
            </div>
          </div>
        );

      case 'radiohead_ok_computer':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ffffff] text-[#0f172a] relative overflow-hidden">
            {/* Top digital glitch lines */}
            <div className="w-full h-4 shrink-0 opacity-75 overflow-hidden border-b border-neutral-300">
              <div className="w-full h-full bg-gradient-to-b from-sky-200/60 to-transparent flex flex-col justify-around px-0.5">
                <div className="w-full h-[1px] bg-sky-500/70" />
                <div className="w-3/4 h-[1px] bg-slate-800" />
              </div>
            </div>

            {/* Vertical typography: RADIOHEAD OK COMPUTER */}
            <div className="flex-1 w-full flex items-center justify-center overflow-hidden">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-bold tracking-tight text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '125px'
                }}
              >
                RADIOHEAD <span className="text-sky-700 font-normal">OK COMPUTER</span>
              </span>
            </div>

            {/* Bottom Parlophone symbol & Catalog */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 text-[5.5px] font-mono text-neutral-500">
              <span className="font-bold scale-90">NODATA</span>
            </div>
          </div>
        );

      case 'beatles_abbey_road':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#121212] text-white relative overflow-hidden">
            {/* Top Drop-T Beatles Logo */}
            <div className="w-full flex items-center justify-center pt-1 shrink-0">
              <span
                className="text-[6.5px] font-serif font-bold tracking-wider text-neutral-200"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed'
                }}
              >
                THE BEATLES
              </span>
            </div>

            {/* Middle Abbey Road text */}
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-medium text-neutral-300 select-none uppercase tracking-wider"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                ABBEY ROAD
              </span>
            </div>

            {/* Bottom: Zebra Crosswalk photo slice */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-3 h-4 rounded-[1px] overflow-hidden border border-white/20">
                <img
                  src={release.artworkUrl}
                  alt=""
                  draggable={false}
                  className="w-full h-full object-cover pointer-events-none"
                  referrerPolicy="no-referrer"
                />
              </div>
              <div className="w-2 h-[1px] bg-green-500/80 rounded-full" />
            </div>
          </div>
        );

      case 'pink_floyd_dark_side':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0a0a0a] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span
                className="text-[6.5px] font-sans font-bold tracking-widest text-neutral-300"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed'
                }}
              >
                PINK FLOYD
              </span>
            </div>

            {/* Middle Prism & Spectrum Rainbow */}
            <div className="flex-1 w-full flex flex-col items-center justify-center gap-0.5 my-0.5">
              <div className="w-2.5 h-2.5 border border-white/60 rotate-45 flex items-center justify-center">
                <div className="w-1 h-1 bg-white/40" />
              </div>
              <div
                className="w-0.5 h-9 rounded-full my-0.5"
                style={{
                  background: 'linear-gradient(to bottom, #ef4444, #f97316, #eab308, #22c55e, #3b82f6, #a855f7)'
                }}
              />
            </div>

            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-500">
              HARVEST
            </div>
          </div>
        );

      case 'blur_parklife':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#eab308] text-[#1c1917] relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[7.5px] font-black tracking-tighter text-blue-900 font-sans lowercase">
                blur
              </span>
            </div>

            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-black tracking-widest text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                PARKLIFE
              </span>
            </div>

            {/* Bottom Greyhound dog illustration */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-2.5 h-2.5 rounded-full bg-neutral-900 flex items-center justify-center text-[5.5px] text-yellow-400 font-bold">
                🐕
              </div>
              <span className="text-[5px] font-mono text-neutral-800">FOODCD</span>
            </div>
          </div>
        );

      case 'the_verve_urban_hymns':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#1c261e] text-slate-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[6px] font-sans font-light tracking-wide text-neutral-400 lowercase">
                the verve
              </span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-serif font-bold text-slate-100 select-none uppercase tracking-wider"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                URBAN HYMNS
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-500">
              HUTCD
            </div>
          </div>
        );

      case 'coldplay_parachutes':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0b172a] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-2 h-2 rounded-full bg-amber-400/90 shadow-[0_0_4px_rgba(251,191,36,0.8)]" />
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-semibold tracking-wider text-amber-300 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                COLDPLAY <span className="font-normal text-white">PARACHUTES</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-amber-400/60">
              PARLOPHONE
            </div>
          </div>
        );

      case 'the_stone_roses':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#3a352c] text-amber-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[6.5px] text-yellow-300 font-bold">🍋</span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7px] font-sans font-black tracking-widest text-amber-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                THE STONE ROSES
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-400">
              ORE CD
            </div>
          </div>
        );

      case 'the_killers_hot_fuss':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0284c7] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-2 h-0.5 bg-red-500 rounded-xs shadow-[0_0_3px_red]" />
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-bold tracking-tight text-white select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                THE KILLERS <span className="text-red-200">HOT FUSS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-sky-200">
              LIZARD
            </div>
          </div>
        );

      case 'queen_greatest_hits':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#7f1d1d] text-amber-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[6.5px] text-amber-300 font-serif">👑</span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-serif font-black tracking-wider text-amber-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                QUEEN <span className="font-sans font-normal text-[6.5px]">GREATEST HITS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-amber-300/70">
              EMI
            </div>
          </div>
        );

      case 'arctic_monkeys_am':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#09090b] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[6.5px] font-black text-white">AM</span>
            </div>
            <div className="flex-1 w-full flex flex-col items-center justify-center gap-0.5 my-0.5">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-semibold tracking-wider text-neutral-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                ARCTIC MONKEYS
              </span>
              <div className="w-[1px] h-8 bg-white/80 rounded-full" />
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-400">
              DOMINO
            </div>
          </div>
        );

      case 'pulp_different_class':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ffffff] text-neutral-900 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-2.5 h-2.5 rounded-full border border-black flex items-center justify-center">
                <span className="text-[5.5px] font-black text-black">P</span>
              </div>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-bold tracking-tight text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                PULP <span className="font-normal text-neutral-600">DIFFERENT CLASS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-500">
              ISLAND
            </div>
          </div>
        );

      case 'david_bowie_hunky_dory':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#d5c7a3] text-neutral-900 relative overflow-hidden">
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-serif font-bold tracking-wider text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                DAVID BOWIE <span className="italic font-normal">HUNKY DORY</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-neutral-600">
              RCA
            </div>
          </div>
        );

      case 'muse_origin_of_symmetry':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ea580c] text-white relative overflow-hidden">
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-black tracking-wider text-white select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '120px'
                }}
              >
                MUSE <span className="font-light text-[6.5px]">ORIGIN OF SYMMETRY</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono text-orange-200">
              MUSHROOM
            </div>
          </div>
        );

      default:
        // Default authentic procedural CD spine for any release (including local user music)
        return (
          <div
            className="w-full h-full flex flex-col justify-between py-1 text-white relative overflow-hidden"
            style={{ backgroundColor: release.spineColor || '#222222' }}
          >
            {/* Top artwork slice thumbnail */}
            <div className="w-full h-5 shrink-0 overflow-hidden border-b border-white/20">
              <img
                src={release.artworkUrl}
                alt=""
                draggable={false}
                className="w-full h-full object-cover pointer-events-none"
                referrerPolicy="no-referrer"
              />
            </div>

            {/* Rotated text */}
            <div className="flex-1 w-full flex items-center justify-center overflow-hidden py-0.5">
              <span
                className="whitespace-nowrap text-[7.5px] font-sans font-bold tracking-tight select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  color: release.spineTextColor || '#ffffff',
                  textShadow: '0 1px 2px rgba(0,0,0,0.8)',
                  maxHeight: '120px'
                }}
              >
                {release.artist} <span className="opacity-60">/</span> {release.title}
              </span>
            </div>

            {/* Bottom catalog ID */}
            <div className="w-full shrink-0 pb-0.5 text-center text-[5.5px] font-mono opacity-60">
              {release.catalogNumber}
            </div>
          </div>
        );
    }
  };

  return (
    <div
      onClick={onClick}
      onDragStart={(e) => e.preventDefault()}
      className={`cd-spine-item group relative transition-all duration-300 ease-out cursor-pointer shrink-0 select-none ${
        isSelected
          ? '-translate-y-5 scale-y-[1.03] scale-x-[1.02] z-30'
          : 'hover:-translate-y-1 z-10'
      }`}
      style={{
        width: '19px', // Authentic slim 10mm CD jewel case proportion
        height: '240px' // Increased ~13% so bases naturally meet and rest directly on the shelf
      }}
      title={`${release.artist} — ${release.title}`}
    >
      {/* Outer Clear Polystyrene Jewel Case Plastic Frame */}
      <div
        className={`w-full h-full rounded-[2.5px] relative flex flex-col overflow-hidden transition-all bg-gradient-to-r from-white/30 via-white/10 via-black/20 to-black/60 p-[2px] ${
          isSelected
            ? 'shadow-[0_0_24px_rgba(255,255,255,0.3),0_18px_36px_rgba(0,0,0,0.95)] ring-[1.5px] ring-white/60'
            : 'shadow-[0_2px_4px_rgba(0,0,0,0.95)] hover:shadow-[0_4px_10px_rgba(0,0,0,0.95)]'
        }`}
      >
        {/* TOP TRANSPARENT ACRYLIC RAIL (Delicate audio CD molded hub notch) */}
        <div className="w-full h-3 shrink-0 bg-gradient-to-b from-white/45 via-white/15 to-transparent border-b border-white/20 relative flex items-center justify-center">
          <div className="w-1.5 h-1.5 rounded-full border border-white/60 bg-white/30 shadow-inner" />
          <div className="absolute inset-x-0 top-0 h-[0.5px] bg-white/70" />
        </div>

        {/* MIDDLE: Paper Spine Insert RECESSED inside the transparent plastic shell */}
        <div className="flex-1 w-full rounded-[1px] relative overflow-hidden shadow-inner my-0.5 border-x border-black/30">
          {renderSpineContent()}

          {/* LEFT SPECULAR HIGHLIGHT (Clear acrylic edge refraction) */}
          <div className="absolute inset-y-0 left-0 w-[1.5px] bg-gradient-to-r from-white/60 via-white/20 to-transparent pointer-events-none" />

          {/* RIGHT GROOVE SHADOW (Jewel case seam) */}
          <div className="absolute inset-y-0 right-0 w-[1.5px] bg-gradient-to-l from-black/80 via-black/30 to-transparent pointer-events-none" />

          {/* Transparent acrylic edge illumination when pulled forward */}
          {isSelected && (
            <div className="absolute inset-0 bg-gradient-to-t from-white/15 via-transparent to-white/25 pointer-events-none" />
          )}
        </div>

        {/* BOTTOM TRANSPARENT ACRYLIC RAIL (Jewel case plastic foot) */}
        <div className="w-full h-2.5 shrink-0 bg-gradient-to-t from-white/35 via-white/10 to-transparent border-t border-white/20 relative flex items-center justify-center">
          <div className="w-2 h-[1px] bg-white/50 rounded-full" />
          <div className="absolute inset-x-0 bottom-0 h-[0.5px] bg-black/70" />
        </div>

        {/* Outer edge specular line along left plastic edge */}
        <div className="absolute inset-y-0 left-0 w-[1px] bg-white/50 pointer-events-none" />
        {/* Outer edge seam line along right plastic edge */}
        <div className="absolute inset-y-0 right-0 w-[1px] bg-black/80 pointer-events-none" />
      </div>
    </div>
  );
};
