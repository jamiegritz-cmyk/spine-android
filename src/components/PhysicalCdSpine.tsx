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
 * Enlarged/zoomed typography and artwork slices for effortless legibility while scrolling.
 */
export const PhysicalCdSpine: React.FC<PhysicalCdSpineProps> = ({
  release,
  isSelected,
  onClick
}) => {
  const renderSpineContent = () => {
    switch (release.spineStyle) {
      case 'concrete_echoes':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#edece8] text-[#121212] relative overflow-hidden font-sans">
            {/* Top Artwork Thumbnail */}
            <div className="w-full shrink-0 flex items-center justify-center pt-0.5 pb-1">
              <div className="w-4 h-4.5 rounded-[1px] overflow-hidden border border-black/30 shadow-xs">
                <img
                  src={release.artworkUrl}
                  alt=""
                  draggable={false}
                  className="w-full h-full object-cover grayscale pointer-events-none"
                  referrerPolicy="no-referrer"
                />
              </div>
            </div>

            {/* Vertical typography: POST-ROCK CONCRETE ECHOES */}
            <div className="flex-1 w-full flex items-center justify-center overflow-hidden">
              <span
                className="whitespace-nowrap text-[10px] font-sans font-black tracking-tight text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '160px'
                }}
              >
                POST-ROCK <span className="font-extrabold text-black">CONCRETE ECHOES</span>
              </span>
            </div>

            {/* Bottom: Catalog PR 004 */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 text-[7px] font-mono font-bold text-neutral-800">
              <span>PR</span>
              <span>004</span>
            </div>
          </div>
        );

      case 'oasis_definitely_maybe':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#f6f0dd] text-[#1c1917] relative overflow-hidden font-sans">
            {/* Top Oasis Boxed Logo */}
            <div className="w-full flex items-center justify-center pt-1 pb-0.5 shrink-0">
              <div className="px-1.5 py-[1.5px] bg-black rounded-[2px] shadow-xs transform rotate-90">
                <span className="text-[7.5px] font-black tracking-tighter text-white font-sans lowercase leading-none">
                  oasis
                </span>
              </div>
            </div>

            {/* Script Definitely Maybe Title */}
            <div className="flex-1 w-full flex items-center justify-center relative overflow-hidden">
              <span
                className="whitespace-nowrap text-[10px] font-serif italic font-bold text-black select-none"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  letterSpacing: '0.04em',
                  maxHeight: '150px'
                }}
              >
                Definitely Maybe
              </span>
            </div>

            {/* Bottom Living Room Photo Motif & Catalog */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-3.5 h-4 rounded-[1.5px] overflow-hidden border border-black/40 shadow-inner">
                <img
                  src={release.artworkUrl}
                  alt=""
                  draggable={false}
                  className="w-full h-full object-cover brightness-95 pointer-events-none"
                  referrerPolicy="no-referrer"
                />
              </div>
              <span className="text-[6.5px] font-mono font-bold text-black/70">CRECD</span>
            </div>
          </div>
        );

      case 'radiohead_ok_computer':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ffffff] text-[#0f172a] relative overflow-hidden">
            {/* Top digital glitch lines */}
            <div className="w-full h-4.5 shrink-0 opacity-85 overflow-hidden border-b border-neutral-300">
              <div className="w-full h-full bg-gradient-to-b from-sky-200/60 to-transparent flex flex-col justify-around px-0.5">
                <div className="w-full h-[1.5px] bg-sky-500/80" />
                <div className="w-4/5 h-[1.5px] bg-slate-800" />
              </div>
            </div>

            {/* Vertical typography: RADIOHEAD OK COMPUTER */}
            <div className="flex-1 w-full flex items-center justify-center overflow-hidden">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-black tracking-tight text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                RADIOHEAD <span className="text-sky-700 font-bold">OK COMPUTER</span>
              </span>
            </div>

            {/* Bottom Parlophone symbol & Catalog */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 text-[6.5px] font-mono text-neutral-600">
              <span className="font-bold">NODATA</span>
            </div>
          </div>
        );

      case 'beatles_abbey_road':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#121212] text-white relative overflow-hidden">
            {/* Top Drop-T Beatles Logo */}
            <div className="w-full flex items-center justify-center pt-1 shrink-0">
              <span
                className="text-[8px] font-serif font-black tracking-wider text-neutral-100"
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
                className="whitespace-nowrap text-[9.5px] font-sans font-bold text-neutral-100 select-none uppercase tracking-wider"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                ABBEY ROAD
              </span>
            </div>

            {/* Bottom: Zebra Crosswalk photo slice */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-3.5 h-4.5 rounded-[1.5px] overflow-hidden border border-white/30 shadow-inner">
                <img
                  src={release.artworkUrl}
                  alt=""
                  draggable={false}
                  className="w-full h-full object-cover pointer-events-none"
                  referrerPolicy="no-referrer"
                />
              </div>
              <div className="w-2.5 h-[1.5px] bg-green-500 rounded-full" />
            </div>
          </div>
        );

      case 'pink_floyd_dark_side':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0a0a0a] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span
                className="text-[8px] font-sans font-black tracking-widest text-neutral-200"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed'
                }}
              >
                PINK FLOYD
              </span>
            </div>

            {/* Middle Prism & Spectrum Rainbow */}
            <div className="flex-1 w-full flex flex-col items-center justify-center gap-1 my-0.5">
              <div className="w-3 h-3 border border-white/70 rotate-45 flex items-center justify-center">
                <div className="w-1.5 h-1.5 bg-white/50" />
              </div>
              <div
                className="w-1 h-11 rounded-full my-0.5"
                style={{
                  background: 'linear-gradient(to bottom, #ef4444, #f97316, #eab308, #22c55e, #3b82f6, #a855f7)'
                }}
              />
            </div>

            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono font-bold text-neutral-400">
              HARVEST
            </div>
          </div>
        );

      case 'blur_parklife':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#eab308] text-[#1c1917] relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[9px] font-black tracking-tighter text-blue-900 font-sans lowercase">
                blur
              </span>
            </div>

            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-black tracking-widest text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                PARKLIFE
              </span>
            </div>

            {/* Bottom Greyhound dog illustration */}
            <div className="w-full shrink-0 flex flex-col items-center pb-0.5 gap-0.5">
              <div className="w-3 h-3 rounded-full bg-neutral-900 flex items-center justify-center text-[6.5px] text-yellow-400 font-bold">
                🐕
              </div>
              <span className="text-[6px] font-mono font-bold text-neutral-800">FOODCD</span>
            </div>
          </div>
        );

      case 'the_verve_urban_hymns':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#1c261e] text-slate-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[7.5px] font-sans font-medium tracking-wide text-neutral-300 lowercase">
                the verve
              </span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-serif font-bold text-slate-100 select-none uppercase tracking-wider"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                URBAN HYMNS
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-neutral-400">
              HUTCD
            </div>
          </div>
        );

      case 'coldplay_parachutes':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0b172a] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-2.5 h-2.5 rounded-full bg-amber-400 shadow-[0_0_5px_rgba(251,191,36,0.9)]" />
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-bold tracking-wider text-amber-300 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                COLDPLAY <span className="font-semibold text-white">PARACHUTES</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-amber-300/80">
              PARLOPHONE
            </div>
          </div>
        );

      case 'the_stone_roses':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#3a352c] text-amber-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[8px] text-yellow-300 font-bold">🍋</span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9px] font-sans font-black tracking-widest text-amber-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                THE STONE ROSES
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-neutral-400">
              ORE CD
            </div>
          </div>
        );

      case 'the_killers_hot_fuss':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#0284c7] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-2.5 h-1 bg-red-500 rounded-xs shadow-[0_0_3px_red]" />
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-bold tracking-tight text-white select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                THE KILLERS <span className="text-red-200">HOT FUSS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-sky-200">
              LIZARD
            </div>
          </div>
        );

      case 'queen_greatest_hits':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#7f1d1d] text-amber-200 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[8px] text-amber-300 font-serif">👑</span>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-serif font-black tracking-wider text-amber-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                QUEEN <span className="font-sans font-bold text-[8px]">GREATEST HITS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-amber-300/80">
              EMI
            </div>
          </div>
        );

      case 'arctic_monkeys_am':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#09090b] text-white relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <span className="text-[8px] font-black text-white">AM</span>
            </div>
            <div className="flex-1 w-full flex flex-col items-center justify-center gap-0.5 my-0.5">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-bold tracking-wider text-neutral-100 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                ARCTIC MONKEYS
              </span>
              <div className="w-[1.5px] h-9 bg-white/90 rounded-full" />
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-neutral-400">
              DOMINO
            </div>
          </div>
        );

      case 'pulp_different_class':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ffffff] text-neutral-900 relative overflow-hidden">
            <div className="w-full pt-1 flex items-center justify-center">
              <div className="w-3 h-3 rounded-full border border-black flex items-center justify-center">
                <span className="text-[6.5px] font-black text-black">P</span>
              </div>
            </div>
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-black tracking-tight text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                PULP <span className="font-bold text-neutral-600">DIFFERENT CLASS</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-neutral-500">
              ISLAND
            </div>
          </div>
        );

      case 'david_bowie_hunky_dory':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#d5c7a3] text-neutral-900 relative overflow-hidden">
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-serif font-black tracking-wider text-neutral-900 select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                DAVID BOWIE <span className="italic font-bold">HUNKY DORY</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-neutral-600">
              RCA
            </div>
          </div>
        );

      case 'muse_origin_of_symmetry':
        return (
          <div className="w-full h-full flex flex-col justify-between py-1 bg-[#ea580c] text-white relative overflow-hidden">
            <div className="flex-1 w-full flex items-center justify-center">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-black tracking-wider text-white select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  maxHeight: '150px'
                }}
              >
                MUSE <span className="font-medium text-[8px]">ORIGIN OF SYMMETRY</span>
              </span>
            </div>
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono text-orange-200">
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
            {/* Top artwork slice thumbnail - enlarged for clarity */}
            <div className="w-full h-6 shrink-0 overflow-hidden border-b border-white/20">
              <img
                src={release.artworkUrl}
                alt=""
                draggable={false}
                className="w-full h-full object-cover pointer-events-none"
                referrerPolicy="no-referrer"
              />
            </div>

            {/* Rotated text - bold and crisp for effortless readability while scrolling */}
            <div className="flex-1 w-full flex items-center justify-center overflow-hidden py-0.5">
              <span
                className="whitespace-nowrap text-[9.5px] font-sans font-bold tracking-tight select-none uppercase"
                style={{
                  writingMode: 'vertical-rl',
                  textOrientation: 'mixed',
                  color: release.spineTextColor || '#ffffff',
                  textShadow: '0 1px 2px rgba(0,0,0,0.85)',
                  maxHeight: '150px'
                }}
              >
                {release.artist} <span className="opacity-60">/</span> {release.title}
              </span>
            </div>

            {/* Bottom catalog ID */}
            <div className="w-full shrink-0 pb-0.5 text-center text-[6.5px] font-mono opacity-70">
              {release.catalogNumber}
            </div>
          </div>
        );
    }
  };

  const catalogCode = React.useMemo(() => {
    const digits = release.catalogNumber?.replace(/\D/g, '') || '';
    if (digits.length >= 4) return digits.slice(-4);
    const hash = Math.abs((release.id + release.title).split('').reduce((acc, c) => acc * 31 + c.charCodeAt(0), 0));
    return String((hash % 9000) + 1000);
  }, [release.id, release.catalogNumber, release.title]);

  return (
    <div
      onClick={onClick}
      onDragStart={(e) => e.preventDefault()}
      className={`cd-spine-item group relative transition-all duration-300 ease-out cursor-pointer shrink-0 select-none ${
        isSelected
          ? '-translate-y-5 scale-y-[1.02] scale-x-[1.02] z-30'
          : 'hover:-translate-y-1.5 z-10'
      }`}
      style={{
        width: '21.5px', // Authentic slim jewel case spine width
        height: '280px'
      }}
      title={`${release.artist} — ${release.title}`}
    >
      {/* Outer Physical Clear Jewel Case Shell */}
      <div
        className={`w-full h-full rounded-t-[1.5px] rounded-b-[0.5px] relative overflow-hidden transition-all bg-white/[0.06] ${
          isSelected
            ? 'shadow-[0_0_16px_rgba(255,255,255,0.22),0_12px_24px_rgba(0,0,0,0.95)]'
            : 'shadow-[0_2px_4px_rgba(0,0,0,0.95)] hover:shadow-[0_4px_8px_rgba(0,0,0,0.95)]'
        }`}
      >
        {/* Layer 1: Top Clear Acrylic Cap Background (12px) */}
        <div className="absolute top-0 inset-x-0 h-[12px] bg-gradient-to-b from-white/20 via-white/5 to-transparent pointer-events-none z-10">
          {/* Top Edge Specular Hairline */}
          <div className="absolute top-0 inset-x-0 h-[0.75px] bg-white/60" />
          {/* Molded Jewel Case Hinge Notches */}
          <div className="absolute top-[4px] inset-x-[1.5px] h-[0.75px] bg-black/45 shadow-[0_0.5px_0_rgba(255,255,255,0.35)]" />
          <div className="absolute top-[8px] inset-x-[2px] h-[0.6px] bg-black/30" />
        </div>

        {/* Layer 2: Inset Paper / Sleeve Insert (Tray Card Inlay) */}
        {/* Sits inset inside the transparent plastic case (2px margins on sides, 12px top, 8px bottom) */}
        <div
          className="absolute inset-x-[2px] top-[12px] bottom-[8px] rounded-[0.5px] overflow-hidden flex flex-col z-0 border border-black/35 shadow-xs"
          style={{
            backgroundColor: release.spineColor || '#222222'
          }}
        >
          {/* Render paper sleeve content (artwork, typography, catalog) */}
          <div className="w-full h-full relative overflow-hidden flex flex-col">
            {renderSpineContent()}

            {/* Printed cardstock paper texture overlay (eliminates flat digital look) */}
            <div className="absolute inset-0 pointer-events-none bg-gradient-to-b from-white/[0.08] via-transparent to-black/[0.18]" />
          </div>
        </div>

        {/* Layer 3: Bottom Clear Acrylic Foot & Physical Shelf Contact */}
        <div className="absolute bottom-0 inset-x-0 h-[8px] bg-gradient-to-t from-black/60 via-white/5 to-transparent pointer-events-none z-10">
          {/* Clear reflection catch */}
          <div className="absolute bottom-[2px] inset-x-[1.5px] h-[0.5px] bg-white/25" />
          {/* Grounding contact shadow onto wooden shelf */}
          <div className="absolute bottom-0 inset-x-0 h-[1.5px] bg-black/95" />
        </div>

        {/* Layer 4: Transparent Plastic Shell Walls & Specular Reflections */}
        {/* Left Acrylic Side Wall (2px) */}
        <div className="absolute inset-y-0 left-0 w-[2px] pointer-events-none z-20 flex">
          <div className="w-[0.75px] h-full bg-white/45" />
          <div className="w-[1px] h-full bg-black/45" />
          <div className="w-[0.25px] h-full bg-white/20" />
        </div>

        {/* Right Acrylic Side Wall (2px) */}
        <div className="absolute inset-y-0 right-0 w-[2px] pointer-events-none z-20 flex justify-end">
          <div className="w-[0.5px] h-full bg-black/35" />
          <div className="w-[1px] h-full bg-black/65" />
          <div className="w-[0.5px] h-full bg-white/25" />
        </div>

        {/* Front Face Light Sheen (Subtle, Restrained Polystyrene Reflection) */}
        <div className="absolute inset-0 pointer-events-none bg-gradient-to-br from-white/[0.07] via-transparent to-black/[0.05] z-20" />
      </div>
    </div>
  );
};
