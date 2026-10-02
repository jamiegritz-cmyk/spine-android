import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Release, Track, RepeatMode, DeviceOrientation } from './types/music';
import { DEFAULT_CD_COLLECTION } from './data/defaultCollection';
import { audioEngine } from './audio/audioEngine';
import { CdJewelCase } from './components/CdJewelCase';
import { CdShelf } from './components/CdShelf';
import { PlaybackControls } from './components/PlaybackControls';
import { QueueDrawer } from './components/QueueDrawer';
import { LocalMusicModal } from './components/LocalMusicModal';
import { CodeExportModal } from './components/CodeExportModal';
import { CorrectArtworkModal } from './components/CorrectArtworkModal';
import { AndroidFrame } from './components/AndroidFrame';
import { Search, X } from 'lucide-react';

export default function App() {
  const [releases, setReleases] = useState<Release[]>(DEFAULT_CD_COLLECTION);
  // Default to Oasis - Definitely Maybe matching reference image
  const [selectedReleaseId, setSelectedReleaseId] = useState<string>('oasis_definitely_maybe');
  const [currentTrackIndex, setCurrentTrackIndex] = useState<number>(0);
  const [isPlaying, setIsPlaying] = useState<boolean>(true);
  // 1:12 current time and 340s duration -> exactly 1:12 and -4:28 as in reference image
  const [currentTime, setCurrentTime] = useState<number>(72);
  const [duration, setDuration] = useState<number>(340);
  const [isShuffle, setIsShuffle] = useState<boolean>(false);
  const [repeatMode, setRepeatMode] = useState<RepeatMode>('off');

  // Filter & Search & Modals
  const [filterCategory, setFilterCategory] = useState<'all' | 'albums' | 'singles'>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isSearchOpen, setIsSearchOpen] = useState<boolean>(false);
  const [orientation, setOrientation] = useState<DeviceOrientation>('portrait');
  const [isQueueOpen, setIsQueueOpen] = useState<boolean>(false);
  const [isLocalPickerOpen, setIsLocalPickerOpen] = useState<boolean>(false);
  const [isExportModalOpen, setIsExportModalOpen] = useState<boolean>(false);
  const [isCorrectModalOpen, setIsCorrectModalOpen] = useState<boolean>(false);

  // Compute filtered releases for the CD shelf
  const filteredReleases = useMemo(() => {
    let list = [...releases];

    if (filterCategory === 'albums') {
      list = list.filter((r) => r.tracks.length > 2);
    } else if (filterCategory === 'singles') {
      list = list.filter((r) => r.tracks.length <= 2);
    }

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      list = list.filter((r) => {
        const matchesTitle = r.title.toLowerCase().includes(q);
        const matchesArtist = r.artist.toLowerCase().includes(q);
        const matchesTrack = r.tracks.some((t) => t.title.toLowerCase().includes(q));
        return matchesTitle || matchesArtist || matchesTrack;
      });
    }

    return list;
  }, [releases, filterCategory, searchQuery]);

  // Current active release
  const currentRelease = useMemo(() => {
    return (
      filteredReleases.find((r) => r.id === selectedReleaseId) ||
      filteredReleases[3] || // Oasis Definitely Maybe
      releases[3]
    );
  }, [filteredReleases, selectedReleaseId, releases]);

  const selectedIndex = useMemo(() => {
    const idx = filteredReleases.findIndex((r) => r.id === currentRelease?.id);
    return idx >= 0 ? idx : 0;
  }, [filteredReleases, currentRelease]);

  const currentTrack: Track | undefined = currentRelease?.tracks[currentTrackIndex];

  // Configure Audio Engine Callbacks
  useEffect(() => {
    audioEngine.setCallbacks(
      (currSec, durSec) => {
        setCurrentTime(currSec);
        if (durSec > 0) setDuration(durSec);
      },
      () => {
        handleTrackEnded();
      }
    );
  }, [repeatMode, isShuffle, currentRelease, currentTrackIndex, filteredReleases]);

  const handleTrackEnded = useCallback(() => {
    if (repeatMode === 'one') {
      audioEngine.seek(0);
      playCurrentTrack(currentTrackIndex);
      return;
    }

    if (currentRelease && currentTrackIndex + 1 < currentRelease.tracks.length) {
      const nextIdx = currentTrackIndex + 1;
      setCurrentTrackIndex(nextIdx);
      playCurrentTrack(nextIdx);
    } else if (repeatMode === 'all' || isShuffle) {
      handleNextTrack();
    } else {
      setIsPlaying(false);
      audioEngine.pause();
    }
  }, [repeatMode, isShuffle, currentRelease, currentTrackIndex]);

  const playCurrentTrack = (trackIdx: number) => {
    if (!currentRelease) return;
    const track = currentRelease.tracks[trackIdx];
    if (!track) return;

    setCurrentTime(0);
    setDuration(track.duration);
    setIsPlaying(true);
    audioEngine.playTrack(track.audioUrl, track.duration, track.syntheticSeed ?? trackIdx);
  };

  const handleSelectRelease = (index: number) => {
    const target = filteredReleases[index];
    if (target && target.id !== currentRelease?.id) {
      setSelectedReleaseId(target.id);
      setCurrentTrackIndex(0);
      setCurrentTime(0);
      setDuration(target.tracks[0]?.duration || 340);
      // Update album cover, title and artist without automatically starting playback
      setIsPlaying(false);
      audioEngine.stop();
    }
  };

  const handlePlayPause = () => {
    if (isPlaying) {
      audioEngine.pause();
      setIsPlaying(false);
    } else {
      if (currentTrack) {
        if (currentTime > 0) {
          audioEngine.resume();
        } else {
          audioEngine.playTrack(
            currentTrack.audioUrl,
            currentTrack.duration,
            currentTrack.syntheticSeed ?? currentTrackIndex
          );
        }
        setIsPlaying(true);
      }
    }
  };

  const handleNextTrack = () => {
    if (!currentRelease) return;

    if (isShuffle) {
      const randomRelIdx = Math.floor(Math.random() * filteredReleases.length);
      const nextRel = filteredReleases[randomRelIdx];
      setSelectedReleaseId(nextRel.id);
      const randomTrkIdx = Math.floor(Math.random() * nextRel.tracks.length);
      setCurrentTrackIndex(randomTrkIdx);
      playCurrentTrack(randomTrkIdx);
      return;
    }

    if (currentTrackIndex + 1 < currentRelease.tracks.length) {
      const nextIdx = currentTrackIndex + 1;
      setCurrentTrackIndex(nextIdx);
      playCurrentTrack(nextIdx);
    } else {
      const nextRelIdx = (selectedIndex + 1) % filteredReleases.length;
      handleSelectRelease(nextRelIdx);
    }
  };

  const handlePreviousTrack = () => {
    if (currentTime > 3) {
      audioEngine.seek(0);
      setCurrentTime(0);
      return;
    }
    if (currentTrackIndex > 0) {
      const prevIdx = currentTrackIndex - 1;
      setCurrentTrackIndex(prevIdx);
      playCurrentTrack(prevIdx);
    } else {
      const prevRelIdx = selectedIndex > 0 ? selectedIndex - 1 : filteredReleases.length - 1;
      handleSelectRelease(prevRelIdx);
    }
  };

  const handleSeek = (seconds: number) => {
    setCurrentTime(seconds);
    audioEngine.seek(seconds);
  };

  const handleToggleShuffle = () => {
    setIsShuffle(!isShuffle);
  };

  const handleCycleRepeat = () => {
    const next: RepeatMode =
      repeatMode === 'off' ? 'all' : repeatMode === 'all' ? 'one' : 'off';
    setRepeatMode(next);
  };

  const handleAddLocalReleases = (newReleases: Release[]) => {
    setReleases((prev) => [...newReleases, ...prev]);
    setSelectedReleaseId(newReleases[0].id);
    setCurrentTrackIndex(0);
  };

  const handleUpdateReleaseArtwork = (updated: Release) => {
    setReleases((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
  };

  return (
    <AndroidFrame
      orientation={orientation}
      onChangeOrientation={setOrientation}
      onOpenExportModal={() => setIsExportModalOpen(true)}
      onOpenLocalPicker={() => setIsLocalPickerOpen(true)}
      onOpenSearch={() => setIsSearchOpen(true)}
      onOpenCorrectModal={() => setIsCorrectModalOpen(true)}
    >
      {/* Search Bar Overlay when toggled */}
      {isSearchOpen && (
        <div className="absolute top-0 inset-x-0 z-40 bg-[#141414] p-3 border-b border-neutral-800 shadow-xl flex items-center gap-2 animate-in slide-in-from-top duration-150">
          <Search className="w-4 h-4 text-neutral-400 shrink-0" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search albums, artists, or songs..."
            autoFocus
            className="flex-1 bg-transparent text-sm text-white focus:outline-none placeholder:text-neutral-500"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="p-1 text-neutral-400 hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <button
            onClick={() => {
              setIsSearchOpen(false);
              setSearchQuery('');
            }}
            className="text-xs font-medium text-neutral-400 hover:text-white px-2 py-1"
          >
            Done
          </button>
        </div>
      )}

      {/* Main View Area */}
      {orientation === 'portrait' ? (
        /* PORTRAIT PHONE LAYOUT (Adjusted to position album art slightly higher, reduce spacing, and give lower 50% to centered CD shelf) */
        <div className="flex-1 flex flex-col justify-between overflow-hidden bg-[#121212]">
          {/* Upper Section: Square Cover Slightly Higher, Reduced Vertical Spacing, Fully Visible Artwork */}
          <div className="flex-1 flex flex-col items-center justify-start px-4 pt-1.5 pb-0 min-h-0">
            {/* Square Front Cover Artwork - Tapping toggles play/pause */}
            {currentRelease && (
              <CdJewelCase
                release={currentRelease}
                currentTrackIndex={currentTrackIndex}
                isPlaying={isPlaying}
                onPlayTrack={(idx) => {
                  setCurrentTrackIndex(idx);
                  playCurrentTrack(idx);
                }}
                onTogglePlayPause={handlePlayPause}
                size="normal"
              />
            )}

            {/* Title & Artist with tight, clean vertical spacing */}
            <div className="mt-1.5 text-center px-4 max-w-sm">
              <h2 className="text-base font-bold text-neutral-100 truncate tracking-tight">
                {currentRelease?.title || 'Definitely Maybe'}
              </h2>
              <p className="text-xs text-neutral-400 truncate font-medium">
                {currentRelease?.artist || 'Oasis'}
              </p>
            </div>

            {/* Playback Scrubber and Transport Controls (Reduced spacing to shelf) */}
            <div className="w-full max-w-sm mt-1 px-2">
              <PlaybackControls
                isPlaying={isPlaying}
                currentTime={currentTime}
                duration={duration}
                isShuffle={isShuffle}
                repeatMode={repeatMode}
                onPlayPause={handlePlayPause}
                onNext={handleNextTrack}
                onPrevious={handlePreviousTrack}
                onSeek={handleSeek}
                onToggleShuffle={handleToggleShuffle}
                onCycleRepeat={handleCycleRepeat}
              />
            </div>
          </div>

          {/* LOWER 50%: Tall Physical CD Spine Shelf with Vertically Centered Spines */}
          <div className="w-full shrink-0">
            <CdShelf
              releases={filteredReleases}
              selectedIndex={selectedIndex}
              onSelectRelease={handleSelectRelease}
              shelfHeightClass="h-[370px] sm:h-[390px]"
            />
          </div>
        </div>
      ) : (
        /* TABLET LANDSCAPE LAYOUT */
        <div className="flex-1 flex flex-col justify-between overflow-hidden bg-[#121212]">
          {/* Upper Section Split: Artwork on Left, Details & Controls on Right */}
          <div className="flex-1 flex items-center justify-center px-8 py-3 gap-10 overflow-hidden min-h-0">
            {/* Left: Square Front Cover Artwork - Tapping toggles play/pause */}
            <div className="shrink-0 flex items-center justify-center">
              {currentRelease && (
                <CdJewelCase
                  release={currentRelease}
                  currentTrackIndex={currentTrackIndex}
                  isPlaying={isPlaying}
                  onPlayTrack={(idx) => {
                    setCurrentTrackIndex(idx);
                    playCurrentTrack(idx);
                  }}
                  onTogglePlayPause={handlePlayPause}
                  size="large"
                />
              )}
            </div>

            {/* Right: Album Title, Artist, Scrubber & Controls */}
            <div className="flex-1 max-w-md flex flex-col justify-center">
              <h2 className="text-2xl font-bold text-neutral-100 tracking-tight">
                {currentRelease?.title || 'Definitely Maybe'}
              </h2>
              <p className="text-sm text-neutral-400 mt-1 font-medium">
                {currentRelease?.artist || 'Oasis'}
              </p>

              {/* Scrubber & Controls */}
              <div className="mt-5 w-full">
                <PlaybackControls
                  isPlaying={isPlaying}
                  currentTime={currentTime}
                  duration={duration}
                  isShuffle={isShuffle}
                  repeatMode={repeatMode}
                  onPlayPause={handlePlayPause}
                  onNext={handleNextTrack}
                  onPrevious={handlePreviousTrack}
                  onSeek={handleSeek}
                  onToggleShuffle={handleToggleShuffle}
                  onCycleRepeat={handleCycleRepeat}
                />
              </div>
            </div>
          </div>

          {/* FULL-WIDTH Continuous Wall of CD Spines Across Tablet Shelf */}
          <div className="w-full shrink-0">
            <CdShelf
              releases={filteredReleases}
              selectedIndex={selectedIndex}
              onSelectRelease={handleSelectRelease}
              shelfHeightClass="h-[270px] sm:h-[290px]"
            />
          </div>
        </div>
      )}

      {/* Queue Drawer Modal */}
      {currentRelease && (
        <QueueDrawer
          isOpen={isQueueOpen}
          onClose={() => setIsQueueOpen(false)}
          currentRelease={currentRelease}
          currentTrackIndex={currentTrackIndex}
          isPlaying={isPlaying}
          onSelectTrack={(idx) => {
            setCurrentTrackIndex(idx);
            playCurrentTrack(idx);
          }}
          queue={currentRelease.tracks}
        />
      )}

      {/* Local Music Scanner Modal */}
      <LocalMusicModal
        isOpen={isLocalPickerOpen}
        onClose={() => setIsLocalPickerOpen(false)}
        onAddReleases={handleAddLocalReleases}
      />

      {/* Manual Artwork Correction & MusicBrainz Matcher Modal */}
      {currentRelease && (
        <CorrectArtworkModal
          isOpen={isCorrectModalOpen}
          onClose={() => setIsCorrectModalOpen(false)}
          release={currentRelease}
          onUpdateArtwork={handleUpdateReleaseArtwork}
        />
      )}

      {/* Android Studio Code Export & ZIP Download Modal */}
      <CodeExportModal
        isOpen={isExportModalOpen}
        onClose={() => setIsExportModalOpen(false)}
      />
    </AndroidFrame>
  );
}
