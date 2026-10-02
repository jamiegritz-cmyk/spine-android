import React from 'react';
import { DeviceOrientation } from '../types/music';
import { Smartphone, Tablet, Code2, Wifi, BatteryCharging, Menu, Search, MoreVertical } from 'lucide-react';

interface AndroidFrameProps {
  orientation: DeviceOrientation;
  onChangeOrientation: (o: DeviceOrientation) => void;
  onOpenExportModal: () => void;
  onOpenLocalPicker: () => void;
  onOpenSearch: () => void;
  onOpenCorrectModal?: () => void;
  catalogNumber?: string;
  filterMode?: 'albums' | 'singles';
  onFilterModeChange?: (mode: 'albums' | 'singles') => void;
  onRefresh?: () => void;
  isRefreshing?: boolean;
  children: React.ReactNode;
}

export const AndroidFrame: React.FC<AndroidFrameProps> = ({
  orientation,
  onChangeOrientation,
  onOpenExportModal,
  onOpenLocalPicker,
  onOpenSearch,
  onOpenCorrectModal,
  catalogNumber = 'RA-6405',
  filterMode = 'albums',
  onFilterModeChange,
  onRefresh,
  isRefreshing = false,
  children
}) => {
  return (
    <div className="min-h-screen w-full bg-[#080808] text-neutral-100 flex flex-col items-center justify-center p-2 sm:p-4 font-sans select-none overflow-x-hidden">
      {/* Top Ambient System Toolbar */}
      <header className="w-full max-w-5xl mb-2 flex items-center justify-between px-2 text-xs">
        {/* Left: App Identity */}
        <div className="flex items-center gap-2">
          <span className="font-bold tracking-widest text-sm text-neutral-200">SPINE</span>
          <span className="text-[11px] text-neutral-500 font-mono hidden sm:inline">
            Native Android CD Shelf
          </span>
        </div>

        {/* Center: Device Mode Switcher (Phone vs Tablet matches reference image!) */}
        <div className="flex items-center gap-1 bg-neutral-900/90 p-1 rounded-xl border border-neutral-800">
          <button
            onClick={() => onChangeOrientation('portrait')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              orientation === 'portrait'
                ? 'bg-neutral-800 text-white shadow-sm'
                : 'text-neutral-400 hover:text-neutral-200'
            }`}
            title="Pixel Phone (Portrait Layout from reference image)"
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Phone</span>
          </button>

          <button
            onClick={() => onChangeOrientation('landscape')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              orientation === 'landscape'
                ? 'bg-neutral-800 text-white shadow-sm'
                : 'text-neutral-400 hover:text-neutral-200'
            }`}
            title="Android Tablet (Landscape Layout from reference image)"
          >
            <Tablet className="w-3.5 h-3.5" />
            <span>Tablet</span>
          </button>
        </div>

        {/* Right: Android Studio Project Export */}
        <button
          onClick={onOpenExportModal}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-neutral-900 hover:bg-neutral-800 text-neutral-200 border border-neutral-800 transition-colors active:scale-95 text-xs font-medium"
          title="Inspect and download Jetpack Compose + Kotlin project"
        >
          <Code2 className="w-3.5 h-3.5 text-emerald-400" />
          <span className="hidden sm:inline">Kotlin Source & Export</span>
        </button>
      </header>

      {/* Realistic Device Frame (Pixel Phone or Android Tablet from Reference Image) */}
      <div
        className={`relative transition-all duration-300 bg-[#11100F] shadow-[0_25px_70px_rgba(0,0,0,0.95)] border-[7px] border-[#222222] flex flex-col overflow-hidden ${
          orientation === 'portrait'
            ? 'w-full max-w-[400px] h-[840px] max-h-[92vh] rounded-[46px]'
            : 'w-full max-w-[880px] h-[580px] max-h-[92vh] rounded-[36px]'
        }`}
      >
        {/* Android Status Bar (Matching reference image: 21:21 / 18% / Wi-Fi) */}
        <div className="w-full h-8 px-6 pt-1 flex items-center justify-between shrink-0 bg-[#11100F] text-[11.5px] font-mono text-neutral-300 select-none z-30">
          {/* Time: 21:21 matching mock-up */}
          <span className="font-semibold tracking-tight">21:21</span>

          {/* Camera Punch Hole on Phone */}
          {orientation === 'portrait' && (
            <div className="w-3.5 h-3.5 rounded-full bg-black border border-neutral-800 shadow-inner -translate-y-0.5" />
          )}

          {/* Wi-Fi & Battery 18% matching mock-up */}
          <div className="flex items-center gap-1.5 text-neutral-300 font-sans text-xs">
            <Wifi className="w-3.5 h-3.5" />
            <div className="flex items-center gap-0.5">
              <span className="font-mono text-[11px] text-neutral-300">18</span>
              <BatteryCharging className="w-3.5 h-3.5 text-red-400" />
            </div>
          </div>
        </div>

        {/* Android App Header (Three independent layout regions: Left (SPINE), Centre (Fixed Controls), Right (Catalog)) */}
        <div className="w-full h-11 px-5 flex items-center justify-between shrink-0 bg-[#11100F] z-30 border-b border-neutral-900/60 relative">
          {/* Left: Spine/menu area */}
          <div className="flex items-center gap-3 shrink-0 z-10">
            <span className="font-black tracking-[0.2em] text-sm text-neutral-100 select-none">
              SPINE
            </span>
            <button
              onClick={onOpenLocalPicker}
              className="p-1 text-neutral-400 hover:text-white transition-colors"
              title="Add Music / MediaStore Library"
            >
              <Menu className="w-4 h-4" />
            </button>
          </div>

          {/* Centre: Fixed Albums / Singles / A-Z / Refresh controls (Anchored dead-center) */}
          <div className="absolute left-1/2 -translate-x-1/2 flex items-center gap-1.5 shrink-0 z-10 pointer-events-auto">
            {onFilterModeChange && (
              <div className="flex items-center bg-[#1F1D1B] p-0.5 rounded-lg border border-neutral-800/80">
                <button
                  onClick={() => onFilterModeChange('albums')}
                  className={`px-2.5 py-0.5 rounded-md text-[11px] font-medium transition-all ${
                    filterMode === 'albums'
                      ? 'bg-[#383430] text-white shadow-xs'
                      : 'text-neutral-400 hover:text-neutral-200'
                  }`}
                >
                  Albums
                </button>
                <button
                  onClick={() => onFilterModeChange('singles')}
                  className={`px-2.5 py-0.5 rounded-md text-[11px] font-medium transition-all ${
                    filterMode === 'singles'
                      ? 'bg-[#383430] text-white shadow-xs'
                      : 'text-neutral-400 hover:text-neutral-200'
                  }`}
                >
                  Singles
                </button>
                <span className="px-1 text-[9px] font-mono text-neutral-400 font-bold">A–Z</span>
              </div>
            )}

            {/* Refresh Library Button */}
            {onRefresh && (
              <button
                onClick={onRefresh}
                className="p-1 text-neutral-400 hover:text-white transition-colors"
                title="Rescan device music"
              >
                <svg
                  className={`w-3.5 h-3.5 ${isRefreshing ? 'animate-spin text-amber-400' : ''}`}
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
              </button>
            )}
          </div>

          {/* Right: Selected album metadata (anchored to right, can truncate) */}
          <div className="flex items-center justify-end shrink-0 max-w-[76px] overflow-hidden z-10">
            <span className="font-mono text-xs text-neutral-400 font-medium truncate">
              {catalogNumber}
            </span>
          </div>
        </div>

        {/* Main Application Screen */}
        <main className="flex-1 flex flex-col overflow-hidden relative bg-[#121212]">
          {children}
        </main>

        {/* Android Bottom Gesture Navigation Bar Pill */}
        <div className="w-full h-4 shrink-0 bg-[#121212] flex items-center justify-center pointer-events-none z-30">
          <div className="w-24 h-1 bg-neutral-600/70 rounded-full" />
        </div>
      </div>
    </div>
  );
};
