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
  children: React.ReactNode;
}

export const AndroidFrame: React.FC<AndroidFrameProps> = ({
  orientation,
  onChangeOrientation,
  onOpenExportModal,
  onOpenLocalPicker,
  onOpenSearch,
  onOpenCorrectModal,
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
            Physical CD Library
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
        className={`relative transition-all duration-300 bg-[#121212] shadow-[0_25px_70px_rgba(0,0,0,0.95)] border-[7px] border-[#222222] flex flex-col overflow-hidden ${
          orientation === 'portrait'
            ? 'w-full max-w-[400px] h-[840px] max-h-[92vh] rounded-[46px]'
            : 'w-full max-w-[880px] h-[580px] max-h-[92vh] rounded-[36px]'
        }`}
      >
        {/* Android Status Bar (Matching reference image: 20:34 / 87% / Wi-Fi) */}
        <div className="w-full h-8 px-6 pt-1 flex items-center justify-between shrink-0 bg-[#121212] text-[11.5px] font-mono text-neutral-300 select-none z-30">
          {/* Time */}
          <span className="font-semibold tracking-tight">20:34</span>

          {/* Camera Punch Hole on Phone */}
          {orientation === 'portrait' && (
            <div className="w-3.5 h-3.5 rounded-full bg-black border border-neutral-800 shadow-inner -translate-y-0.5" />
          )}

          {/* Wi-Fi & Battery 87% */}
          <div className="flex items-center gap-1.5 text-neutral-300 font-sans text-xs">
            <Wifi className="w-3.5 h-3.5" />
            <div className="flex items-center gap-0.5">
              <span className="font-mono text-[11px]">87%</span>
              <BatteryCharging className="w-3.5 h-3.5 text-neutral-200" />
            </div>
          </div>
        </div>

        {/* Android App Bar (Menu ≡, Search 🔍, Overflow ⋮) */}
        <div className="w-full h-11 px-5 flex items-center justify-between shrink-0 bg-[#121212] z-30">
          <button
            onClick={onOpenLocalPicker}
            className="p-1 text-neutral-300 hover:text-white transition-colors"
            title="Library Menu & Add Music"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-4 text-neutral-300">
            <button
              onClick={onOpenSearch}
              className="p-1 hover:text-white transition-colors"
              title="Search CD Collection"
            >
              <Search className="w-5 h-5" />
            </button>
            <button
              onClick={onOpenCorrectModal || onOpenExportModal}
              className="p-1 hover:text-white transition-colors"
              title="Identify / Correct Album Artwork on MusicBrainz"
            >
              <MoreVertical className="w-5 h-5" />
            </button>
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
